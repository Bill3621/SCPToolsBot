/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.commands.handler.bot.regulars;

import dev.vxrp.bot.regulars.RegularsManager;
import dev.vxrp.bot.regulars.handler.RegularsMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.RegularsTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RegularsCommand {
    private final Config config;
    private final Translation translation;

    public RegularsCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void view(SlashCommandInteractionEvent event) {
        User user = event.getOptions().get(0).getAsUser();
        if (!checkExistence(event, user)) return;

        var embed = new RegularsMessageHandler(event.getJDA(), config, translation).getSettings(event.getUser(), translation.regulars().embedSettingsViewTitle(), translation.regulars().embedSettingsViewBody());
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

    public void remove(SlashCommandInteractionEvent event) {
        User user = event.getOptions().get(0).getAsUser();
        if (!checkExistence(event, user)) return;

        var embed = new EmbedBuilder()
                .setColor(0x2ECC70)
                .setTitle(new ColorTool().parse(translation.regulars().embedSyncRemovedTitle()))
                .setDescription(new ColorTool().parse(translation.regulars().embedSyncRemovedBody()))
                .build();

        new RegularsManager(event.getJDA(), config, translation).removeSync(user.getId());
        event.replyEmbeds(embed).queue();
    }

    private boolean checkExistence(SlashCommandInteractionEvent event, User user) {
        if (user.isBot() || !new RegularsTable().exists(user.getId())) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.permissions().embedNotFoundTitle()))
                    .setDescription(new ColorTool().parse(translation.permissions().embedNotFoundBody()))
                    .build();

            event.replyEmbeds(embed).setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}

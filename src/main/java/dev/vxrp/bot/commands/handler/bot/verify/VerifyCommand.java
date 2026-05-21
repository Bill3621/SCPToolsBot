/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.commands.handler.bot.verify;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class VerifyCommand {
    private final Config config;
    private final Translation translation;

    public VerifyCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void pasteVerifyMenu(SlashCommandInteractionEvent event) {
        if (!config.settings().webserver().active()) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.permissions().embedCouldNotSendPanelTitle()))
                    .setDescription(new ColorTool().parse(translation.permissions().embedCouldNotSendPanelBody()))
                    .build();

            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        var embed = new EmbedBuilder()
                .setThumbnail(event.getGuild() != null ? event.getGuild().getIconUrl() : null)
                .setTitle(new ColorTool().parse(translation.verify().embedTemplateTitle()))
                .setDescription(new ColorTool().parse(translation.verify().embedTemplateBody()))
                .build();

        event.replyEmbeds(embed).setComponents(ActionRow.of(
                Button.link(config.settings().verify().oauthLink(), translation.buttons().textVerifyVerify()),
                Button.secondary("verify_show_data", translation.buttons().textVerifyShowData()),
                Button.danger("verify_delete", translation.buttons().textVerifyDelete()))).setEphemeral(true).queue();
    }
}

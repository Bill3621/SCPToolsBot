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

package dev.vxrp.bot.events.stringSelectMenus;

import dev.vxrp.bot.regulars.RegularsManager;
import dev.vxrp.bot.regulars.handler.RegularsMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class RegularsStringSelectMenus {
    private final StringSelectInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public RegularsStringSelectMenus(StringSelectInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String menuId = event.getComponent().getId();
        if (menuId == null) return;

        if (menuId.startsWith("regulars_group_select")) {
            var regularsMessageHandler = new RegularsMessageHandler(event.getJDA(), config, translation);

            new RegularsManager(event.getJDA(), config, translation).syncRegulars(event.getUser().getId(), event.getSelectedOptions().get(0).getValue());
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.regulars().embedSyncSentTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedSyncSentBody()))
                    .build();

            event.getMessage().delete().queue();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            event.replyEmbeds(new RegularsMessageHandler(event.getJDA(), config, translation).getSettings(event.getUser(), null, null)).addActionRow(
                    regularsMessageHandler.getSettingsActionRow(event.getUser().getId())
            ).setEphemeral(true).queue();
        }
    }
}

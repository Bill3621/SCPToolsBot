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

import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.bot.modals.ApplicationTemplateModals;
import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ApplicationTypeTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class ApplicationStringSelectMenus {
    private final StringSelectInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public ApplicationStringSelectMenus(StringSelectInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String menuId = event.getComponent().getId();
        if (menuId == null) return;

        if (menuId.startsWith("application_activation_add")) {
            String[] parts = menuId.split(":");
            event.replyModal(new ApplicationTemplateModals(translation).chooseCountModal(event.getSelectedOptions().get(0).getValue(), parts[2])).queue();
        }

        if (menuId.startsWith("application_activation_remove")) {
            String roleId = event.getSelectedOptions().get(0).getValue();
            String messageId = menuId.split(":")[2];

            event.deferEdit().queue();
            new ApplicationMessageHandler(config, translation).editActivationMessage(event.getUser().getId(), roleId, event.getChannel().asTextChannel(), messageId, null, null, null, false, event.getUser().getId(), 0);
        }

        if (menuId.startsWith("application_position")) {
            var typeEntry = new ApplicationTypeTable().query(event.getSelectedOptions().get(0).getValue());
            if (typeEntry != null && !typeEntry.active) {
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.application().embedPositionNotActiveTitle()))
                        .setDescription(new ColorTool().parse(translation.application().embedPositionNotActiveBody()))
                        .build();
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                event.replyModal(new TicketTemplateModals(translation).supportApplicationModal(event.getSelectedOptions().get(0).getValue())).queue();
            }
        }
    }
}

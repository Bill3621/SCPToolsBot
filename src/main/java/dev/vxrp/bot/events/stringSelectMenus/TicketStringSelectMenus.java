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

import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class TicketStringSelectMenus {
    private final StringSelectInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public TicketStringSelectMenus(StringSelectInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        var ticketTemplateModals = new TicketTemplateModals(translation);
        String selectedValue = event.getSelectedOptions().get(0).getValue();

        if (selectedValue.startsWith("general")) {
            event.replyModal(ticketTemplateModals.supportGeneralModal()).queue();
        }

        if (selectedValue.startsWith("report")) {
            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedReportUserTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedReportUserBody()))
                    .build();

            event.replyEmbeds(embed).setComponents(ActionRow.of(
                    EntitySelectMenu.create("ticket_report", EntitySelectMenu.SelectTarget.USER).build()))
                    .setEphemeral(true).queue();
        }

        if (selectedValue.startsWith("error")) {
            event.replyModal(ticketTemplateModals.supportErrorModal()).queue();
        }

        if (selectedValue.startsWith("unban")) {
            event.replyModal(ticketTemplateModals.supportUnbanModal()).queue();
        }

        if (selectedValue.startsWith("complaint")) {
            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedComplaintUserTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedComplaintUserBody()))
                    .build();

            event.replyEmbeds(embed).setComponents(ActionRow.of(
                    EntitySelectMenu.create("ticket_complaint", EntitySelectMenu.SelectTarget.USER).build()))
                    .setEphemeral(true).queue();
        }

        if (selectedValue.startsWith("application")) {
            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedApplicationPositionTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedApplicationPositionBody()))
                    .build();

            var menuBuilder = StringSelectMenu.create("application_position");
            for (var type : config.ticket().applicationTypes()) {
                menuBuilder.addOption(type.name(), type.roleID(), type.description(),
                        Emoji.fromFormatted(type.emoji()));
            }

            event.replyEmbeds(embed).setComponents(ActionRow.of(menuBuilder.build())).setEphemeral(true).queue();
        }
    }
}

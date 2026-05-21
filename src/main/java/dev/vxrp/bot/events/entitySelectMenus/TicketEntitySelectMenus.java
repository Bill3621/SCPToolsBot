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

package dev.vxrp.bot.events.entitySelectMenus;

import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class TicketEntitySelectMenus {
    private final EntitySelectInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public TicketEntitySelectMenus(EntitySelectInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;

        String menuId = event.getComponent().getId();
        if (menuId == null) return;

        if (menuId.startsWith("ticket_report")) {
            var user = event.getValues().get(0);
            event.replyModal(new TicketTemplateModals(translation).supportReportModal(user.getId())).queue();
        }

        if (menuId.startsWith("ticket_complaint")) {
            var user = event.getValues().get(0);
            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedComplaintAnonymousTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedComplaintAnonymousBody()))
                    .build();

            event.replyEmbeds(embed).setActionRow(
                    Button.success("ticket_anonymous_accept:" + user.getId(), translation.buttons().textSupportAnonymousAccept()).withEmoji(Emoji.fromFormatted("🔒")),
                    Button.danger("ticket_anonymous_deny:" + user.getId(), translation.buttons().textSupportAnonymousDeny()).withEmoji(Emoji.fromFormatted("🔓"))
            ).setEphemeral(true).queue();
        }
    }
}

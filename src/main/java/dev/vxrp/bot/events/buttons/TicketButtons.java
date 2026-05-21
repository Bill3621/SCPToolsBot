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

package dev.vxrp.bot.events.buttons;

import dev.vxrp.bot.modals.GlobalTemplateModals;
import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.PermissionType;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.bot.ticket.handler.TicketSettingsHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.TicketTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.time.Instant;

public class TicketButtons {
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;
    private final net.dv8tion.jda.api.entities.MessageEmbed noHandlerEmbed;

    public TicketButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
        this.noHandlerEmbed = new EmbedBuilder()
                .setTitle(new ColorTool().parse(translation.support().embedTicketNoHandlerTitle()))
                .setDescription(new ColorTool().parse(translation.support().embedTicketNoHandlerBody()))
                .build();
    }

    public void init() {
        var api = event.getJDA();
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null)
            return;

        if (buttonId.startsWith("ticket_anonymous_accept")) {
            String userId = buttonId.split(":")[1];
            event.replyModal(new TicketTemplateModals(translation).supportComplaintModal(userId, true)).queue();
        }

        if (buttonId.startsWith("ticket_anonymous_deny")) {
            String userId = buttonId.split(":")[1];
            event.replyModal(new TicketTemplateModals(translation).supportComplaintModal(userId, false)).queue();
        }

        if (buttonId.startsWith("ticket_claim")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.support().embedLogClaimedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogClaimedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).claimTicket(event.getUser(),
                    event.getChannel().asThreadChannel(), event.getChannelId(), event.getUser().getId());
        }

        if (buttonId.startsWith("ticket_close")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;

            event.replyModal(new GlobalTemplateModals(translation).reasonModal("ticket_close:" + event.getChannelId()))
                    .queue();
        }

        if (buttonId.startsWith("ticket_settings")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;
            if (new TicketTable().determineHandler(event.getChannelId())) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var ticketHandler = new TicketSettingsHandler(api, config, translation);

            var settings = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedSettingsTitle()
                            .replace("%user%", event.getUser().getGlobalName())))
                    .setDescription(new ColorTool().parse(translation.support().embedSettingsBody()))
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(settings).setComponents(
                    ticketHandler.settingsActionRow(new TicketTable().getTicketStatus(event.getChannelId())))
                    .setEphemeral(true).queue();
        }

        if (buttonId.startsWith("ticket_setting_open")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;
            if (new TicketTable().determineHandler(event.getChannelId())) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.support().embedLogOpenedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogOpenedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            event.getMessage().delete().queue();
            new TicketSettingsHandler(api, config, translation).openTicket(event.getUser(),
                    event.getChannel().asThreadChannel(), event.getChannelId());
        }

        if (buttonId.startsWith("ticket_setting_pause")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;
            if (new TicketTable().determineHandler(event.getChannelId())) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var embed = new EmbedBuilder()
                    .setColor(0xf1c40f)
                    .setTitle(new ColorTool().parse(translation.support().embedLogPausedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogPausedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            event.getMessage().delete().queue();
            new TicketSettingsHandler(api, config, translation).pauseTicket(event.getUser(),
                    event.getChannel().asThreadChannel(), event.getChannelId());
        }

        if (buttonId.startsWith("ticket_setting_suspend")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;
            if (new TicketTable().determineHandler(event.getChannelId())) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.support().embedLogSuspendedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogSuspendedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            event.getMessage().delete().queue();
            new TicketSettingsHandler(api, config, translation).suspendTicket(event.getUser(),
                    event.getChannel().asThreadChannel(), event.getChannelId());
        }

        if (buttonId.startsWith("ticket_setting_close")) {
            if (permissionCheck(PermissionType.TICKET, new TicketTable().determineTicketType(event.getChannelId())))
                return;

            event.replyModal(new GlobalTemplateModals(translation).reasonModal("ticket_close:" + event.getChannelId()))
                    .queue();
        }

        if (buttonId.startsWith("ticket_log_claim")) {
            String channelId = buttonId.split(":")[1];
            if (permissionCheck(PermissionType.TICKET_LOGS, new TicketTable().determineTicketType(channelId)))
                return;

            var channel = event.getJDA().getThreadChannelById(channelId);
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.support().embedLogClaimedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogClaimedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).claimTicket(event.getUser(), channel, channelId,
                    event.getUser().getId());
        }

        if (buttonId.startsWith("ticket_log_open")) {
            String channelId = buttonId.split(":")[1];
            if (permissionCheck(PermissionType.TICKET_LOGS, new TicketTable().determineTicketType(channelId)))
                return;
            if (new TicketTable().determineHandler(channelId)) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var channel = event.getJDA().getThreadChannelById(channelId);
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.support().embedLogOpenedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogOpenedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).openTicket(event.getUser(), channel, channelId);
        }

        if (buttonId.startsWith("ticket_log_pause")) {
            String channelId = buttonId.split(":")[1];
            if (permissionCheck(PermissionType.TICKET_LOGS, new TicketTable().determineTicketType(channelId)))
                return;
            if (new TicketTable().determineHandler(channelId)) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var channel = event.getJDA().getThreadChannelById(channelId);
            var embed = new EmbedBuilder()
                    .setColor(0xf1c40f)
                    .setTitle(new ColorTool().parse(translation.support().embedLogPausedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogPausedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).pauseTicket(event.getUser(), channel, channelId);
        }

        if (buttonId.startsWith("ticket_log_suspend")) {
            String channelId = buttonId.split(":")[1];
            if (permissionCheck(PermissionType.TICKET_LOGS, new TicketTable().determineTicketType(channelId)))
                return;
            if (new TicketTable().determineHandler(channelId)) {
                event.replyEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var channel = event.getJDA().getThreadChannelById(channelId);
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.support().embedLogSuspendedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogSuspendedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).suspendTicket(event.getUser(), channel, channelId);
        }

        if (buttonId.startsWith("ticket_log_close")) {
            String channelId = buttonId.split(":")[1];
            if (permissionCheck(PermissionType.TICKET_LOGS, new TicketTable().determineTicketType(channelId)))
                return;

            event.replyModal(new GlobalTemplateModals(translation).reasonModal("ticket_close:" + channelId)).queue();
        }
    }

    private boolean permissionCheck(PermissionType permissionType, TicketType ticketType) {
        var result = new PermissionManager(config, translation).determinePermissions(event.getUser(), permissionType,
                ticketType);
        if (result.getEmbed() != null) {
            event.replyEmbeds(result.getEmbed()).setEphemeral(true).queue();
        }
        return !result.isPermitted();
    }
}

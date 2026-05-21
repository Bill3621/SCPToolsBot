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

package dev.vxrp.bot.ticket.handler;

import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.TicketTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

public class TicketLogHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(TicketLogHandler.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public TicketLogHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public String logMessage(String creator, User handler, String ticketId, TicketStatus ticketStatus, ThreadChannel childChannel) {
        MessageEmbed logEmbed = createMessage(creator, handler, ticketStatus, childChannel, false, null, "None");

        var channel = api.getTextChannelById(config.ticket().settings().ticketLogChannel());
        if (channel == null) {
            logger.error("Could not send ticket log message for ticket '{}'", ticketId);
            return null;
        }

        boolean isHandled = handler != null;

        return channel.sendMessageEmbeds(logEmbed)
                .setActionRow(logActionRow(ticketStatus, isHandled, ticketId))
                .complete()
                .getId();
    }

    public void editMessage(String ticketId, String creator, User handler, TicketStatus ticketStatus) {
        String logMessage = new TicketTable().getLogMessage(ticketId);
        String creatorId = new TicketTable().getTicketCreator(ticketId);
        String handlerId = new TicketTable().getTicketHandler(ticketId);
        TicketStatus status = new TicketTable().getTicketStatus(ticketId);

        var child = api.getThreadChannelById(ticketId);
        User handlerUser = null;
        if (handlerId != null) {
            handlerUser = api.retrieveUserById(handlerId).complete();
        }

        if (creator != null) creatorId = creator;
        if (handler != null) handlerUser = handler;
        if (ticketStatus != null) status = ticketStatus;

        MessageEmbed logEmbed = createMessage(creatorId, handlerUser, status, child, false, null, "None");

        var channel = api.getTextChannelById(config.ticket().settings().ticketLogChannel());
        if (channel == null) {
            logger.error("Could not edit ticket log message for ticket '{}'", ticketId);
            return;
        }

        boolean isHandled = handlerId != null;

        channel.editMessageEmbedsById(logMessage, logEmbed)
                .setActionRow(logActionRow(status, isHandled, ticketId))
                .queue();
    }

    public void closeMessage(String ticketId, User closedUser, String reason) {
        String logMessage = new TicketTable().getLogMessage(ticketId);

        String creator = new TicketTable().getTicketCreator(ticketId);
        User handler = api.retrieveUserById(new TicketTable().getTicketHandler(ticketId)).complete();
        var childChannel = api.getThreadChannelById(ticketId);

        var channel = api.getTextChannelById(config.ticket().settings().ticketLogChannel());
        if (channel == null) {
            logger.error("Could not delete ticket log message for ticket '{}'", ticketId);
            return;
        }

        var message = channel.retrieveMessageById(logMessage).complete();
        message.editMessageComponents().queue();
        message.editMessageEmbeds(createMessage(creator, handler, TicketStatus.CLOSED, childChannel, true, closedUser, reason)).queue();
    }

    private MessageEmbed createMessage(String ticketCreator, User ticketHandler, TicketStatus ticketStatus, ThreadChannel childChannel, boolean closedMessage, User closedUser, String reason) {
        ColorTool colorTool = new ColorTool();
        String thumbnailUrl = "";
        String creatorUserMention = "anonymous";
        String creatorUserName = "anonymous";
        String handlerUserName = "none";

        if (!"anonymous".equals(ticketCreator)) {
            User creatorUser = api.retrieveUserById(ticketCreator).complete();
            creatorUserName = creatorUser.getGlobalName();
            thumbnailUrl = String.valueOf(creatorUser.getAvatarUrl());
            creatorUserMention = api.retrieveUserById(ticketCreator).complete().getAsMention();
        }
        if (ticketHandler != null) handlerUserName = ticketHandler.getAsMention();

        int usableColor = 0x2ECC70;
        String usableTitle = colorTool.parse(translation.support().embedLogTitle()
                .replace("%name%", childChannel != null ? childChannel.getName() : "unknown")
                .replace("%user%", creatorUserName));
        String usableDescription = colorTool.parse(translation.support().embedLogBody()
                .replace("%status%", ticketStatus.toString())
                .replace("%channel%", childChannel != null ? childChannel.getAsMention() : "unknown")
                .replace("%creator%", creatorUserMention)
                .replace("%handler%", handlerUserName));

        if (closedMessage) {
            usableColor = 0xE74D3C;
            usableTitle = colorTool.parse(translation.support().embedClosedLogTitle()
                    .replace("%name%", childChannel != null ? childChannel.getName() : "unknown")
                    .replace("%user%", creatorUserName));

            usableDescription = colorTool.parse(translation.support().embedClosedLogBody()
                    .replace("%status%", ticketStatus.toString())
                    .replace("%channel%", childChannel != null ? childChannel.getAsMention() : "unknown")
                    .replace("%creator%", creatorUserMention)
                    .replace("%handler%", handlerUserName)
                    .replace("%closed_user%", closedUser != null ? closedUser.getAsMention() : "unknown")
                    .replace("%reason%", reason));
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(usableColor)
                .setTitle(usableTitle)
                .setDescription(usableDescription)
                .setTimestamp(Instant.now());

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            builder.setThumbnail(thumbnailUrl);
        }

        return builder.build();
    }

    private Collection<ItemComponent> logActionRow(TicketStatus status, boolean handler, String ticketId) {
        Collection<ItemComponent> rows = new ArrayList<>();

        Button claim = Button.primary("ticket_log_claim:" + ticketId, translation.buttons().textSupportLogClaim()).withEmoji(Emoji.fromFormatted("📫"));
        Button open = Button.success("ticket_log_open:" + ticketId, translation.buttons().textSupportLogOpen()).withEmoji(Emoji.fromFormatted("🚪"));
        Button pause = Button.primary("ticket_log_pause:" + ticketId, translation.buttons().textSupportLogPause()).withEmoji(Emoji.fromFormatted("🌙"));
        Button suspend = Button.primary("ticket_log_suspend:" + ticketId, translation.buttons().textSupportLogSuspend()).withEmoji(Emoji.fromFormatted("🔒"));
        Button close = Button.danger("ticket_log_close:" + ticketId, translation.buttons().textSupportLogClose()).withEmoji(Emoji.fromFormatted("🪫"));

        if (handler) claim = claim.asDisabled();
        switch (status) {
            case OPEN -> open = open.asDisabled();
            case PAUSED -> pause = pause.asDisabled();
            case SUSPENDED -> suspend = suspend.asDisabled();
            case CLOSED -> close = close.asDisabled();
        }

        rows.add(claim);
        rows.add(open);
        rows.add(pause);
        rows.add(suspend);
        rows.add(close);

        return rows;
    }
}

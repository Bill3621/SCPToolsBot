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

package dev.vxrp.bot.noticeofdeparture.handler;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NoticeOfDepartureMessageHandler {
    private final JDA api;
    private final Config config;
    private final Translation translation;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(NoticeOfDepartureMessageHandler.class);

    public NoticeOfDepartureMessageHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void sendTemplate(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedTemplateTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedTemplateBody()
                        .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting())));

        channel.sendMessageEmbeds(builder.build()).setComponents(ActionRow.of(
                Button.success("notice_of_departure_file", translation.buttons().textNoticeOfDepartureFile())
                        .withEmoji(Emoji.fromFormatted("\u23F0"))))
                .queue();
    }

    public void sendDecisionMessage(String userId, String date, String reason) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = LocalDate.parse(date, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();
        String relativeTime = TimeFormat.RELATIVE.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();

        var user = api.retrieveUserById(userId).complete();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(new ColorTool().parse(
                translation.noticeOfDeparture().embedDecisionTitle()
                        .replace("%number%", String.valueOf(new NoticeOfDepartureTable().retrieveSerial() + 1))
                        .replace("%user%", String.valueOf(user.getGlobalName()))));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedDecisionBody()
                        .replace("%current_date%", discordCurrentDate)
                        .replace("%end_date%", discordEndDate)
                        .replace("%relative%", relativeTime)
                        .replace("%reason%", reason)));
        builder.setTimestamp(Instant.now());

        TextChannel channel = api.getTextChannelById(config.settings().noticeOfDeparture().decisionChannel());
        if (channel != null) {
            channel.sendMessageEmbeds(builder.build()).setComponents(ActionRow.of(
                    Button.success("notice_of_departure_decision_accept:" + userId + ":" + endDate.format(formatter),
                            translation.buttons().textNoticeOfDepartureAccept()).withEmoji(
                                    Emoji.fromFormatted("\uD83D\uDCD8")),
                    Button.danger("notice_of_departure_decision_dismiss:" + userId + ":" + endDate.format(formatter),
                            translation.buttons().textNoticeOfDepartureDismissed()).withEmoji(
                                    Emoji.fromFormatted("\uFAF7"))))
                    .queue();
        } else {
            logger.error("Could not correctly retrieve notice of departure decision channel, does it exist?");
        }
    }

    public void sendAcceptedMessage(String reason, String userId, String date) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = LocalDate.parse(date, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();
        String relativeTime = TimeFormat.RELATIVE.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x2ECC70);
        builder.setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedAcceptedTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedAcceptedBody()
                        .replace("%current_date%", discordCurrentDate)
                        .replace("%end_date%", discordEndDate)
                        .replace("%relative%", relativeTime)
                        .replace("%reason%", reason)));

        var privateChannel = api.retrieveUserById(userId).complete().openPrivateChannel().complete();
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public void sendDismissedMessage(String reason, String userId) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0xE74D3C);
        builder.setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedDismissedTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedDismissedBody()
                        .replace("%reason%", reason)));

        var privateChannel = api.retrieveUserById(userId).complete().openPrivateChannel().complete();
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public void sendNoticeMessage(String reason, String handlerId, String userId, String date) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = LocalDate.parse(date, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();
        String relativeTime = TimeFormat.RELATIVE.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();

        var handler = api.retrieveUserById(handlerId).complete();
        var user = api.retrieveUserById(userId).complete();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(new ColorTool().parse(
                translation.noticeOfDeparture().embedNoticeTitle()
                        .replace("%number%", String.valueOf(new NoticeOfDepartureTable().retrieveSerial() + 1))
                        .replace("%user%", String.valueOf(user.getGlobalName()))));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedNoticeBody()
                        .replace("%user%", handler.getAsMention())
                        .replace("%current_date%", discordCurrentDate)
                        .replace("%end_date%", discordEndDate)
                        .replace("%relative%", relativeTime)
                        .replace("%reason%", reason)));

        TextChannel channel = api.getTextChannelById(config.settings().noticeOfDeparture().noticeChannel());
        if (channel == null) {
            logger.error("Could not correctly retrieve notice of departure notice channel, does it exist?");
            return;
        }

        Message message = channel.sendMessageEmbeds(builder.build()).setComponents(ActionRow.of(
                Button.danger("notice_of_departure_revoke:" + userId + ":" + endDate.format(formatter),
                        translation.buttons().textNoticeOfDepartureRevoked())))
                .complete();

        new NoticeOfDepartureTable().addToDatabase(userId, true, handlerId, channel.getId(), message.getId(),
                currentDate.format(formatter), endDate.format(formatter));
    }

    public void sendRevokedMessage(String reason, String userId, String beginDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.parse(beginDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG
                .atInstant(parsedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0xE74D3C);
        builder.setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedRevokedTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedRevokedBody()
                        .replace("%current_date%", discordCurrentDate)
                        .replace("%end_date%", discordEndDate)
                        .replace("%reason%", reason)));

        var privateChannel = api.retrieveUserById(userId).complete().openPrivateChannel().complete();
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public void sendEndedMessage(String userId, String beginDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.parse(beginDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG
                .atInstant(parsedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0xE74D3C);
        builder.setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedEndedTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.noticeOfDeparture().embedEndedBody()
                        .replace("%current_date%", discordCurrentDate)
                        .replace("%end_date%", discordEndDate)));

        try {
            var privateChannel = api.awaitReady().retrieveUserById(userId).complete().openPrivateChannel().complete();
            privateChannel.sendMessageEmbeds(builder.build()).queue();

            NoticeOfDepartureTable table = new NoticeOfDepartureTable();
            String channelId = table.retrieveChannel(userId);
            String messageId = table.retrieveMessage(userId);
            if (channelId != null && messageId != null) {
                TextChannel channel = api.awaitReady().getTextChannelById(channelId);
                if (channel != null) {
                    channel.retrieveMessageById(messageId).complete().delete().complete();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
        String dateFormat = config.settings().noticeOfDeparture().dateFormatting();
        String exampleDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern(dateFormat));
        String title = new ColorTool().parse(translation.noticeOfDeparture().embedTemplateTitle());
        String intro = new ColorTool().parse(translation.noticeOfDeparture().embedTemplateBody());
        String returnDate = new ColorTool().parse(translation.noticeOfDeparture().textTemplateReturnDate()
                .replace("%formatter%", dateFormat)
                .replace("%example%", exampleDate));
        String startDate = new ColorTool().parse(translation.noticeOfDeparture().textTemplateStartDate());
        String reason = new ColorTool().parse(translation.noticeOfDeparture().textTemplateReason());
        Button button = Button.success("notice_of_departure_file", translation.buttons().textNoticeOfDepartureFile())
                .withEmoji(Emoji.fromFormatted("\u23F0"));

        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + title + "\n" + intro));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("### " + translation.noticeOfDeparture().modalTimeTitle() + "\n" + returnDate));
        content.add(TextDisplay.of("### " + translation.noticeOfDeparture().modalStartDateTitle() + "\n" + startDate));
        content.add(TextDisplay.of("### " + translation.noticeOfDeparture().modalExplanationTitle() + "\n" + reason));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("-# " + new ColorTool().parse(translation.noticeOfDeparture().textTemplateFooter())));
        content.add(ActionRow.of(button));
        channel.sendMessage(message(content, null)).queue();
    }

    public void sendNoticeMessage(String reason, String handlerId, String userId, String date, String startDate) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        LocalDate currentDate = LocalDate.parse(startDate, formatter);
        LocalDate endDate = LocalDate.parse(date, formatter);

        String discordCurrentDate = TimeFormat.DATE_LONG
                .atInstant(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
        String discordEndDate = TimeFormat.DATE_LONG.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();
        String relativeTime = TimeFormat.RELATIVE.atInstant(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .toString();

        var user = api.retrieveUserById(userId).complete();
        String title = new ColorTool().parse(translation.noticeOfDeparture().embedNoticeTitle()
                .replace("%number%", String.valueOf(new NoticeOfDepartureTable().retrieveSerial() + 1))
                .replace("%user%", user.getEffectiveName()));

        TextChannel channel = api.getTextChannelById(config.settings().noticeOfDeparture().noticeChannel());
        if (channel == null) {
            logger.error("Could not correctly retrieve notice of departure notice channel, does it exist?");
            return;
        }

        Button button = Button.danger("notice_of_departure_revoke:" + userId + ":" + endDate.format(formatter),
                translation.buttons().textNoticeOfDepartureRevoked());
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + title));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(discordCurrentDate + " → " + discordEndDate + " · " + relativeTime));
        content.add(TextDisplay.of("**" + translation.noticeOfDeparture().modalExplanationTitle() + "**\n> "
                + reason.replace("\n", "\n> ")));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(ActionRow.of(button));
        Message message = channel.sendMessage(message(content, null)).complete();

        new NoticeOfDepartureTable().addToDatabase(userId, true, handlerId, channel.getId(), message.getId(),
                startDate, endDate.format(formatter));
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

        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + new ColorTool().parse(translation.noticeOfDeparture().embedRevokedTitle())));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(discordCurrentDate + " → " + discordEndDate));
        content.add(TextDisplay.of("**" + translation.noticeOfDeparture().modalExplanationTitle() + "**\n> "
                + reason.replace("\n", "\n> ")));
        MessageCreateData message = message(content, 0xE74D3C);

        api.retrieveUserById(userId).queue(user -> user.openPrivateChannel().queue(
                channel -> channel.sendMessage(message).queue(),
                error -> logger.warn("Could not open a private channel to notify {} about their revoked notice", userId)),
                error -> logger.warn("Could not retrieve user {} to notify them about their revoked notice", userId));
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

        String title = new ColorTool().parse(translation.noticeOfDeparture().embedEndedTitle());
        String summary = new ColorTool().parse(translation.noticeOfDeparture().embedEndedBody());
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + title));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(summary + "\n" + discordCurrentDate + " → " + discordEndDate));
        MessageCreateData message = message(content, null);

        api.retrieveUserById(userId).queue(user -> user.openPrivateChannel().queue(
                channel -> channel.sendMessage(message).queue(),
                error -> logger.warn("Could not open a private channel to notify {} about their ended notice", userId)),
                error -> logger.warn("Could not retrieve user {} to notify them about their ended notice", userId));

        NoticeOfDepartureTable table = new NoticeOfDepartureTable();
        String channelId = table.retrieveChannel(userId);
        String messageId = table.retrieveMessage(userId);
        if (channelId != null) {
            TextChannel channel = api.getTextChannelById(channelId);
            if (channel != null) {
                List<ContainerChildComponent> publicContent = new ArrayList<>();
                publicContent.add(TextDisplay.of("## " + title));
                publicContent.add(Separator.createDivider(Separator.Spacing.SMALL));
                publicContent.add(TextDisplay.of("<@" + userId + ">\n" + summary + "\n"
                        + discordCurrentDate + " → " + discordEndDate));
                channel.sendMessage(message(publicContent, null)).queue();
                if (messageId != null) {
                    channel.retrieveMessageById(messageId).queue(notice -> notice.delete().queue(),
                            error -> logger.warn("Could not delete expired notice of departure message for {}", userId));
                }
            }
        }
    }

    public static MessageCreateData feedback(String title, String body, boolean success) {
        return message(List.of(TextDisplay.of("### " + title + "\n" + body.strip())),
                success ? 0x2ECC70 : 0xE74D3C);
    }

    public static MessageCreateData error(MessageEmbed embed) {
        return feedback(embed.getTitle(), embed.getDescription(), false);
    }

    public static MessageCreateData noticeDetails(String title, String filedBy, String currentDate,
                                                  String endDate, String reasonUnavailable) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + title));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(filedBy + "\n" + currentDate + " → " + endDate));
        content.add(TextDisplay.of("-# " + reasonUnavailable));
        return message(content, null);
    }

    private static MessageCreateData message(List<? extends ContainerChildComponent> content, Integer accentColor) {
        Container container = Container.of(content);
        if (accentColor != null) container = container.withAccentColor(accentColor);
        return new MessageCreateBuilder()
                .useComponentsV2()
                .setAllowedMentions(List.of())
                .setComponents(List.of(container))
                .build();
    }
}

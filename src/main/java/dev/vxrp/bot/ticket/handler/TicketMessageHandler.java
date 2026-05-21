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
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.TicketTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TicketMessageHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(TicketMessageHandler.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public TicketMessageHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void sendTemplate(TextChannel channel, Guild guild) {
        ColorTool colorTool = new ColorTool();
        MessageEmbed embed = new EmbedBuilder()
                .setThumbnail(guild.getIconUrl())
                .setTitle(colorTool.parse(translation.support().embedTemplateSupportTitle()).trim())
                .setDescription(colorTool.parse(translation.support().embedTemplateSupportBody()).trim())
                .build();

        channel.sendMessageEmbeds(embed)
                .setComponents(ActionRow.of(StringSelectMenu.create("ticket")
                        .addOption(translation.selectMenus().textSupportNameGeneral(), "general",
                                translation.selectMenus().textsupportDescriptionGeneral(), Emoji.fromFormatted("⚙️"))
                        .addOption(translation.selectMenus().textSupportNameReport(), "report",
                                translation.selectMenus().textSupportDescriptionReport(), Emoji.fromFormatted("⚖️"))
                        .addOption(translation.selectMenus().textSupportNameError(), "error",
                                translation.selectMenus().textSupportDescriptionError(), Emoji.fromFormatted("⛓️‍💥"))
                        .addOption(translation.selectMenus().textSupportNameUnban(), "unban",
                                translation.selectMenus().textSupportDescriptionUnban(), Emoji.fromFormatted("⌛"))
                        .addOption(translation.selectMenus().textSupportNameComplaint(), "complaint",
                                translation.selectMenus().textSupportDescriptionComplaint(), Emoji.fromFormatted("🚫"))
                        .addOption(translation.selectMenus().textSupportNameApplication(), "application",
                                translation.selectMenus().textSupportDescriptionApplication(),
                                Emoji.fromFormatted("📩"))
                        .build()))
                .queue();
    }

    public Message sendMessage(TicketType type, ThreadChannel channel, String userId, String modalId,
            List<ModalMapping> modalValues) {
        switch (type) {
            case GENERAL -> {
                return channel.sendMessageEmbeds(generalMessage(channel, userId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.GENERAL, false))
                        .complete();
            }
            case REPORT -> {
                return channel.sendMessageEmbeds(reportMessage(channel, userId, modalId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.REPORT, false))
                        .complete();
            }
            case ERROR -> {
                return channel.sendMessageEmbeds(errorMessage(channel, userId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.ERROR, false))
                        .complete();
            }
            case UNBAN -> {
                return channel.sendMessageEmbeds(unbanMessage(channel, userId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.UNBAN, false))
                        .complete();
            }
            case COMPLAINT -> {
                return channel.sendMessageEmbeds(complaintMessage(channel, userId, modalId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.COMPLAINT, false))
                        .complete();
            }
            case APPLICATION -> {
                return channel.sendMessageEmbeds(applicationMessage(channel, userId, modalId, modalValues))
                        .setComponents(messageActionRow(TicketStatus.OPEN, TicketType.APPLICATION, false))
                        .complete();
            }
            default -> {
                return null;
            }
        }
    }

    public void editMessage(String ticketId, ThreadChannel ticketChannel, TicketStatus ticketStatus) {
        String message = new TicketTable().getMessage(ticketId);

        String handlerId = new TicketTable().getTicketHandler(ticketId);
        TicketStatus status = new TicketTable().getTicketStatus(ticketId);
        TicketType ticketType = new TicketTable().getTicketType(ticketId);

        if (ticketStatus != null)
            status = ticketStatus;

        boolean isHandled = handlerId != null;

        ticketChannel.editMessageById(message, "")
                .setComponents(messageActionRow(status, ticketType, isHandled))
                .queue();
    }

    private MessageEmbed generalMessage(ThreadChannel channel, String userId, List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        User user = api.retrieveUserById(userId).complete();

        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setTitle(colorTool
                        .parse(translation.support().embedTicketGeneralTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketGeneralBody()
                        .replace("%issuerId%", userId)
                        .replace("%subject%", modalValues.get(0).getAsString())
                        .replace("%explanation%", modalValues.get(1).getAsString())))
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed reportMessage(ThreadChannel channel, String userId, String modalId,
            List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        User user = api.retrieveUserById(userId).complete();

        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setTitle(colorTool
                        .parse(translation.support().embedTicketReportTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketReportBody()
                        .replace("%issuerId%", userId)
                        .replace("%reported%", "<@" + modalId.split(":")[1] + ">")
                        .replace("%reason%", modalValues.get(0).getAsString())
                        .replace("%proof%", modalValues.get(1).getAsString())))
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed errorMessage(ThreadChannel channel, String userId, List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        User user = api.retrieveUserById(userId).complete();

        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setTitle(colorTool
                        .parse(translation.support().embedTicketErrorTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketErrorBody()
                        .replace("%issuerId%", userId)
                        .replace("%problem%", modalValues.get(0).getAsString())
                        .replace("%times%", modalValues.get(1).getAsString())
                        .replace("%reproduce%", modalValues.get(2).getAsString())
                        .replace("%additional%", modalValues.get(3).getAsString())))
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed unbanMessage(ThreadChannel channel, String userId, List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        User user = api.retrieveUserById(userId).complete();

        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setTitle(colorTool
                        .parse(translation.support().embedTicketUnbanTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketUnbanBody()
                        .replace("%issuerId%", userId)
                        .replace("%steamId%", modalValues.get(0).getAsString())
                        .replace("%reason%", modalValues.get(1).getAsString())))
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed complaintMessage(ThreadChannel channel, String userId, String modalId,
            List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        String userMention = "**Anonymous**";
        String staff = "Anonymous";
        if (!"anonymous".equals(modalId.split(":")[1]))
            staff = "<@" + modalId.split(":")[1] + ">";
        if (!"anonymous".equals(userId))
            userMention = "<@" + userId + ">";

        String authorName;
        String authorIcon;
        if (!"anonymous".equals(userId)) {
            User u = api.retrieveUserById(userId).complete();
            authorName = u.getGlobalName();
            authorIcon = u.getAvatarUrl();
        } else {
            authorName = "Anonymous";
            authorIcon = "https://www.pngarts.com/files/4/Anonymous-Mask-Transparent-Images.png";
        }

        return new EmbedBuilder()
                .setAuthor(authorName, null, authorIcon)
                .setTitle(colorTool
                        .parse(translation.support().embedTicketComplaintTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketComplaintBody()
                        .replace("%issuerId%", userMention)
                        .replace("%staff%", staff)
                        .replace("%reason%", modalValues.get(0).getAsString())
                        .replace("%proof%", modalValues.get(1).getAsString())))
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed applicationMessage(ThreadChannel channel, String userId, String modalId,
            List<ModalMapping> modalValues) {
        ColorTool colorTool = new ColorTool();
        User user = api.retrieveUserById(userId).complete();

        return new EmbedBuilder()
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setTitle(colorTool.parse(
                        translation.support().embedTicketApplicationTitle().replace("%name%", channel.getName())))
                .setDescription(colorTool.parse(translation.support().embedTicketApplicationBody()
                        .replace("%issuerId%", userId)
                        .replace("%roleId%", modalId.split(":")[1])
                        .replace("%name%", modalValues.get(0).getAsString()))
                        .replace("%age%", modalValues.get(1).getAsString())
                        .replace("%playtime%", modalValues.get(2).getAsString())
                        .replace("%reason%", modalValues.get(3).getAsString())
                        .replace("%skills%", modalValues.get(4).getAsString()))
                .setTimestamp(Instant.now())
                .build();
    }

    public void sendClosedMessage(String userId, String handlerId, ThreadChannel threadChannel, String reason) {
        if ("anonymous".equals(userId))
            return;
        User user = api.retrieveUserById(userId).complete();
        User handler = api.retrieveUserById(handlerId).complete();

        ColorTool colorTool = new ColorTool();
        MessageEmbed embed = new EmbedBuilder()
                .setColor(0xE74D3C)
                .setTitle(colorTool.parse(translation.support().embedClosedTitle()))
                .setDescription(colorTool.parse(translation.support().embedClosedBody()
                        .replace("%name%", threadChannel.getName())
                        .replace("%handler%", handler.getAsMention())
                        .replace("%ticket%", threadChannel.getAsMention())
                        .replace("%reason%", reason)))
                .build();

        user.openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
    }

    private ActionRow messageActionRow(TicketStatus status, TicketType type, boolean handler) {
        Collection<ActionRowChildComponent> rows = new ArrayList<>();

        Button claim = Button.primary("ticket_claim", translation.buttons().ticketSupportClaim())
                .withEmoji(Emoji.fromFormatted("🚪"));
        Button close = Button.danger("ticket_close:" + type, translation.buttons().ticketSupportClose())
                .withEmoji(Emoji.fromFormatted("🪫"));
        Button settings = Button.secondary("ticket_settings", translation.buttons().textSupportSettings())
                .withEmoji(Emoji.fromFormatted("⚙️"));

        if (handler)
            claim = claim.asDisabled();
        if (status == TicketStatus.CLOSED)
            close = close.asDisabled();

        rows.add(claim);
        rows.add(close);
        rows.add(settings);

        return ActionRow.of(rows);
    }
}

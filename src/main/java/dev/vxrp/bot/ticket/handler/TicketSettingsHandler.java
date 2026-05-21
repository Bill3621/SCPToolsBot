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
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.TicketTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class TicketSettingsHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(TicketSettingsHandler.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public TicketSettingsHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void claimTicket(User user, ThreadChannel ticketChannel, String id, String userId) {
        ColorTool colorTool = new ColorTool();
        var embed = new EmbedBuilder()
                .setAuthor(user.getGlobalName(), null, user.getAvatarUrl())
                .setColor(0x2ECC70)
                .setTitle(colorTool.parse(translation.support().embedTicketClaimedTitle().replace("%user%", user.getGlobalName())))
                .setDescription(colorTool.parse(translation.support().embedTicketClaimedBody().replace("%user%", user.getAsMention())))
                .build();

        ticketChannel.sendMessageEmbeds(embed).queue();

        User handlerUser = api.retrieveUserById(userId).complete();

        new TicketTable().updateTicketHandler(id, userId);
        new TicketMessageHandler(api, config, translation).editMessage(id, ticketChannel, null);
        new TicketLogHandler(api, config, translation).editMessage(id, null, handlerUser, null);

        if (new TicketTable().getTicketType(id) == TicketType.APPLICATION) {
            new ApplicationTable().updateTicketHandler(id, handlerUser.getId());
        }

        logger.info("Ticket {} claimed by user: {}", id, user.getId());
    }

    public void openTicket(User user, ThreadChannel ticketChannel, String id) {
        ColorTool colorTool = new ColorTool();
        var embed = new EmbedBuilder()
                .setAuthor(user.getGlobalName(), null, user.getAvatarUrl())
                .setColor(0x2ECC70)
                .setTitle(colorTool.parse(translation.support().embedTicketOpenedTitle().replace("%user%", user.getGlobalName())))
                .setDescription(colorTool.parse(translation.support().embedTicketOpenedBody()))
                .build();

        ticketChannel.sendMessageEmbeds(embed).queue();
        new TicketMessageHandler(api, config, translation).editMessage(id, ticketChannel, TicketStatus.OPEN);
        new TicketLogHandler(api, config, translation).editMessage(id, null, null, TicketStatus.OPEN);
        new TicketTable().updateTicketStatus(id, TicketStatus.OPEN);
        var child = api.getThreadChannelById(id);
        if (child != null) {
            child.getManager().setLocked(false).queue();
        }

        logger.info("Ticket {} opened by user: {}", id, user.getId());
    }

    public void pauseTicket(User user, ThreadChannel ticketChannel, String id) {
        ColorTool colorTool = new ColorTool();
        var embed = new EmbedBuilder()
                .setAuthor(user.getGlobalName(), null, user.getAvatarUrl())
                .setColor(0xf1c40f)
                .setTitle(colorTool.parse(translation.support().embedTicketPausedTitle().replace("%user%", user.getGlobalName())))
                .setDescription(colorTool.parse(translation.support().embedTicketPausedBody()))
                .build();

        ticketChannel.sendMessageEmbeds(embed).queue();
        new TicketMessageHandler(api, config, translation).editMessage(id, ticketChannel, TicketStatus.PAUSED);
        new TicketLogHandler(api, config, translation).editMessage(id, null, null, TicketStatus.PAUSED);
        new TicketTable().updateTicketStatus(id, TicketStatus.PAUSED);
        var child = api.getThreadChannelById(id);
        if (child != null) {
            child.getManager().setLocked(true).queue();
        }

        logger.info("Ticket {} paused by user: {}", id, user.getId());
    }

    public void suspendTicket(User user, ThreadChannel ticketChannel, String id) {
        ColorTool colorTool = new ColorTool();
        var embed = new EmbedBuilder()
                .setAuthor(user.getGlobalName(), null, user.getAvatarUrl())
                .setColor(0xE74D3C)
                .setTitle(colorTool.parse(translation.support().embedTicketSuspendedTitle().replace("%user%", user.getGlobalName())))
                .setDescription(colorTool.parse(translation.support().embedTicketSuspendedBody()))
                .build();

        ticketChannel.sendMessageEmbeds(embed).queue();
        new TicketMessageHandler(api, config, translation).editMessage(id, ticketChannel, TicketStatus.SUSPENDED);
        new TicketLogHandler(api, config, translation).editMessage(id, null, null, TicketStatus.SUSPENDED);
        new TicketTable().updateTicketStatus(id, TicketStatus.SUSPENDED);
        var child = api.getThreadChannelById(id);
        if (child != null) {
            child.getManager().setLocked(true).queue();
        }

        logger.info("Ticket {} suspended by user: {}", id, user.getId());
    }

    public void archiveTicket(User user, ThreadChannel ticketChannel, String id, String reason) {
        ColorTool colorTool = new ColorTool();
        var embed = new EmbedBuilder()
                .setAuthor(user.getGlobalName(), null, user.getAvatarUrl())
                .setColor(0xE74D3C)
                .setTitle(colorTool.parse(translation.support().embedTicketClosedTitle().replace("%user%", user.getGlobalName())))
                .setDescription(colorTool.parse(translation.support().embedTicketClosedBody().replace("%reason%", reason)))
                .build();

        ticketChannel.sendMessageEmbeds(embed).queue();
        new TicketLogHandler(api, config, translation).closeMessage(id, user, reason);
        String ticketCreator = new TicketTable().getTicketCreator(id);
        if (ticketCreator != null) {
            new TicketMessageHandler(api, config, translation).sendClosedMessage(ticketCreator, user.getId(), ticketChannel, reason);
        }
        new TicketTable().updateTicketStatus(id, TicketStatus.CLOSED);
        var child = api.getThreadChannelById(id);

        if (child != null) {
            child.getManager().setLocked(true).queue();
            child.getManager().setArchived(true).queue();
        }

        if (new TicketTable().getTicketType(id) == TicketType.APPLICATION) {
            new ApplicationTable().delete(id);
        }

        logger.info("Ticket {} archived by user: {}", id, user.getId());
    }

    public Collection<ItemComponent> settingsActionRow(TicketStatus status) {
        Collection<ItemComponent> rows = new ArrayList<>();

        Button open = Button.success("ticket_setting_open", translation.buttons().textSupportSettingsOpen()).withEmoji(Emoji.fromFormatted("🚪"));
        Button pause = Button.primary("ticket_setting_pause", translation.buttons().textSupportSettingsPause()).withEmoji(Emoji.fromFormatted("🌙"));
        Button suspend = Button.primary("ticket_setting_suspend", translation.buttons().textSupportSettingsSuspend()).withEmoji(Emoji.fromFormatted("🔒"));
        Button close = Button.danger("ticket_setting_close", translation.buttons().textSupportSettingsClose()).withEmoji(Emoji.fromFormatted("🪫"));

        switch (status) {
            case OPEN -> open = open.asDisabled();
            case PAUSED -> pause = pause.asDisabled();
            case SUSPENDED -> suspend = suspend.asDisabled();
            case CLOSED -> close = close.asDisabled();
        }

        rows.add(open);
        rows.add(pause);
        rows.add(suspend);
        rows.add(close);

        return rows;
    }
}

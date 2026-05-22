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

package dev.vxrp.bot.ticket;

import dev.vxrp.bot.ticket.data.TicketTypes;
import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.bot.ticket.handler.TicketLogHandler;
import dev.vxrp.bot.ticket.handler.TicketMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.TicketTable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class TicketManager {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(TicketManager.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public TicketManager(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public ThreadChannel createTicket(TicketType ticketType, TicketStatus ticketStatus, String ticketCreator, User ticketHandler, String modalId, List<ModalMapping> modalValue) {
        TicketTypes settings = querySettings(ticketType);
        if (settings == null) {
            logger.error("Can't create ticket, settings are not loaded");
            return null;
        }

        var channel = api.getTextChannelById(settings.parentChannel());
        if (channel == null) {
            logger.error("Could not find specified parent channel for '{}' ticket", settings.name());
            return null;
        }

        ThreadChannel child;
        try {
            child = channel.createThreadChannel(
                    settings.childRules().parentName().replace("%r%", String.valueOf(new TicketTable().retrieveSerial())),
                    true
            ).complete();
        } catch (Exception e) {
            logger.error("Could not create thread channel for ticket", e);
            return null;
        }

        TicketStatus effectiveStatus = settings.childRules().lockOnDefault() ? TicketStatus.PAUSED : ticketStatus;

        if (settings.childRules().lockOnDefault()) {
            child.getManager().setLocked(true).complete();
        }

        if (!"anonymous".equals(ticketCreator)) {
            User creatorUser = api.retrieveUserById(ticketCreator).complete();
            child.sendMessage(creatorUser.getAsMention()).complete().delete().queue();
        }
        for (String roleId : settings.roles()) {
            var role = api.getRoleById(roleId);
            if (role == null) {
                logger.error("Could not find role {} for ticket {}, does it exist?", roleId, settings.name());
                continue;
            }
            child.sendMessage(role.getAsMention()).complete().delete().queue();
        }

        String logMessage = new TicketLogHandler(api, config, translation).logMessage(ticketCreator, ticketHandler, child.getId(), effectiveStatus, child);
        if (logMessage == null) {
            logger.error("Could not carry out log message correctly");
            return null;
        }

        var message = new TicketMessageHandler(api, config, translation).sendMessage(ticketType, child, ticketCreator, modalId, modalValue);
        new TicketTable().addToDatabase(child.getId(), ticketType, effectiveStatus, LocalDate.now().toString(), ticketCreator, ticketHandler, logMessage, message.getId(), "CURRENTLY NOT IMPLEMENTED");
        logger.info("Created ticket: {} of type: {} by user: {} with current status {}", child.getId(), ticketType, ticketCreator, effectiveStatus);
        return child;
    }

    private TicketTypes querySettings(TicketType ticketType) {
        for (var option : config.ticket().types()) {
            if (!option.type().replace("support::", "").equals(ticketType.toString())) continue;
            return option;
        }
        logger.error("Could not correctly load ticket settings");
        return null;
    }
}

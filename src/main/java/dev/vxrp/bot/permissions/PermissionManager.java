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

package dev.vxrp.bot.permissions;

import dev.vxrp.bot.permissions.enums.PermissionType;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.bot.permissions.handler.PermissionMessageHandler;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PermissionManager {
    private final Logger logger = LoggerFactory.getLogger(PermissionManager.class);
    private final String insufficientValuesMessage = "Please supply the needed values for permission check";
    private final Config config;
    private final Translation translation;

    public PermissionManager(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public PermissionResult determinePermissions(User user, PermissionType permissionType, TicketType ticketType) {
        List<String> userRoles = queryUserRoles(user);
        List<String> permRoles = permissionRoles(permissionType, ticketType);

        if (userRoles == null || permRoles == null) {
            logger.warn("Permission denied due to failures in permission chain");
        }

        if ((permissionType == PermissionType.TICKET && ticketType == null)
                || (permissionType == PermissionType.TICKET_LOGS && ticketType == null)) {
            logger.error(insufficientValuesMessage);
            return new PermissionResult(false, null);
        }

        for (String role : userRoles) {
            if (permRoles.contains(role)) {
                logger.debug("Permission action for user: {}, for permission type: {}, permitted", user.getId(), permissionType);
                return new PermissionResult(true, null);
            }
        }

        MessageEmbed message = new PermissionMessageHandler(config, translation).getPermissionMessage(permissionType);
        logger.debug("Permission action for user: {}, for permission type: {}, denied", user.getId(), permissionType);
        return new PermissionResult(false, message);
    }

    public MessageEmbed checkStatus(StatusMessageType messageType, boolean... checks) {
        for (boolean check : checks) {
            if (!check) {
                return new PermissionMessageHandler(config, translation).getStatusMessage(messageType);
            }
        }
        return null;
    }

    private List<String> queryUserRoles(User user) {
        List<String> roleIdList = new java.util.ArrayList<>();
        var guild = user.getJDA().getGuildById(config.settings().guildId());
        if (guild == null) return null;

        var member = guild.retrieveMemberById(user.getId()).complete();
        if (member == null) {
            logger.error("Could not find any roles to determine permissions for User: {}", user.getId());
            return null;
        }

        var currentRoles = member.getRoles();
        for (var role : currentRoles) {
            roleIdList.add(role.getId());
        }
        return roleIdList;
    }

    private List<String> permissionRoles(PermissionType permissionType, TicketType ticketType) {
        switch (permissionType) {
            case TICKET: {
                for (var type : config.ticket().types()) {
                    if (!type.type().replace("support::", "").equals(ticketType.toString())) continue;

                    if (type.roles().isEmpty()) {
                        logger.error("Could not find any roles to determine permissions for ticket type: {}", ticketType);
                        return null;
                    }
                    return type.roles();
                }
                break;
            }
            case TICKET_LOGS: {
                for (var type : config.ticket().types()) {
                    if (!type.type().replace("support::", "").equals(ticketType.toString())) continue;

                    if (type.logPermissionRoles().isEmpty()) {
                        logger.error("Could not find any roles to determine permissions for log of ticket type: {}", ticketType);
                        return null;
                    }
                    return type.logPermissionRoles();
                }
                break;
            }
            case NOTICE_OF_DEPARTURES: {
                if (config.settings().noticeOfDeparture().rolesAccess().isEmpty()) {
                    logger.error("Could not find any roles to determine permissions for notice of departures");
                    return null;
                }
                return config.settings().noticeOfDeparture().rolesAccess();
            }
            case REGULARS:
            case STATUS_BOT:
            case APPLICATION:
                break;
        }
        return null;
    }

    public static class PermissionResult {
        private final boolean permitted;
        private final MessageEmbed embed;

        public PermissionResult(boolean permitted, MessageEmbed embed) {
            this.permitted = permitted;
            this.embed = embed;
        }

        public boolean isPermitted() {
            return permitted;
        }

        public MessageEmbed getEmbed() {
            return embed;
        }
    }
}

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

package dev.vxrp.bot.permissions.handler;

import dev.vxrp.bot.permissions.enums.PermissionMessageType;
import dev.vxrp.bot.permissions.enums.PermissionType;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PermissionMessageHandler {
    private final Config config;
    private final Translation translation;

    public PermissionMessageHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public MessageEmbed getPermissionMessage(PermissionType permissionType) {
        switch (permissionType) {
            case TICKET:
                return ticketMessage(PermissionMessageType.INSUFFICIENT_PERMISSION);
            case TICKET_LOGS:
                return ticketMessage(PermissionMessageType.INSUFFICIENT_PERMISSION);
            case NOTICE_OF_DEPARTURES:
                return noticeOfDepartureMessage(PermissionMessageType.INSUFFICIENT_PERMISSION);
            default:
                return null;
        }
    }

    public MessageEmbed getStatusMessage(StatusMessageType messageType) {
        ColorTool colorTool = new ColorTool();
        switch (messageType) {
            case PANEL:
                return new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(colorTool.parse(translation.permissions().embedCouldNotSendPanelTitle()))
                        .setDescription(colorTool.parse(translation.permissions().embedCouldNotSendPanelBody()))
                        .build();
            case MODAL:
                return new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(colorTool.parse(translation.permissions().embedCouldNotSendModalTitle()))
                        .setDescription(colorTool.parse(translation.permissions().embedCouldNotSendModalBody()))
                        .build();
            case COMMAND:
                return new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(colorTool.parse(translation.permissions().embedCouldNotSendCommandTitle()))
                        .setDescription(colorTool.parse(translation.permissions().embedCouldNotSendCommandBody()))
                        .build();
            case TEMPLATE:
                return new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(colorTool.parse(translation.permissions().embedCouldNotSendTemplateTitle()))
                        .setDescription(colorTool.parse(translation.permissions().embedCouldNotSendTemplateBody()))
                        .build();
            default:
                return null;
        }
    }

    private MessageEmbed ticketMessage(PermissionMessageType permissionMessageType) {
        ColorTool colorTool = new ColorTool();
        String description = colorTool.parse(
                translation.permissions().embedTicketDeniedBody()
                        .replace("%permission_message%", choosePermissionMessage(permissionMessageType).trim())
        );
        return new EmbedBuilder()
                .setTitle(colorTool.parse(translation.permissions().embedTicketDeniedTitle()))
                .setDescription(description)
                .build();
    }

    private MessageEmbed noticeOfDepartureMessage(PermissionMessageType permissionMessageType) {
        ColorTool colorTool = new ColorTool();
        String description = colorTool.parse(
                translation.permissions().embedNoticeOfDepartureDeniedBody()
                        .replace("%permission_message%", choosePermissionMessage(permissionMessageType).trim())
        );
        return new EmbedBuilder()
                .setTitle(colorTool.parse(translation.permissions().embedNoticeOfDepartureDeniedTitle()))
                .setDescription(description)
                .build();
    }

    private String choosePermissionMessage(PermissionMessageType permissionMessageType) {
        switch (permissionMessageType) {
            case INSUFFICIENT_PERMISSION:
                return translation.permissions().textInsufficientPermission();
            case DEACTIVATED_ACTION:
                return translation.permissions().textInteractionDisabled();
            default:
                return "";
        }
    }
}

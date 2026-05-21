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

import dev.vxrp.bot.modals.NoticeOfDepartureTemplateModals;
import dev.vxrp.bot.noticeofdeparture.enums.ActionId;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.PermissionType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class NoticeOfDepartureButtons {
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        if (buttonId.startsWith("notice_of_departure_file")) {
            event.replyModal(new NoticeOfDepartureTemplateModals(config, translation).generalModal()).queue();
        }

        if (buttonId.startsWith("notice_of_departure_decision_accept")) {
            if (permissionCheck(PermissionType.NOTICE_OF_DEPARTURES)) return;
            String[] splitId = buttonId.split(":");

            String userId = splitId[1];
            String endTime = splitId[2];

            event.replyModal(new NoticeOfDepartureTemplateModals(config, translation).reasonActionModal(ActionId.ACCEPTING, userId, endTime)).queue();
        }

        if (buttonId.startsWith("notice_of_departure_decision_dismiss")) {
            if (permissionCheck(PermissionType.NOTICE_OF_DEPARTURES)) return;
            String[] splitId = buttonId.split(":");

            String userId = splitId[1];
            String endTime = splitId[2];

            event.replyModal(new NoticeOfDepartureTemplateModals(config, translation).reasonActionModal(ActionId.DISMISSING, userId, endTime)).queue();
        }

        if (buttonId.startsWith("notice_of_departure_revoke")) {
            if (permissionCheck(PermissionType.NOTICE_OF_DEPARTURES)) return;
            String[] splitId = buttonId.split(":");

            String userId = splitId[1];
            String endTime = splitId[2];

            event.replyModal(new NoticeOfDepartureTemplateModals(config, translation).reasonActionModal(ActionId.REVOKING, userId, endTime)).queue();
        }
    }

    private boolean permissionCheck(PermissionType permissionType) {
        var result = new PermissionManager(config, translation).determinePermissions(event.getUser(), permissionType, null);
        if (result.getEmbed() != null) {
            event.replyEmbeds(result.getEmbed()).setEphemeral(true).queue();
        }
        return !result.isPermitted();
    }
}

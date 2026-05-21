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

package dev.vxrp.bot.modals;

import dev.vxrp.bot.noticeofdeparture.enums.ActionId;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class NoticeOfDepartureTemplateModals {
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureTemplateModals(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public Modal generalModal() {
        TextInput time = TextInput.create("notice_of_departure_general_time",
                        translation.noticeOfDeparture().modalTimeTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(10, 10)
                .setPlaceholder(translation.noticeOfDeparture().modalTimePlaceHolder()
                        .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting()))
                .build();

        TextInput explanation = TextInput.create("notice_of_departure_general_explanation",
                        translation.noticeOfDeparture().modalExplanationTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.noticeOfDeparture().modalExplanationPlaceHolder())
                .build();

        return Modal.create("notice_of_departure_general", translation.noticeOfDeparture().modalTitle())
                .addComponents(ActionRow.of(time), ActionRow.of(explanation))
                .build();
    }

    public Modal reasonActionModal(ActionId actionId, String userId, String endTime) {
        TextInput reason = TextInput.create("notice_of_departure_reason_action_reason",
                        translation.noticeOfDeparture().modalReasonActionReasonTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.noticeOfDeparture().modalReasonActionPlaceholder())
                .build();

        return Modal.create("notice_of_departure_reason_action_" + actionId + ":" + userId + ":" + endTime,
                        translation.noticeOfDeparture().modalReasonActionTitle())
                .addComponents(ActionRow.of(reason))
                .build();
    }
}

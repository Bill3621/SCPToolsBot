/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license with this file, you may obtain the
 * license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.modals;

import dev.vxrp.bot.noticeofdeparture.enums.ActionId;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

public class NoticeOfDepartureTemplateModals {
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureTemplateModals(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public Modal generalModal() {
        Label time = Label.of(translation.noticeOfDeparture().modalTimeTitle(), TextInput
                .create("notice_of_departure_general_time", TextInputStyle.SHORT).setRequired(true)
                .setRequiredRange(10, 10)
                .setPlaceholder(translation.noticeOfDeparture().modalTimePlaceHolder().replace(
                        "%formatter%", config.settings().noticeOfDeparture().dateFormatting()))
                .build());

        Label explanation = Label.of(translation.noticeOfDeparture().modalExplanationTitle(),
                TextInput
                        .create("notice_of_departure_general_explanation", TextInputStyle.PARAGRAPH)
                        .setRequired(true).setRequiredRange(4, 2000)
                        .setPlaceholder(
                                translation.noticeOfDeparture().modalExplanationPlaceHolder())
                        .build());

        return Modal
                .create("notice_of_departure_general", translation.noticeOfDeparture().modalTitle())
                .addComponents(time, explanation).build();
    }

    public Modal reasonActionModal(ActionId actionId, String userId, String endTime) {
        Label reason = Label.of(translation.noticeOfDeparture().modalReasonActionReasonTitle(),
                TextInput
                        .create("notice_of_departure_reason_action_reason",
                                TextInputStyle.PARAGRAPH)
                        .setRequired(true).setRequiredRange(4, 2000)
                        .setPlaceholder(
                                translation.noticeOfDeparture().modalReasonActionPlaceholder())
                        .build());

        return Modal
                .create("notice_of_departure_reason_action_" + actionId + ":" + userId + ":"
                        + endTime, translation.noticeOfDeparture().modalReasonActionTitle())
                .addComponents(reason).build();
    }
}

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

import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureDrafts;
import dev.vxrp.bot.noticeofdeparture.enums.ActionId;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NoticeOfDepartureTemplateModals {
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureTemplateModals(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public static final String START_DATE_ID = "notice_of_departure_general_start";
    public static final String RETURN_DATE_ID = "notice_of_departure_general_time";
    public static final String EXPLANATION_ID = "notice_of_departure_general_explanation";

    public Modal generalModal() {
        return generalModal(NoticeOfDepartureDrafts.Draft.EMPTY);
    }

    public Modal generalModal(NoticeOfDepartureDrafts.Draft draft) {
        var text = translation.noticeOfDeparture();
        String dateFormat = config.settings().noticeOfDeparture().dateFormatting();
        String exampleDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern(dateFormat));

        Label startDate = Label.of(text.modalStartDateTitle(), fill(text.modalStartDateDescription(), dateFormat, exampleDate),
                TextInput.create(START_DATE_ID, TextInputStyle.SHORT).setRequired(false).setMaxLength(20)
                        .setPlaceholder(fill(text.modalStartDatePlaceholder(), dateFormat, exampleDate))
                        .setValue(valueOrNull(draft.startDate())).build());

        Label returnDate = Label.of(text.modalTimeTitle(), fill(text.modalTimeDescription(), dateFormat, exampleDate),
                TextInput.create(RETURN_DATE_ID, TextInputStyle.SHORT).setRequired(true).setMaxLength(20)
                        .setPlaceholder(fill(text.modalTimePlaceHolder(), dateFormat, exampleDate))
                        .setValue(valueOrNull(draft.returnDate())).build());

        Label explanation = Label.of(text.modalExplanationTitle(),
                TextInput.create(EXPLANATION_ID, TextInputStyle.PARAGRAPH).setRequired(true).setRequiredRange(4, 2000)
                        .setPlaceholder(text.modalExplanationPlaceHolder())
                        .setValue(valueOrNull(draft.explanation())).build());

        return Modal.create("notice_of_departure_general", text.modalTitle())
                .addComponents(startDate, returnDate, explanation).build();
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

    private static String fill(String template, String dateFormat, String exampleDate) {
        return template.replace("%formatter%", dateFormat).replace("%example%", exampleDate);
    }

    private static String valueOrNull(String value) {
        return value.isBlank() ? null : value;
    }
}

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

import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

public class TicketTemplateModals {
    private final Translation translation;

    public TicketTemplateModals(Translation translation) {
        this.translation = translation;
    }

    public Modal supportGeneralModal() {
        Label subject = Label.of(translation.support().modalGeneralSubjectTitle(),
                TextInput.create("general_subject", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalGeneralSubjectPlaceholder())
                        .build());

        Label explanation = Label.of(translation.support().modalComplaintExplanationTitle(),
                TextInput.create("general_explanation", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 2000)
                        .setPlaceholder(translation.support().modalComplaintExplanationPlaceholder())
                        .build());

        return Modal.create("ticket_general", translation.support().modalGeneralTitle())
                .addComponents(subject, explanation)
                .build();
    }

    public Modal supportReportModal(String userId) {
        Label reason = Label.of(translation.support().modalReportReasonTitle(),
                TextInput.create("report_reason", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalReportReasonPlaceholder())
                        .build());

        Label proof = Label.of(translation.support().modalReportProofTitle(),
                TextInput.create("report_proof", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 2000)
                        .setPlaceholder(translation.support().modalReportProofPlaceholder())
                        .build());

        return Modal.create("ticket_report:" + userId, translation.support().modalReportTitle())
                .addComponents(reason, proof)
                .build();
    }

    public Modal supportErrorModal() {
        Label problem = Label.of(translation.support().modalErrorProblemTitle(),
                TextInput.create("error_problem", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalErrorProblemPlaceholder())
                        .build());

        Label times = Label.of(translation.support().modalErrorTimesTitle(),
                TextInput.create("error_times", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalErrorTimesPlaceholder())
                        .build());

        Label reproduce = Label.of(translation.support().modalErrorReproduceTitle(),
                TextInput.create("error_reproduce", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 1000)
                        .setPlaceholder(translation.support().modalErrorReproducePlaceholder())
                        .build());

        Label additional = Label.of(translation.support().modalErrorAdditionalTitle(),
                TextInput.create("error_additional", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 1000)
                        .setPlaceholder(translation.support().modalErrorAdditionalPlaceholder())
                        .build());

        return Modal.create("ticket_error", translation.support().modalErrorTitle())
                .addComponents(problem, times, reproduce, additional)
                .build();
    }

    public Modal supportUnbanModal() {
        Label steamId = Label.of(translation.support().modalUnbanSteamIdTitle(),
                TextInput.create("unban_steamID", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(17, 17)
                        .setPlaceholder(translation.support().modalUnbanSteamIdPlaceholder())
                        .build());

        Label reason = Label.of(translation.support().modalUnbanReasonTitle(),
                TextInput.create("unban_reason", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 2000)
                        .setPlaceholder(translation.support().modalUnbanReasonPlaceholder())
                        .build());

        return Modal.create("ticket_unban", translation.support().modalUnbanTitle())
                .addComponents(steamId, reason)
                .build();
    }

    public Modal supportComplaintModal(String userId, boolean anonymous) {
        Label subject = Label.of(translation.support().modalComplaintSubjectTitle(),
                TextInput.create("complaint_subject", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalComplaintSubjectPlaceholder())
                        .build());

        Label explanation = Label.of(translation.support().modalComplaintExplanationTitle(),
                TextInput.create("complaint_explanation", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 2000)
                        .setPlaceholder(translation.support().modalComplaintExplanationPlaceholder())
                        .build());

        return Modal.create("ticket_complaint:" + userId + ":" + anonymous, translation.support().modalComplaintTitle())
                .addComponents(subject, explanation)
                .build();
    }

    public Modal supportApplicationModal(String roleId) {
        Label name = Label.of(translation.support().modalApplicationNameTitle(),
                TextInput.create("application_name", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalApplicationNamePlaceholder())
                        .build());

        Label age = Label.of(translation.support().modalApplicationAgeTitle(),
                TextInput.create("application_age", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(2, 3)
                        .setPlaceholder(translation.support().modalApplicationAgePlaceholder())
                        .build());

        Label playtime = Label.of(translation.support().modalApplicationPlaytimeTitle(),
                TextInput.create("application_playtime", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setRequiredRange(4, 100)
                        .setPlaceholder(translation.support().modalApplicationPlaytimePlaceholder())
                        .build());

        Label reasonOfApplication = Label.of(translation.support().modalApplicationReasonsOfApplicationTitle(),
                TextInput.create("application_reason_of_application", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 1000)
                        .setPlaceholder(translation.support().modalApplicationReasonsOfApplicationPlaceholder())
                        .build());

        Label skills = Label.of(translation.support().modalApplicationSkillsTitle(),
                TextInput.create("application_skills", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setRequiredRange(4, 1000)
                        .setPlaceholder(translation.support().modalApplicationSkillsPlaceholder())
                        .build());

        return Modal.create("ticket_application:" + roleId, translation.support().modalApplicationTitle())
                .addComponents(name, age, playtime, reasonOfApplication, skills)
                .build();
    }
}

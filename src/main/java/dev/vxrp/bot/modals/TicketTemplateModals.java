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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class TicketTemplateModals {
    private final Translation translation;

    public TicketTemplateModals(Translation translation) {
        this.translation = translation;
    }

    public Modal supportGeneralModal() {
        TextInput subject = TextInput.create("general_subject",
                        translation.support().modalGeneralSubjectTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalGeneralSubjectPlaceholder())
                .build();

        TextInput explanation = TextInput.create("general_explanation",
                        translation.support().modalComplaintExplanationTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.support().modalComplaintExplanationPlaceholder())
                .build();

        return Modal.create("ticket_general", translation.support().modalGeneralTitle())
                .addComponents(ActionRow.of(subject), ActionRow.of(explanation))
                .build();
    }

    public Modal supportReportModal(String userId) {
        TextInput reason = TextInput.create("report_reason",
                        translation.support().modalReportReasonTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalReportReasonPlaceholder())
                .build();

        TextInput proof = TextInput.create("report_proof",
                        translation.support().modalReportProofTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.support().modalReportProofPlaceholder())
                .build();

        return Modal.create("ticket_report:" + userId, translation.support().modalReportTitle())
                .addComponents(ActionRow.of(reason), ActionRow.of(proof))
                .build();
    }

    public Modal supportErrorModal() {
        TextInput problem = TextInput.create("error_problem",
                        translation.support().modalErrorProblemTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalErrorProblemPlaceholder())
                .build();

        TextInput times = TextInput.create("error_times",
                        translation.support().modalErrorTimesTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalErrorTimesPlaceholder())
                .build();

        TextInput reproduce = TextInput.create("error_reproduce",
                        translation.support().modalErrorReproduceTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 1000)
                .setPlaceholder(translation.support().modalErrorReproducePlaceholder())
                .build();

        TextInput additional = TextInput.create("error_additional",
                        translation.support().modalErrorAdditionalTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 1000)
                .setPlaceholder(translation.support().modalErrorAdditionalPlaceholder())
                .build();

        return Modal.create("ticket_error", translation.support().modalErrorTitle())
                .addComponents(ActionRow.of(problem), ActionRow.of(times), ActionRow.of(reproduce), ActionRow.of(additional))
                .build();
    }

    public Modal supportUnbanModal() {
        TextInput steamId = TextInput.create("unban_steamID",
                        translation.support().modalUnbanSteamIdTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(17, 17)
                .setPlaceholder(translation.support().modalUnbanSteamIdPlaceholder())
                .build();

        TextInput reason = TextInput.create("unban_reason",
                        translation.support().modalUnbanReasonTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.support().modalUnbanReasonPlaceholder())
                .build();

        return Modal.create("ticket_unban", translation.support().modalUnbanTitle())
                .addComponents(ActionRow.of(steamId), ActionRow.of(reason))
                .build();
    }

    public Modal supportComplaintModal(String userId, boolean anonymous) {
        TextInput subject = TextInput.create("complaint_subject",
                        translation.support().modalComplaintSubjectTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalComplaintSubjectPlaceholder())
                .build();

        TextInput explanation = TextInput.create("complaint_explanation",
                        translation.support().modalComplaintExplanationTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 2000)
                .setPlaceholder(translation.support().modalComplaintExplanationPlaceholder())
                .build();

        return Modal.create("ticket_complaint:" + userId + ":" + anonymous, translation.support().modalComplaintTitle())
                .addComponents(ActionRow.of(subject), ActionRow.of(explanation))
                .build();
    }

    public Modal supportApplicationModal(String roleId) {
        TextInput name = TextInput.create("application_name",
                        translation.support().modalApplicationNameTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalApplicationNamePlaceholder())
                .build();

        TextInput age = TextInput.create("application_age",
                        translation.support().modalApplicationAgeTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(2, 3)
                .setPlaceholder(translation.support().modalApplicationAgePlaceholder())
                .build();

        TextInput playtime = TextInput.create("application_playtime",
                        translation.support().modalApplicationPlaytimeTitle(),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 100)
                .setPlaceholder(translation.support().modalApplicationPlaytimePlaceholder())
                .build();

        TextInput reasonOfApplication = TextInput.create("application_reason_of_application",
                        translation.support().modalApplicationReasonsOfApplicationTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 1000)
                .setPlaceholder(translation.support().modalApplicationReasonsOfApplicationPlaceholder())
                .build();

        TextInput skills = TextInput.create("application_skills",
                        translation.support().modalApplicationSkillsTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(4, 1000)
                .setPlaceholder(translation.support().modalApplicationSkillsPlaceholder())
                .build();

        return Modal.create("ticket_application:" + roleId, translation.support().modalApplicationTitle())
                .addComponents(ActionRow.of(name), ActionRow.of(age), ActionRow.of(playtime), ActionRow.of(reasonOfApplication), ActionRow.of(skills))
                .build();
    }
}

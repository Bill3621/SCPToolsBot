/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.ticket.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ticket(
    TicketSettings settings,
    @JsonProperty("application_types") List<ApplicationTypes> applicationTypes,
    @JsonProperty("application_questions") List<ApplicationQuestion> applicationQuestions,
    List<TicketTypes> types
) {
    public Ticket {
        if (applicationQuestions != null) {
            ApplicationQuestion.validate(applicationQuestions, "application_questions");
            applicationQuestions = List.copyOf(applicationQuestions);
        }
        if (applicationTypes == null || applicationTypes.isEmpty()) {
            throw new IllegalArgumentException("application_types must contain at least one position");
        }
        for (ApplicationTypes type : applicationTypes) {
            if (type.questions() == null && applicationQuestions == null) {
                throw new IllegalArgumentException("Configure questions for " + type.name() + " or application_questions");
            }
        }
        applicationTypes = List.copyOf(applicationTypes);
    }

    /** Uses position-specific questions when present, otherwise the shared questions. */
    public List<ApplicationQuestion> questionsForRole(String roleId) {
        return applicationTypes.stream()
                .filter(type -> type.roleID().equals(roleId))
                .map(type -> type.questions() == null ? applicationQuestions : type.questions())
                .findFirst().orElse(null);
    }
}

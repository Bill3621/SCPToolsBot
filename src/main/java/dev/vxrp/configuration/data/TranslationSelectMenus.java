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

package dev.vxrp.configuration.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationSelectMenus(
    @JsonProperty("TEXT_SUPPORT_NAME_GENERAL") String textSupportNameGeneral,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_GENERAL") String textsupportDescriptionGeneral,
    @JsonProperty("TEXT_SUPPORT_NAME_REPORT") String textSupportNameReport,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_REPORT") String textSupportDescriptionReport,
    @JsonProperty("TEXT_SUPPORT_NAME_ERROR") String textSupportNameError,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_ERROR") String textSupportDescriptionError,
    @JsonProperty("TEXT_SUPPORT_NAME_UNBAN") String textSupportNameUnban,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_UNBAN") String textSupportDescriptionUnban,
    @JsonProperty("TEXT_SUPPORT_NAME_COMPLAINT") String textSupportNameComplaint,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_COMPLAINT") String textSupportDescriptionComplaint,
    @JsonProperty("TEXT_SUPPORT_NAME_APPLICATION") String textSupportNameApplication,
    @JsonProperty("TEXT_SUPPORT_DESCRIPTION_APPLICATION") String textSupportDescriptionApplication
) {}

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

public record Translation(
    @JsonProperty("PERMISSIONS") TranslationPermissions permissions,
    @JsonProperty("SETTINGS") TranslationSettings settings,
    @JsonProperty("STATUS") TranslationStatus status,
    @JsonProperty("HELP") TranslationHelp help,
    @JsonProperty("PLAYER") TranslationPlayer player,
    @JsonProperty("SUPPORT") TranslationSupport support,
    @JsonProperty("APPLICATION") TranslationApplication application,
    @JsonProperty("VERIFY") TranslationVerify verify,
    @JsonProperty("NOTICE_OF_DEPARTURE") TranslationNoticeOfDeparture noticeOfDeparture,
    @JsonProperty("REGULARS") TranslationRegulars regulars,
    @JsonProperty("STATUS_BARS") TranslationStatusBars statusBars,
    @JsonProperty("BUTTONS") TranslationButtons buttons,
    @JsonProperty("SELECT_MENUS") TranslationSelectMenus selectMenus
) {}

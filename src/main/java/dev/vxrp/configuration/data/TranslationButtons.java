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

public record TranslationButtons(
    @JsonProperty("TEXT_SETTINGS_START") String textSettingsStart,
    @JsonProperty("TEXT_SETTINGS_CONFIGURE") String textSettingsConfigure,
    @JsonProperty("TEXT_SETTINGS_CURRENT") String textSettingsCurrent,
    @JsonProperty("TEXT_SETTINGS_INFORMATION") String textSettingsInformation,
    @JsonProperty("TEXT_SETTINGS_NEWS") String textSettingsNews,
    @JsonProperty("TEXT_RULES_PASTE") String textRulesPaste,
    @JsonProperty("TEXT_RULES_UPDATE") String textRulesUpdate,
    @JsonProperty("TEXT_PLAYER_STATS") String textPlayerStats,
    @JsonProperty("TEXT_PLAYER_MODERATION") String textPlayerModeration,
    @JsonProperty("TEXT_PLAYER_APPEAL") String textPlayerAppeal,
    @JsonProperty("TEXT_PLAYER_TICKET") String textPlayerTicket,
    @JsonProperty("TEXT_PLAYER_PANEL") String textPlayerPanel,
    @JsonProperty("TEXT_SUPPORT_ANONYMOUS_ACCEPT") String textSupportAnonymousAccept,
    @JsonProperty("TEXT_SUPPORT_ANONYMOUS_DENY") String textSupportAnonymousDeny,
    @JsonProperty("TEXT_SUPPORT_SETTINGS") String textSupportSettings,
    @JsonProperty("TEXT_SUPPORT_CLAIM") String ticketSupportClaim,
    @JsonProperty("TEXT_SUPPORT_CLOSE") String ticketSupportClose,
    @JsonProperty("TEXT_SUPPORT_SETTINGS_OPEN") String textSupportSettingsOpen,
    @JsonProperty("TEXT_SUPPORT_SETTINGS_PAUSE") String textSupportSettingsPause,
    @JsonProperty("TEXT_SUPPORT_SETTINGS_SUSPEND") String textSupportSettingsSuspend,
    @JsonProperty("TEXT_SUPPORT_SETTINGS_CLOSE") String textSupportSettingsClose,
    @JsonProperty("TEXT_SUPPORT_LOG_CLAIM") String textSupportLogClaim,
    @JsonProperty("TEXT_SUPPORT_LOG_OPEN") String textSupportLogOpen,
    @JsonProperty("TEXT_SUPPORT_LOG_PAUSE") String textSupportLogPause,
    @JsonProperty("TEXT_SUPPORT_LOG_SUSPEND") String textSupportLogSuspend,
    @JsonProperty("TEXT_SUPPORT_LOG_CLOSE") String textSupportLogClose,
    @JsonProperty("TEXT_APPLICATION_ACTIVATION_ADD") String textApplicationActivationAdd,
    @JsonProperty("TEXT_APPLICATION_ACTIVATION_REMOVE") String textApplicationActivationRemove,
    @JsonProperty("TEXT_APPLICATION_ACTIVATION_COMPLETE_SETUP") String textApplicationActivationCompleteSetup,
    @JsonProperty("TEXT_APPLICATION_DEACTIVATE") String textApplicationDeactivate,
    @JsonProperty("TEXT_APPLICATION_OPEN_TICKET") String textApplicationOpenTickets,
    @JsonProperty("TEXT_VERIFY_VERIFY") String textVerifyVerify,
    @JsonProperty("TEXT_VERIFY_SHOW_DATA") String textVerifyShowData,
    @JsonProperty("TEXT_VERIFY_DELETE") String textVerifyDelete,
    @JsonProperty("TEXT_NOTICE_OF_DEPARTURE_FILE") String textNoticeOfDepartureFile,
    @JsonProperty("TEXT_NOTICE_OF_DEPARTURE_ACCEPT") String textNoticeOfDepartureAccept,
    @JsonProperty("TEXT_NOTICE_OF_DEPARTURE_DISMISSED") String textNoticeOfDepartureDismissed,
    @JsonProperty("TEXT_NOTICE_OF_DEPARTURE_REVOKED") String textNoticeOfDepartureRevoked,
    @JsonProperty("TEXT_NOTICE_OF_DEPARTURE_DELETE") String textNoticeOfDepartureDelete,
    @JsonProperty("TEXT_REGULAR_OPEN_SETTINGS") String textRegularOpenSettings,
    @JsonProperty("TEXT_REGULAR_SYNC") String textRegularSync,
    @JsonProperty("TEXT_REGULAR_SYNC_DEACTIVATE") String textRegularSyncDeactivate,
    @JsonProperty("TEXT_REGULAR_SYNC_REACTIVATE") String textRegularSyncReactivate,
    @JsonProperty("TEXT_REGULAR_SYNC_REMOVE") String textRegularSyncRemove
) {}

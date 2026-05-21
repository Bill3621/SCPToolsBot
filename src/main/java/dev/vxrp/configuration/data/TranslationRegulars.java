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

public record TranslationRegulars(
    @JsonProperty("EMBED_TEMPLATE_TITLE") String embedTemplateTitle,
    @JsonProperty("EMBED_TEMPLATE_BODY") String embedTemplateBody,
    @JsonProperty("EMBED_TEMPLATE_GROUP_BODY") String embedTemplateGroupBody,
    @JsonProperty("EMBED_TEMPLATE_ROLE_BODY") String embedTemplateRoleBody,
    @JsonProperty("TEXT_TEMPLATE_ROLE_TIMEFRAME") String textTemplateRoleTimeframe,
    @JsonProperty("TEXT_TEMPLATE_ROLE_XP") String textTemplateRoleXp,
    @JsonProperty("EMBED_SETTINGS_TITLE") String embedSettingsTitle,
    @JsonProperty("EMBED_SETTINGS_BODY") String embedSettingsBody,
    @JsonProperty("EMBED_SETTINGS_VIEW_TITLE") String embedSettingsViewTitle,
    @JsonProperty("EMBED_SETTINGS_VIEW_BODY") String embedSettingsViewBody,
    @JsonProperty("EMBED_SETTINGS_FIELD_GROUP_NAME") String embedSettingsFieldGroupName,
    @JsonProperty("EMBED_SETTINGS_FIELD_ROLE_NAME") String embedSettingsFieldRoleName,
    @JsonProperty("EMBED_SETTINGS_FIELD_PLAYTIME_NAME") String embedSettingsFieldPlaytimeName,
    @JsonProperty("EMBED_SETTINGS_FIELD_XP_NAME") String embedSettingsFieldXp,
    @JsonProperty("EMBED_SETTINGS_FIELD_LAST_CHECKED") String embedSettingsFieldLastChecked,
    @JsonProperty("EMBED_SYNC_GROUP_SELECT_TITLE") String embedSyncGroupSelectTitle,
    @JsonProperty("EMBED_SYNC_GROUP_SELECT_BODY") String embedSyncGroupSelectBody,
    @JsonProperty("EMBED_NOT_VERIFIED_TITLE") String embedNotVerifiedTitle,
    @JsonProperty("EMBED_NOT_VERIFIED_BODY") String embedNotVerifiedBody,
    @JsonProperty("EMBED_SYNC_SENT_TITLE") String embedSyncSentTitle,
    @JsonProperty("EMBED_SYNC_SENT_BODY") String embedSyncSentBody,
    @JsonProperty("EMBED_SYNC_DEACTIVATED_TITLE") String embedSyncDeactivatedTitle,
    @JsonProperty("EMBED_SYNC_DEACTIVATED_BODY") String embedSyncDeactivatedBody,
    @JsonProperty("EMBED_SYNC_REACTIVATED_TITLE") String embedSyncReactivatedTitle,
    @JsonProperty("EMBED_SYNC_REACTIVATED_BODY") String embedSyncReactivatedBody,
    @JsonProperty("EMBED_SYNC_REMOVED_CONFIRM_TITLE") String embedSyncRemovedConfirmTitle,
    @JsonProperty("EMBED_SYNC_REMOVED_CONFIRM_BODY") String embedSyncRemovedConfirmBody,
    @JsonProperty("EMBED_SYNC_REMOVED_MESSAGE_TITLE") String embedSyncRemovedTitle,
    @JsonProperty("EMBED_SYNC_REMOVED_MESSAGE_BODY") String embedSyncRemovedBody
) {}

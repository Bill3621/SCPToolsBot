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

public record TranslationStatus(
    @JsonProperty("ACTIVITY_OFFLINE") String activityOffline,
    @JsonProperty("ACTIVITY_MAINTENANCE") String activityMaintenance,
    @JsonProperty("EMBED_ESTABLISHED_TITLE") String embedEstablishedTitle,
    @JsonProperty("EMBED_ESTABLISHED_BODY") String embedEstablishedBody,
    @JsonProperty("EMBED_ESTABLISHED_REASON_FIELD_NAME") String embedEstablishedReasonFieldName,
    @JsonProperty("EMBED_ESTABLISHED_REASON_FIELD_VALUE") String embedEstablishedReasonFieldValue,
    @JsonProperty("EMBED_ESTABLISHED_RESPONSE_FIELD_NAME") String embedEstablishedResponseFieldName,
    @JsonProperty("EMBED_ESTABLISHED_RESPONSE_FIELD_VALUE") String embedEstablishedResponseFieldValue,
    @JsonProperty("EMBED_LOST_TITLE") String embedLostTitle,
    @JsonProperty("EMBED_LOST_BODY") String embedLostBody,
    @JsonProperty("EMBED_LOST_REASON_FIELD_NAME") String embedLostReasonFieldName,
    @JsonProperty("EMBED_LOST_REASON_FIELD_VALUE") String embedLostReasonFieldValue,
    @JsonProperty("EMBED_LOST_RESPONSE_FIELD_NAME") String embedLostResponseFieldName,
    @JsonProperty("EMBED_LOST_RESPONSE_FIELD_VALUE") String embedLostResponseFieldValue,
    @JsonProperty("EMBED_ONLINE_TITLE") String embedOnlineTitle,
    @JsonProperty("EMBED_ONLINE_BODY") String embedOnlineBody,
    @JsonProperty("EMBED_ONLINE_REASON_FIELD_NAME") String embedOnlineReasonFieldName,
    @JsonProperty("EMBED_ONLINE_REASON_FIELD_VALUE") String embedOnlineReasonFieldValue,
    @JsonProperty("EMBED_ONLINE_RESPONSE_FIELD_NAME") String embedOnlineResponseFieldName,
    @JsonProperty("EMBED_ONLINE_RESPONSE_FIELD_VALUE") String embedOnlineResponseFieldValue,
    @JsonProperty("EMBED_OFFLINE_TITLE") String embedOfflineTitle,
    @JsonProperty("EMBED_OFFLINE_BODY") String embedOfflineBody,
    @JsonProperty("EMBED_OFFLINE_REASON_FIELD_NAME") String embedOfflineReasonFieldName,
    @JsonProperty("EMBED_OFFLINE_REASON_FIELD_VALUE") String embedOfflineReasonFieldValue,
    @JsonProperty("EMBED_OFFLINE_RESPONSE_FIELD_NAME") String embedOfflineResponseFieldName,
    @JsonProperty("EMBED_OFFLINE_RESPONSE_FIELD_VALUE") String embedOfflineResponseFieldValue,
    @JsonProperty("EMBED_MAINTENANCE_ON_TITLE") String embedMaintenanceOnTitle,
    @JsonProperty("EMBED_MAINTENANCE_ON_BODY") String embedMaintenanceOnBody,
    @JsonProperty("EMBED_MAINTENANCE_ON_REASON_FIELD_NAME") String embedMaintenanceOnReasonFieldName,
    @JsonProperty("EMBED_MAINTENANCE_ON_REASON_FIELD_VALUE") String embedMaintenanceOnReasonFieldValue,
    @JsonProperty("EMBED_MAINTENANCE_ON_RESPONSE_FIELD_NAME") String embedMaintenanceOnResponseFieldName,
    @JsonProperty("EMBED_MAINTENANCE_ON_RESPONSE_FIELD_VALUE") String embedMaintenanceOnResponseFieldValue,
    @JsonProperty("EMBED_MAINTENANCE_OFF_TITLE") String embedMaintenanceOffTitle,
    @JsonProperty("EMBED_MAINTENANCE_OFF_BODY") String embedMaintenanceOffBody,
    @JsonProperty("EMBED_MAINTENANCE_OFF_REASON_FIELD_NAME") String embedMaintenanceOffReasonFieldName,
    @JsonProperty("EMBED_MAINTENANCE_OFF_REASON_FIELD_VALUE") String embedMaintenanceOffReasonFieldValue,
    @JsonProperty("EMBED_MAINTENANCE_OFF_RESPONSE_FIELD_NAME") String embedMaintenanceOffResponseFieldName,
    @JsonProperty("EMBED_MAINTENANCE_OFF_RESPONSE_FIELD_VALUE") String embedMaintenanceOffResponseFieldValue,
    @JsonProperty("EMBED_PLAYERLIST_TITLE") String embedPlayerlistTitle,
    @JsonProperty("EMBED_PLAYERLIST_BODY") String embedPlayerlistBody,
    @JsonProperty("EMBED_PLAYERLIST_EMPTY") String embedPlayerlistEmpty,
    @JsonProperty("EMBED_PLAYERLIST_COULDNT_FETCH") String embedPlayerlistCouldntFetch,
    @JsonProperty("EMBED_PLAYERLIST_PLAYER") String embedPlayerlistPlayer,
    @JsonProperty("EMBED_STATUS_ACTIVATED_TITLE") String embedStatusActivatedTitle,
    @JsonProperty("EMBED_STATUS_ACTIVATED_BODY") String embedStatusActivatedBody,
    @JsonProperty("EMBED_STATUS_DEACTIVATED_TITLE") String embedStatusDeactivatedTitle,
    @JsonProperty("EMBED_STATUS_DEACTIVATED_BODY") String embedStatusDeactivatedBody
) {}

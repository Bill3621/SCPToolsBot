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

public record TranslationVerify(
    @JsonProperty("EMBED_TEMPLATE_TITLE") String embedTemplateTitle,
    @JsonProperty("EMBED_TEMPLATE_BODY") String embedTemplateBody,
    @JsonProperty("EMBED_DATA_TITLE") String embedDataTitle,
    @JsonProperty("EMBED_DATA_BODY") String embedDataBody,
    @JsonProperty("EMBED_DATA_FIELD_VERIFIED_TITLE") String embedDataFieldVerifiedTitle,
    @JsonProperty("EMBED_DATA_FIELD_STEAMID_TITLE") String embedDataFieldSteamIdTitle,
    @JsonProperty("EMBED_DATA_FIELD_TIMESTAMP_TITLE") String embedDataFieldTimestampTitle,
    @JsonProperty("EMBED_DATA_FIELD_DELETE_VALUE") String embedDataFieldDeleteValue,
    @JsonProperty("EMBED_NO_DATA_TITLE") String embedNoDataTitle,
    @JsonProperty("EMBED_NO_DATA_BODY") String embedNoDataBody,
    @JsonProperty("EMBED_LOG_VERIFIED_TITLE") String embedLogVerifiedTitle,
    @JsonProperty("EMBED_LOG_VERIFIED_BODY") String embedLogVerifiedBody,
    @JsonProperty("EMBED_LOG_DELETED_TITLE") String embedLogDeletedTitle,
    @JsonProperty("EMBED_LOG_DELETED_BODY") String embedLogDeletedBody,
    @JsonProperty("EMBED_DELETION_SENT_TITLE") String embedDeletionSentTitle,
    @JsonProperty("EMBED_DELETION_SENT_BODY") String embedDeletionSentBody
) {}

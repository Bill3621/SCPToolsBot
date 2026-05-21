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

public record TranslationPlayer(
    @JsonProperty("EMBED_STATISTICS_BODY") String embedStatisticsBody,
    @JsonProperty("EMBED_STEAMID_FIELD_NAME") String embedStatisticsSteamIdFieldName,
    @JsonProperty("EMBED_STEAMID_FIELD_VALUE") String embedStatisticsSteamIdFieldValue,
    @JsonProperty("EMBED_PLAYTIME_FIELD_NAME") String embedStatisticsPlaytimeFiledName,
    @JsonProperty("EMBED_PLAYTIME_FIELD_VALUE") String embedStatisticsPlaytimeFieldValue,
    @JsonProperty("EMBED_BANNED_FIELD_NAME") String embedStatisticsBannedFieldName,
    @JsonProperty("EMBED_BANNED_FIELD_VALUE") String embedStatisticsBannedFieldValue,
    @JsonProperty("EMBED_FILLER_ONE_FIELD_NAME") String embedStatisticsFillerOneFiledName,
    @JsonProperty("EMBED_FILLER_ONE_FIELD_VALUE") String embedStatisticsFillerOneFiledValue,
    @JsonProperty("EMBED_WARNS_FIELD_NAME") String embedStatisticsWarnsFieldName,
    @JsonProperty("EMBED_WARNS_FIELD_VALUE") String embedStatisticsWarnsFieldValue,
    @JsonProperty("EMBED_MUTES_FIELD_NAME") String embedStatisticsMutesFieldName,
    @JsonProperty("EMBED_MUTES_FIELD_VALUE") String embedStatisticsMutesFieldValue,
    @JsonProperty("EMBED_WATCHLIST_FIELD_NAME") String embedStatisticsWatchlistFieldName,
    @JsonProperty("EMBED_WATCHLIST_FIELD_VALUE") String embedStatisticsWatchlistFieldValue,
    @JsonProperty("EMBED_FILLER_TWO_FIELD_NAME") String embedStatisticsFillerTwoFiledName,
    @JsonProperty("EMBED_FILLER_TWO_FIELD_VALUE") String embedStatisticsFillerTwoFiledValue
) {}

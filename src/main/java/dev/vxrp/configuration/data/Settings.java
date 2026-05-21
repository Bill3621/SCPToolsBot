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

import java.util.List;

public record Settings(
    String token,
    @JsonProperty("client_secret") String clientSecret,
    @JsonProperty("guild_id") String guildId,
    @JsonProperty("load_translation") String loadTranslation,
    boolean debug,
    @JsonProperty("advanced_debug") boolean advancedDebug,
    ConfigUpdates updates,
    @JsonProperty("activity_type") String activityType,
    @JsonProperty("activity_content") String activityContent,
    ConfigDatabase database,
    ConfigWebserver webserver,
    ConfigCedmod cedmod,
    @JsonProperty("XP") ConfigXP xp,
    ConfigVerify verify,
    @JsonProperty("notice_of_departure") ConfigNoticeOfDeparture noticeOfDeparture,
    ConfigRegulars regulars
) {}

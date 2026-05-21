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

public record ConfigNoticeOfDeparture(
    boolean active,
    @JsonProperty("date_formatting") String dateFormatting,
    @JsonProperty("decision_channel_id") String decisionChannel,
    @JsonProperty("notice_channel_id") String noticeChannel,
    @JsonProperty("roles_access_notices") List<String> rolesAccess,
    @JsonProperty("check_type") String checkUnit,
    @JsonProperty("check_rate") int checkRate
) {}

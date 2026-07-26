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

package dev.vxrp.bot.status.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Status(
    boolean active,
    String api,
    @JsonProperty("account_id") String accountId,
    @JsonProperty("post_server_status") boolean postServerStatus,
    @JsonProperty("post_channel") String postChannel,
    @JsonProperty("page_url") String pageUrl,
    @JsonProperty("check_playerlist") boolean checkPlayerlist,
    @JsonProperty("global_playerlist") PlayerList globalPlayerlist,
    @JsonProperty("check_rate") int checkRate,
    @JsonProperty("retry_to_fetch_data") int retryToFetchData,
    @JsonProperty("suspect_rate_limit_until") int suspectRateLimitUntil,
    @JsonProperty("idle_after") int idleAfter,
    @JsonProperty("idle_check_rate") int idleCheckRate,
    List<Instance> instances
) {}

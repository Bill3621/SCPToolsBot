/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordConnection(
    String id,
    String name,
    String type,
    @JsonProperty("friend_sync") boolean friendSync,
    @JsonProperty("metadata_visibility") int metadataVisibility,
    @JsonProperty("show_activity") boolean showActivity,
    @JsonProperty("two_way_link") boolean twoWayLink,
    boolean verified,
    int visibility
) {}

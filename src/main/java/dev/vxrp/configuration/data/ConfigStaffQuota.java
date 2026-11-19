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

public record ConfigStaffQuota(
    boolean active,
    @JsonProperty("report_channel_id") String reportChannel,
    @JsonProperty("dm_missed") boolean dmMissed,
    @JsonProperty("roles_view_others") List<String> rolesViewOthers,
    List<Role> roles
) {
    public static final ConfigStaffQuota DISABLED = new ConfigStaffQuota(false, "", false, List.of(), List.of());

    public ConfigStaffQuota {
        if (reportChannel == null) reportChannel = "";
        rolesViewOthers = rolesViewOthers == null ? List.of() : List.copyOf(rolesViewOthers);
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public record Role(@JsonProperty("role_id") String roleId, double hours) {}
}

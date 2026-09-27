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

package dev.vxrp.bot.ticket.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ApplicationVotingSettings(
    int quorum,
    @JsonProperty("quorum_percent") Integer quorumPercent,
    @JsonProperty("senior_closer_roles") List<String> seniorCloserRoles
) {
    public ApplicationVotingSettings {
        quorum = quorum < 1 ? 3 : quorum;
        if (quorumPercent != null && (quorumPercent < 1 || quorumPercent > 100)) {
            throw new IllegalArgumentException("application_voting.quorum_percent must be between 1 and 100");
        }
        seniorCloserRoles = seniorCloserRoles == null ? List.of() : List.copyOf(seniorCloserRoles);
    }

    public ApplicationVotingSettings() {
        this(3, null, List.of());
    }

    /** Resolve the quorum once per application so later staff changes do not shift its threshold. */
    public int requiredVotes(int eligibleVoters) {
        return quorumPercent == null ? quorum : Math.max(1, (int) (((long) eligibleVoters * quorumPercent + 99) / 100));
    }
}

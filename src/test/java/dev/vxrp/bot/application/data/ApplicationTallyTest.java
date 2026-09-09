/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTallyTest {
    @Test
    void abstentionsDoNotCountTowardQuorum() {
        var tally = new ApplicationTally(1, 1, 12, 3);

        assertEquals(2, tally.counted());
        assertFalse(tally.quorumReached());
        assertNull(tally.recommendation());
    }

    @Test
    void majorityBecomesRecommendationAfterQuorum() {
        var tally = new ApplicationTally(2, 1, 4, 3);

        assertTrue(tally.quorumReached());
        assertEquals(Boolean.TRUE, tally.recommendation());
    }

    @Test
    void tieHasNoRecommendation() {
        var tally = new ApplicationTally(2, 2, 0, 3);

        assertTrue(tally.quorumReached());
        assertNull(tally.recommendation());
    }
}

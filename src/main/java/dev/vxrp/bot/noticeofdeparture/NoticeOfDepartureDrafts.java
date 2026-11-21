/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.noticeofdeparture;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class NoticeOfDepartureDrafts {
    private static final Duration LIFETIME = Duration.ofMinutes(15);
    private static final Map<String, Stored> DRAFTS = new ConcurrentHashMap<>();

    private NoticeOfDepartureDrafts() {}

    public record Draft(String startDate, String returnDate, String explanation) {
        public static final Draft EMPTY = new Draft("", "", "");
    }

    private record Stored(Draft draft, Instant expiresAt) {}

    public static void save(String userId, Draft draft) {
        Instant now = Instant.now();
        DRAFTS.values().removeIf(stored -> stored.expiresAt().isBefore(now));
        DRAFTS.put(userId, new Stored(draft, now.plus(LIFETIME)));
    }

    public static Optional<Draft> take(String userId) {
        return Optional.ofNullable(DRAFTS.remove(userId))
                .filter(stored -> stored.expiresAt().isAfter(Instant.now()))
                .map(Stored::draft);
    }
}

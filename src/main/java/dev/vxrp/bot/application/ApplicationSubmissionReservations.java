package dev.vxrp.bot.application;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/** Counts submissions awaiting their durable PROVISIONING record. */
final class ApplicationSubmissionReservations {
    private final Map<String, Integer> pending = new HashMap<>();

    synchronized boolean reserve(String roleId, Integer limit, LongSupplier openCount) {
        int count = pending.getOrDefault(roleId, 0);
        if (limit != null && openCount.getAsLong() + count >= limit) {
            return false;
        }
        pending.put(roleId, count + 1);
        return true;
    }

    synchronized void release(String roleId) {
        pending.computeIfPresent(roleId, (id, count) -> count == 1 ? null : count - 1);
    }
}

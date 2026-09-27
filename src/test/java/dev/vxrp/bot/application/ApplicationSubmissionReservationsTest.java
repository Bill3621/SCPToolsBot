package dev.vxrp.bot.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationSubmissionReservationsTest {
    @Test
    void pendingSubmissionOccupiesLastSlotBeforeDatabaseInsert() {
        var reservations = new ApplicationSubmissionReservations();
        assertTrue(reservations.reserve("role", 2, () -> 1));
        assertFalse(reservations.reserve("role", 2, () -> 1));
        assertTrue(reservations.reserve("other-role", 2, () -> 1));
        reservations.release("role");
        assertFalse(reservations.reserve("role", 2, () -> 2));
    }

    @Test
    void failedSubmissionReleasesCapacity() {
        var reservations = new ApplicationSubmissionReservations();
        assertTrue(reservations.reserve("role", 1, () -> 0));
        reservations.release("role");
        assertTrue(reservations.reserve("role", 1, () -> 0));
        assertFalse(reservations.reserve("role", 1, () -> 0));
    }

    @Test
    void unlimitedRolesAcceptMultiplePendingSubmissions() {
        var reservations = new ApplicationSubmissionReservations();
        assertTrue(reservations.reserve("role", null, () -> 100));
        assertTrue(reservations.reserve("role", null, () -> 100));
        reservations.release("role");
        reservations.release("role");
        assertTrue(reservations.reserve("role", 1, () -> 0));
    }
}

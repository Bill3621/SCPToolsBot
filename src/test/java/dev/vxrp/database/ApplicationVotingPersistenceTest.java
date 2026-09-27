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

package dev.vxrp.database;

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationVoteAction;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.database.migrations.ApplicationMigrationRunner;
import dev.vxrp.database.tables.database.ApplicationAnswerTable;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationVoteTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationVotingPersistenceTest {
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE tickets (id TEXT PRIMARY KEY, status TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE applications (id TEXT PRIMARY KEY, roleId TEXT NOT NULL, "
                    + "state BOOLEAN NOT NULL, result BOOLEAN NOT NULL, issuer TEXT NOT NULL, handler TEXT)");
        }
        ApplicationMigrationRunner.run(connection);
        setDatabaseConnection(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        setDatabaseConnection(null);
        connection.close();
    }

    @Test
    void answersVotesAndDecisionRemainDurable() {
        var applications = new ApplicationTable();
        var votes = new ApplicationVoteTable();
        var answers = List.of(
                new ApplicationAnswer("application_name", "Ada"),
                new ApplicationAnswer("application_age", "24"));

        applications.createProvisioning("application-1", "role-1", "applicant-1", null, 3, answers);
        applications.markOpen("application-1", "review-channel", "review-message");
        assertTrue(applications.setReviewThreadId("application-1", "review-message", "discussion-thread"));
        assertFalse(applications.setReviewThreadId("application-1", "other-message", "other-thread"));

        votes.set("application-1", "staff-1", ApplicationVoteChoice.APPROVE);
        votes.set("application-1", "staff-1", ApplicationVoteChoice.DENY);
        votes.set("application-1", "staff-2", ApplicationVoteChoice.ABSTAIN);

        var tally = votes.tally("application-1", 3);
        assertEquals(0, tally.approve());
        assertEquals(1, tally.deny());
        assertEquals(1, tally.abstain());
        assertFalse(tally.quorumReached());
        assertEquals(List.of(ApplicationVoteAction.SET, ApplicationVoteAction.CHANGE,
                        ApplicationVoteAction.SET),
                votes.history("application-1").stream().map(event -> event.action()).toList());
        assertEquals(answers.size(), new ApplicationAnswerTable().list("application-1").size());

        assertTrue(applications.closeIfOpen("application-1", ApplicationDecision.DENIED, "senior-1", "Not ready"));
        assertFalse(applications.closeIfOpen("application-1", ApplicationDecision.APPROVED, "senior-2", "Race"));

        var closed = applications.retrieveById("application-1").orElseThrow();
        assertEquals(ApplicationLifecycle.CLOSED, closed.lifecycle());
        assertEquals(ApplicationDecision.DENIED, closed.decision());
        assertEquals("senior-1", closed.closedBy());
        assertEquals("Not ready", closed.closeReason());
        assertEquals("discussion-thread", closed.reviewThreadId());
    }

    @Test
    void openCountTracksApplicationsUntilTheyClose() {
        var applications = new ApplicationTable();
        applications.createProvisioning("candidate-1", "role-1", "applicant-1", null, 3, List.of());
        applications.createProvisioning("candidate-2", "role-2", "applicant-2", null, 3, List.of());
        assertEquals(1, applications.retrieveOpenCount("role-1"));

        applications.markOpen("candidate-1", "review-channel", "review-message");
        assertEquals(1, applications.retrieveOpenCount("role-1"));
        applications.closeIfOpen("candidate-1", ApplicationDecision.DENIED, "senior-1", "Not ready");
        assertEquals(0, applications.retrieveOpenCount("role-1"));
        assertEquals(1, applications.retrieveOpenCount("role-2"));
    }

    @Test
    void approvedCountIgnoresPendingAndDeniedApplications() {
        var applications = new ApplicationTable();
        applications.createProvisioning("pending", "role-1", "applicant-1", null, 3, List.of());
        applications.createProvisioning("denied", "role-1", "applicant-2", null, 3, List.of());
        applications.markOpen("pending", "review-channel", "review-message-1");
        applications.markOpen("denied", "review-channel", "review-message-2");
        assertEquals(0, applications.retrieveApprovedCount("role-1"));

        applications.closeIfOpen("denied", ApplicationDecision.DENIED, "senior-1", "Not ready");
        assertEquals(0, applications.retrieveApprovedCount("role-1"));
        applications.closeIfOpen("pending", ApplicationDecision.APPROVED, "senior-1", "Ready");
        assertEquals(1, applications.retrieveApprovedCount("role-1"));
        assertEquals(0, applications.retrieveApprovedCount("role-2"));
    }

    @Test
    void approvalCannotExceedAcceptedCandidateLimit() {
        var applications = new ApplicationTable();
        applications.createProvisioning("first", "role-1", "applicant-1", null, 3, List.of());
        applications.createProvisioning("second", "role-1", "applicant-2", null, 3, List.of());
        applications.markOpen("first", "review-channel", "review-message-1");
        applications.markOpen("second", "review-channel", "review-message-2");

        var first = applications.retrieveById("first").orElseThrow();
        var second = applications.retrieveById("second").orElseThrow();
        assertEquals(ApplicationTable.CloseOutcome.CLOSED, DatabaseManager.transaction(connection ->
                applications.closeWithinLimit(connection, first, 1, ApplicationDecision.APPROVED, "senior-1", "Ready")));
        assertEquals(ApplicationTable.CloseOutcome.LIMIT_REACHED, DatabaseManager.transaction(connection ->
                applications.closeWithinLimit(connection, second, 1, ApplicationDecision.APPROVED, "senior-1", "Ready")));
        assertEquals(ApplicationTable.CloseOutcome.ALREADY_CLOSED, DatabaseManager.transaction(connection ->
                applications.closeWithinLimit(connection, first, 1, ApplicationDecision.APPROVED, "senior-1", "Again")));
        assertEquals(ApplicationLifecycle.OPEN, applications.retrieveById("second").orElseThrow().lifecycle());
        assertEquals(ApplicationTable.CloseOutcome.CLOSED, DatabaseManager.transaction(connection ->
                applications.closeWithinLimit(connection, second, 1, ApplicationDecision.DENIED, "senior-1", "Not selected")));
        assertEquals(1, applications.retrieveApprovedCount("role-1"));
    }

    @Test
    void legacyOpenApplicationsCanCloseButClosedTicketsCannot() throws Exception {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO applications (id, roleId, state, result, issuer) VALUES "
                    + "('legacy-open', 'role-1', 0, 0, 'user-1'), ('legacy-closed', 'role-1', 0, 0, 'user-2')");
            statement.executeUpdate("INSERT INTO tickets (id, status) VALUES ('legacy-open', 'OPEN'), ('legacy-closed', 'CLOSED')");
        }
        var applications = new ApplicationTable();
        assertEquals(ApplicationLifecycle.OPEN, applications.retrieveById("legacy-open").orElseThrow().lifecycle());
        assertTrue(applications.closeIfOpen("legacy-open", ApplicationDecision.APPROVED, "senior-1", "Ready"));
        assertEquals(ApplicationLifecycle.CLOSED, applications.retrieveById("legacy-open").orElseThrow().lifecycle());
        assertFalse(applications.closeIfOpen("legacy-open", ApplicationDecision.DENIED, "senior-2", "Duplicate"));
        assertFalse(applications.closeIfOpen("legacy-closed", ApplicationDecision.APPROVED, "senior-1", "Already closed"));
    }

    @Test
    void approvedCountIncludesLegacyAcceptedApplications() throws Exception {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO applications (id, roleId, state, result, issuer) "
                    + "VALUES ('legacy-approved', 'role-1', 0, 1, 'user-1')");
        }
        assertEquals(1, new ApplicationTable().retrieveApprovedCount("role-1"));
    }

    @Test
    void removingVoteLeavesAppendOnlyHistory() {
        var applications = new ApplicationTable();
        var votes = new ApplicationVoteTable();
        applications.createProvisioning("application-2", "role-1", "applicant-1", null, 3, List.of());
        applications.markOpen("application-2", "review-channel", "review-message");

        votes.set("application-2", "staff-1", ApplicationVoteChoice.APPROVE);
        assertTrue(votes.remove("application-2", "staff-1"));

        assertTrue(votes.list("application-2").isEmpty());
        assertEquals(List.of(ApplicationVoteAction.SET, ApplicationVoteAction.REMOVE),
                votes.history("application-2").stream().map(event -> event.action()).toList());
    }

    private static void setDatabaseConnection(Connection connection) throws Exception {
        Field field = DatabaseManager.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(null, connection);
    }
}

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
 * any form of claim, damages or other other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.database.tables.database;

import dev.vxrp.bot.application.data.ApplicationTally;
import dev.vxrp.bot.application.data.ApplicationVote;
import dev.vxrp.bot.application.data.ApplicationVoteEvent;
import dev.vxrp.bot.application.enums.ApplicationVoteAction;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApplicationVoteTable {
    public ApplicationVote getCurrent(String applicationId, String voterId) {
        return getCurrent(DatabaseManager.getConnection(), applicationId, voterId);
    }

    public ApplicationVote getCurrent(Connection connection, String applicationId, String voterId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT choice, updated_at FROM application_votes WHERE application_id = ? AND voter_id = ?")) {
            statement.setString(1, applicationId);
            statement.setString(2, voterId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? readVote(applicationId, voterId, results) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ApplicationVote set(String applicationId, String voterId, ApplicationVoteChoice choice) {
        return DatabaseManager.transaction(connection -> set(connection, applicationId, voterId, choice));
    }

    public ApplicationVote set(Connection connection, String applicationId, String voterId,
                               ApplicationVoteChoice choice) throws SQLException {
        if (choice == null) {
            throw new IllegalArgumentException("Vote choice cannot be null");
        }

        ApplicationVote current = getCurrent(connection, applicationId, voterId);
        if (current != null && current.choice() == choice) {
            return current;
        }

        Instant now = Instant.now();
        ApplicationVoteAction action = current == null ? ApplicationVoteAction.SET : ApplicationVoteAction.CHANGE;
        upsert(connection, applicationId, voterId, choice, now);
        appendEvent(connection, applicationId, voterId, action, choice, now);
        return new ApplicationVote(applicationId, voterId, choice, now);
    }

    public ApplicationVote change(String applicationId, String voterId, ApplicationVoteChoice choice) {
        return set(applicationId, voterId, choice);
    }

    public ApplicationVote change(Connection connection, String applicationId, String voterId,
                                  ApplicationVoteChoice choice) throws SQLException {
        return set(connection, applicationId, voterId, choice);
    }

    public boolean remove(String applicationId, String voterId) {
        return DatabaseManager.transaction(connection -> remove(connection, applicationId, voterId));
    }

    public boolean remove(Connection connection, String applicationId, String voterId) throws SQLException {
        ApplicationVote current = getCurrent(connection, applicationId, voterId);
        if (current == null) {
            return false;
        }

        Instant now = Instant.now();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM application_votes WHERE application_id = ? AND voter_id = ?")) {
            statement.setString(1, applicationId);
            statement.setString(2, voterId);
            statement.executeUpdate();
        }
        appendEvent(connection, applicationId, voterId, ApplicationVoteAction.REMOVE, null, now);
        return true;
    }

    public ApplicationTally tally(String applicationId, int quorumRequired) {
        return tally(DatabaseManager.getConnection(), applicationId, quorumRequired);
    }

    public ApplicationTally tally(Connection connection, String applicationId, int quorumRequired) {
        int approve = 0;
        int deny = 0;
        int abstain = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT choice, COUNT(*) AS total FROM application_votes WHERE application_id = ? GROUP BY choice")) {
            statement.setString(1, applicationId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    switch (parseChoice(results.getString("choice"))) {
                        case APPROVE -> approve = results.getInt("total");
                        case DENY -> deny = results.getInt("total");
                        case ABSTAIN -> abstain = results.getInt("total");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ApplicationTally(approve, deny, abstain, Math.max(0, quorumRequired));
    }

    public List<ApplicationVote> list(String applicationId) {
        return list(DatabaseManager.getConnection(), applicationId);
    }

    public List<ApplicationVote> list(Connection connection, String applicationId) {
        List<ApplicationVote> votes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT voter_id, choice, updated_at FROM application_votes WHERE application_id = ? ORDER BY voter_id")) {
            statement.setString(1, applicationId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    votes.add(readVote(applicationId, results.getString("voter_id"), results));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return votes;
    }

    public List<ApplicationVote> retrieveVotes(String applicationId) {
        return list(applicationId);
    }

    public List<ApplicationVoteEvent> history(String applicationId) {
        return history(DatabaseManager.getConnection(), applicationId);
    }

    public List<ApplicationVoteEvent> history(Connection connection, String applicationId) {
        List<ApplicationVoteEvent> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, voter_id, action, choice, created_at FROM application_vote_events " +
                        "WHERE application_id = ? ORDER BY created_at, id")) {
            statement.setString(1, applicationId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    events.add(new ApplicationVoteEvent(results.getString("id"), applicationId,
                            results.getString("voter_id"), parseAction(results.getString("action")),
                            parseNullableChoice(results.getString("choice")), parseInstant(results.getString("created_at"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return events;
    }

    public List<ApplicationVoteEvent> retrieveHistory(String applicationId) {
        return history(applicationId);
    }

    private static void upsert(Connection connection, String applicationId, String voterId,
                               ApplicationVoteChoice choice, Instant updatedAt) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE application_votes SET choice = ?, updated_at = ? WHERE application_id = ? AND voter_id = ?")) {
            update.setString(1, choice.name());
            update.setString(2, updatedAt.toString());
            update.setString(3, applicationId);
            update.setString(4, voterId);
            if (update.executeUpdate() > 0) {
                return;
            }
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO application_votes (application_id, voter_id, choice, updated_at) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, applicationId);
            insert.setString(2, voterId);
            insert.setString(3, choice.name());
            insert.setString(4, updatedAt.toString());
            insert.executeUpdate();
        }
    }

    private static void appendEvent(Connection connection, String applicationId, String voterId,
                                    ApplicationVoteAction action, ApplicationVoteChoice choice, Instant createdAt)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO application_vote_events (id, application_id, voter_id, action, choice, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, applicationId);
            statement.setString(3, voterId);
            statement.setString(4, action.name());
            statement.setString(5, choice == null ? null : choice.name());
            statement.setString(6, createdAt.toString());
            statement.executeUpdate();
        }
    }

    private static ApplicationVote readVote(String applicationId, String voterId, ResultSet results)
            throws SQLException {
        return new ApplicationVote(applicationId, voterId, parseChoice(results.getString("choice")),
                parseInstant(results.getString("updated_at")));
    }

    private static ApplicationVoteChoice parseChoice(String value) {
        try {
            return ApplicationVoteChoice.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown application vote choice: " + value, e);
        }
    }

    private static ApplicationVoteChoice parseNullableChoice(String value) {
        return value == null ? null : parseChoice(value);
    }

    private static ApplicationVoteAction parseAction(String value) {
        try {
            return ApplicationVoteAction.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown application vote action: " + value, e);
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

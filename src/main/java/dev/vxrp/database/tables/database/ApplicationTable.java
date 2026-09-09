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

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationTable {
    private static final int DEFAULT_QUORUM = 3;
    private static final Duration DEFAULT_PROVISIONING_TIMEOUT = Duration.ofMinutes(30);

    public void addToDatabase(String id, String roleId, boolean state, boolean result, String issuer, String handler) {
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO applications (id, roleId, state, result, issuer, handler, lifecycle_status, created_at, quorum_required) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            bindLegacyApplication(statement, id, roleId, state, result, issuer, handler);
            statement.setString(7, ApplicationLifecycle.OPEN.name());
            statement.setString(8, Instant.now().toString());
            statement.setInt(9, DEFAULT_QUORUM);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createProvisioning(String id, String roleId, String issuer, String handler, int quorumRequired,
                                   List<ApplicationAnswer> answers) {
        DatabaseManager.transaction(connection -> {
            createProvisioning(connection, id, roleId, issuer, handler, quorumRequired, answers);
            return null;
        });
    }

    public void createProvisioning(Connection connection, String id, String roleId, String issuer, String handler,
                                   int quorumRequired, List<ApplicationAnswer> answers) throws SQLException {
        createProvisioning(connection, id, roleId, issuer, handler, quorumRequired);
        new ApplicationAnswerTable().replace(connection, id, answers);
    }

    public void createProvisioning(Connection connection, String id, String roleId, String issuer, String handler,
                                   int quorumRequired) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO applications (id, roleId, state, result, issuer, handler, lifecycle_status, created_at, quorum_required) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            bindLegacyApplication(statement, id, roleId, false, false, issuer, handler);
            statement.setString(7, ApplicationLifecycle.PROVISIONING.name());
            statement.setString(8, Instant.now().toString());
            statement.setInt(9, normalizedQuorum(quorumRequired));
            statement.executeUpdate();
        }
    }

    public void markOpen(String id, String reviewChannelId, String reviewMessageId) {
        updateReviewPointer(id, reviewChannelId, reviewMessageId);
    }

    public void markOpen(Connection connection, String id, String reviewChannelId, String reviewMessageId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE applications SET lifecycle_status = ?, state = ?, review_channel_id = ?, review_message_id = ? " +
                        "WHERE id = ? AND lifecycle_status = ?")) {
            statement.setString(1, ApplicationLifecycle.OPEN.name());
            statement.setBoolean(2, true);
            statement.setString(3, reviewChannelId);
            statement.setString(4, reviewMessageId);
            statement.setString(5, id);
            statement.setString(6, ApplicationLifecycle.PROVISIONING.name());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Application is no longer provisioning: " + id);
            }
        }
    }

    public void updateReviewPointer(String id, String reviewChannelId, String reviewMessageId) {
        DatabaseManager.transaction(connection -> {
            markOpen(connection, id, reviewChannelId, reviewMessageId);
            return null;
        });
    }

    public void markFailed(String id) {
        DatabaseManager.transaction(connection -> {
            markFailed(connection, id);
            return null;
        });
    }

    public void markFailed(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE applications SET lifecycle_status = ?, state = ? WHERE id = ? AND lifecycle_status = ?")) {
            statement.setString(1, ApplicationLifecycle.FAILED.name());
            statement.setBoolean(2, false);
            statement.setString(3, id);
            statement.setString(4, ApplicationLifecycle.PROVISIONING.name());
            statement.executeUpdate();
        }
    }

    public Optional<ApplicationRecord> retrieveById(String id) {
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(applicationQuery() + " WHERE a.id = ?")) {
            statement.setString(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return Optional.of(readRecord(results, DatabaseManager.getConnection()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<ApplicationRecord> getById(String id) {
        return retrieveById(id);
    }

    public List<ApplicationRecord> list(ApplicationQueueFilter filter, int page, int pageSize) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = Math.max(1, pageSize);
        return retrievePage(filter, normalizedPage * normalizedSize, normalizedSize);
    }

    public List<ApplicationRecord> retrievePage(ApplicationQueueFilter filter, int offset, int limit) {
        List<ApplicationRecord> matching = filter(filter);
        int start = Math.min(Math.max(0, offset), matching.size());
        int end = Math.min(start + Math.max(0, limit), matching.size());
        return new ArrayList<>(matching.subList(start, end));
    }

    public List<ApplicationRecord> filter(ApplicationQueueFilter filter) {
        List<ApplicationRecord> records = new ArrayList<>();
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet results = statement.executeQuery(applicationQuery() + " ORDER BY a.created_at DESC, a.id DESC")) {
            while (results.next()) {
                ApplicationRecord record = readRecord(results, DatabaseManager.getConnection());
                if (matchesFilter(record, filter)) {
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public List<ApplicationRecord> retrieveAll() {
        return filter(ApplicationQueueFilter.ALL);
    }

    public long count(ApplicationQueueFilter filter) {
        return filter(filter).size();
    }

    public long count() {
        return count(ApplicationQueueFilter.ALL);
    }

    public long retrieveOpenCount() {
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet results = statement.executeQuery(openCountQuery())) {
            return results.next() ? results.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long retrieveOpenCount(String roleId) {
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(
                openCountQuery() + " AND a.roleId = ?")) {
            statement.setString(1, roleId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long retrieveSerial(String roleId) {
        return retrieveOpenCount(roleId);
    }

    public void updateTicketHandler(String id, String handler) {
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(
                "UPDATE applications SET handler = ? WHERE id = ?")) {
            statement.setString(1, handler);
            statement.setString(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTicketHandler(Connection connection, String id, String handler) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE applications SET handler = ? WHERE id = ?")) {
            statement.setString(1, handler);
            statement.setString(2, id);
            statement.executeUpdate();
        }
    }

    public boolean closeIfOpen(String id, ApplicationDecision decision, String closedBy, String reason) {
        return DatabaseManager.transaction(connection -> closeIfOpen(connection, id, decision, closedBy, reason, Instant.now()));
    }

    public boolean closeIfOpen(Connection connection, String id, ApplicationDecision decision, String closedBy,
                               String reason, Instant closedAt) throws SQLException {
        String sql = "UPDATE applications SET lifecycle_status = ?, decision = ?, state = ?, result = ?, " +
                "closed_at = ?, closed_by = ?, close_reason = ? WHERE id = ? AND lifecycle_status = 'OPEN'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ApplicationLifecycle.CLOSED.name());
            statement.setString(2, decision.name());
            statement.setBoolean(3, false);
            statement.setBoolean(4, decision == ApplicationDecision.APPROVED);
            statement.setString(5, closedAt.toString());
            statement.setString(6, closedBy);
            statement.setString(7, reason);
            statement.setString(8, id);
            return statement.executeUpdate() == 1;
        }
    }

    public int markStaleProvisioningFailed() {
        return markStaleProvisioningFailed(DEFAULT_PROVISIONING_TIMEOUT);
    }

    public int markStaleProvisioningFailed(Duration timeout) {
        Instant cutoff = Instant.now().minus(timeout);
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(
                "UPDATE applications SET lifecycle_status = ?, state = ? WHERE lifecycle_status = ? AND created_at < ?")) {
            statement.setString(1, ApplicationLifecycle.FAILED.name());
            statement.setBoolean(2, false);
            statement.setString(3, ApplicationLifecycle.PROVISIONING.name());
            statement.setString(4, cutoff.toString());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String id) {
        // Application rows are the durable review record and must survive ticket archiving.
    }

    private static void bindLegacyApplication(PreparedStatement statement, String id, String roleId, boolean state,
                                              boolean result, String issuer, String handler) throws SQLException {
        statement.setString(1, id);
        statement.setString(2, roleId);
        statement.setBoolean(3, state);
        statement.setBoolean(4, result);
        statement.setString(5, issuer);
        statement.setString(6, handler);
    }

    private static int normalizedQuorum(int quorumRequired) {
        return quorumRequired < 1 ? DEFAULT_QUORUM : quorumRequired;
    }

    private static String applicationQuery() {
        return "SELECT a.id, a.roleId, a.issuer, a.handler, a.lifecycle_status, a.decision, a.created_at, " +
                "a.closed_at, a.closed_by, a.close_reason, a.review_channel_id, a.review_message_id, a.quorum_required, " +
                "a.state, a.result FROM applications a";
    }

    private static String openCountQuery() {
        return "SELECT COUNT(*) FROM applications a WHERE " + openLifecycleCondition(null);
    }

    private static String openLifecycleCondition(String alias) {
        String applicationAlias = alias == null ? "a." : alias.isBlank() ? "" : alias + ".";
        return "(" + applicationAlias + "lifecycle_status IN ('PROVISIONING', 'OPEN') OR " +
                "(" + applicationAlias + "lifecycle_status IS NULL AND NOT EXISTS (SELECT 1 FROM tickets t WHERE t.id = " +
                applicationAlias + "id AND t.status = 'CLOSED')))";
    }

    private static ApplicationRecord readRecord(ResultSet results, Connection connection) throws SQLException {
        String id = results.getString("id");
        String lifecycleValue = results.getString("lifecycle_status");
        ApplicationLifecycle lifecycle = lifecycleValue == null
                ? legacyLifecycle(connection, id)
                : parseLifecycle(lifecycleValue);
        String decisionValue = results.getString("decision");
        int quorum = results.getInt("quorum_required");
        if (results.wasNull()) {
            quorum = DEFAULT_QUORUM;
        }
        return new ApplicationRecord(id, results.getString("roleId"), results.getString("issuer"),
                results.getString("handler"), lifecycle, parseDecision(decisionValue),
                parseInstant(results.getString("created_at")), parseInstant(results.getString("closed_at")),
                results.getString("closed_by"), results.getString("close_reason"),
                results.getString("review_channel_id"), results.getString("review_message_id"), quorum);
    }

    private static ApplicationLifecycle legacyLifecycle(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT status FROM tickets WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next() && "CLOSED".equalsIgnoreCase(results.getString(1))) {
                    return ApplicationLifecycle.CLOSED;
                }
            }
        }
        return ApplicationLifecycle.OPEN;
    }

    private static ApplicationLifecycle parseLifecycle(String value) {
        try {
            return ApplicationLifecycle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApplicationLifecycle.OPEN;
        }
    }

    private static ApplicationDecision parseDecision(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ApplicationDecision.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
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

    private boolean matchesFilter(ApplicationRecord record, ApplicationQueueFilter filter) {
        if (filter == null || filter == ApplicationQueueFilter.ALL) {
            return true;
        }
        if (filter == ApplicationQueueFilter.OPEN) {
            return record.lifecycle() == ApplicationLifecycle.OPEN;
        }
        if (filter == ApplicationQueueFilter.CLOSED) {
            return record.lifecycle() == ApplicationLifecycle.CLOSED;
        }
        if (filter == ApplicationQueueFilter.READY) {
            return record.lifecycle() == ApplicationLifecycle.OPEN
                    && new ApplicationVoteTable().tally(record.id(), record.quorumRequired()).quorumReached();
        }
        return false;
    }
}

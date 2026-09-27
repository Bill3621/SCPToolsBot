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

package dev.vxrp.database.migrations;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public final class ApplicationMigrationRunner {
    private static final int APPLICATION_SCHEMA_VERSION = 1;

    private ApplicationMigrationRunner() {}

    public static void run(Connection connection) throws SQLException {
        createMigrationTable(connection);
        addApplicationColumns(connection);
        createApplicationTables(connection);
        createApplicationIndexes(connection);
        recordVersion(connection);
    }

    private static void createMigrationTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS schema_migrations (" +
                    "version INTEGER PRIMARY KEY, applied_at TEXT NOT NULL)");
        }
    }

    private static void addApplicationColumns(Connection connection) throws SQLException {
        addColumnIfAbsent(connection, "applications", "lifecycle_status", "VARCHAR(32)");
        addColumnIfAbsent(connection, "applications", "decision", "VARCHAR(32)");
        addColumnIfAbsent(connection, "applications", "created_at", "VARCHAR(64)");
        addColumnIfAbsent(connection, "applications", "closed_at", "VARCHAR(64)");
        addColumnIfAbsent(connection, "applications", "closed_by", "VARCHAR(255)");
        addColumnIfAbsent(connection, "applications", "close_reason", "TEXT");
        addColumnIfAbsent(connection, "applications", "review_channel_id", "VARCHAR(255)");
        addColumnIfAbsent(connection, "applications", "review_message_id", "VARCHAR(255)");
        addColumnIfAbsent(connection, "applications", "review_thread_id", "VARCHAR(255)");
        addColumnIfAbsent(connection, "applications", "quorum_required", "INTEGER");
    }

    private static void createApplicationTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS application_answers (" +
                    "application_id VARCHAR(255) NOT NULL, " +
                    "field_key VARCHAR(255) NOT NULL, " +
                    "answer TEXT NOT NULL, " +
                    "PRIMARY KEY (application_id, field_key))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS application_votes (" +
                    "application_id VARCHAR(255) NOT NULL, " +
                    "voter_id VARCHAR(255) NOT NULL, " +
                    "choice VARCHAR(32) NOT NULL, " +
                    "updated_at VARCHAR(64) NOT NULL, " +
                    "PRIMARY KEY (application_id, voter_id))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS application_vote_events (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "application_id VARCHAR(255) NOT NULL, " +
                    "voter_id VARCHAR(255) NOT NULL, " +
                    "action VARCHAR(32) NOT NULL, " +
                    "choice VARCHAR(32), " +
                    "created_at VARCHAR(64) NOT NULL)");
        }
    }

    private static void createApplicationIndexes(Connection connection) throws SQLException {
        createIndexIfAbsent(connection, "idx_applications_lifecycle", "applications", "lifecycle_status");
        createIndexIfAbsent(connection, "idx_applications_role_lifecycle", "applications", "roleId, lifecycle_status");
        createIndexIfAbsent(connection, "idx_application_answers_application", "application_answers", "application_id");
        createIndexIfAbsent(connection, "idx_application_votes_application", "application_votes", "application_id");
        createIndexIfAbsent(connection, "idx_application_vote_events_application", "application_vote_events", "application_id, created_at");
        createIndexIfAbsent(connection, "idx_application_vote_events_voter", "application_vote_events", "voter_id, created_at");
    }

    private static void recordVersion(Connection connection) throws SQLException {
        if (versionExists(connection, APPLICATION_SCHEMA_VERSION)) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO schema_migrations (version, applied_at) VALUES (?, ?)")) {
            statement.setInt(1, APPLICATION_SCHEMA_VERSION);
            statement.setString(2, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void addColumnIfAbsent(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        return metadataColumnExists(metadata, table, column)
                || metadataColumnExists(metadata, table.toUpperCase(), column.toUpperCase())
                || metadataColumnExists(metadata, table.toLowerCase(), column.toLowerCase());
    }

    private static boolean metadataColumnExists(DatabaseMetaData metadata, String table, String column)
            throws SQLException {
        try (ResultSet columns = metadata.getColumns(null, null, table, column)) {
            return columns.next();
        }
    }

    private static void createIndexIfAbsent(Connection connection, String index, String table, String columns)
            throws SQLException {
        if (indexExists(connection, table, index)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE INDEX " + index + " ON " + table + " (" + columns + ")");
        }
    }

    private static boolean indexExists(Connection connection, String table, String index) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        return metadataIndexExists(metadata, table, index)
                || metadataIndexExists(metadata, table.toUpperCase(), index.toUpperCase())
                || metadataIndexExists(metadata, table.toLowerCase(), index.toLowerCase());
    }

    private static boolean metadataIndexExists(DatabaseMetaData metadata, String table, String index)
            throws SQLException {
        try (ResultSet indexes = metadata.getIndexInfo(null, null, table, false, false)) {
            while (indexes.next()) {
                if (index.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean versionExists(Connection connection, int version) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT version FROM schema_migrations WHERE version = ?")) {
            statement.setInt(1, version);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }
}

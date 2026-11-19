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

package dev.vxrp.database.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public final class RegularsPlaytimeMigration {
    private static final int VERSION = 2;

    private RegularsPlaytimeMigration() {}

    public static void run(Connection connection) throws SQLException {
        if (applied(connection)) return;

        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement update = connection.createStatement(); PreparedStatement record = connection.prepareStatement("INSERT INTO schema_migrations (version, applied_at) VALUES (?, ?)")) {
            update.executeUpdate("UPDATE regulars SET playtime = playtime / 3600");
            record.setInt(1, VERSION);
            record.setString(2, Instant.now().toString());
            record.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private static boolean applied(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT version FROM schema_migrations WHERE version = ?")) {
            statement.setInt(1, VERSION);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }
}

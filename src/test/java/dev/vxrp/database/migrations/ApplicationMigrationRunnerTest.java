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

package dev.vxrp.database.migrations;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationMigrationRunnerTest {
    @Test
    void migrationIsIdempotentAndPreservesLegacyApplications() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE tickets (id TEXT PRIMARY KEY, status TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE applications (id TEXT PRIMARY KEY, roleId TEXT NOT NULL, "
                    + "state BOOLEAN NOT NULL, result BOOLEAN NOT NULL, issuer TEXT NOT NULL, handler TEXT)");
            statement.executeUpdate("INSERT INTO applications VALUES ('legacy', 'role', 1, 0, 'user', NULL)");

            ApplicationMigrationRunner.run(connection);
            ApplicationMigrationRunner.run(connection);

            try (var migrations = statement.executeQuery("SELECT COUNT(*) FROM schema_migrations WHERE version = 1")) {
                assertTrue(migrations.next());
                assertEquals(1, migrations.getInt(1));
            }
            try (var applications = statement.executeQuery("SELECT lifecycle_status, review_thread_id FROM applications WHERE id = 'legacy'")) {
                assertTrue(applications.next());
                assertEquals(null, applications.getString(1));
                assertEquals(null, applications.getString(2));
            }
            try (var tables = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                    + "AND name IN ('application_answers', 'application_votes', 'application_vote_events')")) {
                assertTrue(tables.next());
                assertEquals(3, tables.getInt(1));
            }
        }
    }
}

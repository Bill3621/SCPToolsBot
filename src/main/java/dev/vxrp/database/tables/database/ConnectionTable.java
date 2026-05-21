/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
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

import dev.vxrp.database.DatabaseManager;
import dev.vxrp.database.data.ConnectionDatabaseEntry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionTable {

    public void insertIfNotExists(String id, Boolean status, Boolean maintenance) {
        if (exists(id)) return;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO connections (id, status, maintenance) VALUES (?, ?, ?)")) {
            stmt.setString(1, id);
            if (status != null) {
                stmt.setBoolean(2, status);
            } else {
                stmt.setNull(2, java.sql.Types.BOOLEAN);
            }
            if (maintenance != null) {
                stmt.setBoolean(3, maintenance);
            } else {
                stmt.setNull(3, java.sql.Types.BOOLEAN);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ConnectionDatabaseEntry queryFromTable(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT status, maintenance FROM connections WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean status = rs.getBoolean("status") && !rs.wasNull();
                    boolean maintenance = rs.getBoolean("maintenance") && !rs.wasNull();
                    return new ConnectionDatabaseEntry(id, status, maintenance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ConnectionDatabaseEntry(id, false, false);
    }

    public void databaseNotExists(String id, boolean status) {
        if (exists(id)) return;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO connections (id, status) VALUES (?, ?)")) {
            stmt.setString(1, id);
            stmt.setBoolean(2, status);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void postConnectionToDatabase(String id, boolean status) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "UPDATE connections SET status = ? WHERE id = ?")) {
            stmt.setBoolean(1, status);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMaintenance(String id, Boolean maintenance) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "UPDATE connections SET maintenance = ? WHERE id = ?")) {
            if (maintenance != null) {
                stmt.setBoolean(1, maintenance);
            } else {
                stmt.setNull(1, java.sql.Types.BOOLEAN);
            }
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean exists(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT id FROM connections WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

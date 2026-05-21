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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationTable {

    public void addToDatabase(String id, String roleId, boolean state, boolean result,
                              String issuer, String handler) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO applications (id, roleId, state, result, issuer, handler) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, id);
            stmt.setString(2, roleId);
            stmt.setBoolean(3, state);
            stmt.setBoolean(4, result);
            stmt.setString(5, issuer);
            stmt.setString(6, handler);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTicketHandler(String id, String handler) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "UPDATE applications SET handler = ? WHERE id = ?")) {
            stmt.setString(1, handler);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "DELETE FROM applications WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long retrieveSerial(String roleId) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT COUNT(*) FROM applications WHERE roleId = ?")) {
            stmt.setString(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}

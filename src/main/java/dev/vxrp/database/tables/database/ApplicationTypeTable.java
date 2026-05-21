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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationTypeTable {
    private final Logger logger = LoggerFactory.getLogger(ApplicationTypeTable.class);

    public static class ApplicationType {
        public final String roleId;
        public final boolean active;
        public final Integer members;
        public final String initializer;

        public ApplicationType(String roleId, boolean active, Integer members, String initializer) {
            this.roleId = roleId;
            this.active = active;
            this.members = members;
            this.initializer = initializer;
        }
    }

    public boolean exists(String roleId) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT roleId FROM application_types WHERE roleId = ?")) {
            stmt.setString(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRedundantValues(List<String> roleIds) {
        try (PreparedStatement selectStmt = DatabaseManager.getConnection().prepareStatement("SELECT roleId FROM application_types");
             ResultSet rs = selectStmt.executeQuery()) {
            while (rs.next()) {
                String currentId = rs.getString("roleId");
                if (!roleIds.contains(currentId)) {
                    try (PreparedStatement deleteStmt = DatabaseManager.getConnection().prepareStatement(
                            "DELETE FROM application_types WHERE roleId = ?")) {
                        deleteStmt.setString(1, currentId);
                        deleteStmt.executeUpdate();
                        logger.info("Found and deleted redundant application type from database: {}", currentId);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToDatabase(String roleId, boolean active, Integer members, String initializer) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO application_types (roleId, active, members, initializer) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, roleId);
            stmt.setBoolean(2, active);
            if (members != null) {
                stmt.setInt(3, members);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setString(4, initializer);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ApplicationType> getAllEntries() {
        List<ApplicationType> typeList = new ArrayList<>();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM application_types");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int membersVal = rs.getInt("members");
                typeList.add(new ApplicationType(
                        rs.getString("roleId"),
                        rs.getBoolean("active"),
                        rs.wasNull() ? null : membersVal,
                        rs.getString("initializer")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return typeList;
    }

    public ApplicationType query(String roleId) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT * FROM application_types WHERE roleId = ?")) {
            stmt.setString(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int membersVal = rs.getInt("members");
                    return new ApplicationType(
                            rs.getString("roleId"),
                            rs.getBoolean("active"),
                            rs.wasNull() ? null : membersVal,
                            rs.getString("initializer"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void changeType(String roleId, boolean active, int members, String initializer) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "UPDATE application_types SET active = ?, members = ?, initializer = ? WHERE roleId = ?")) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, members);
            stmt.setString(3, initializer);
            stmt.setString(4, roleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

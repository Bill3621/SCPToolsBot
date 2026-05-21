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

import dev.vxrp.bot.status.enums.PlayerlistType;
import dev.vxrp.database.DatabaseManager;
import dev.vxrp.database.data.StatusDatabaseEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatusTable {

    public void addToDatabase(PlayerlistType type, String channelId, String messageId,
                              String port, String created, String lastUpdated) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO playerlist (type, channel_id, message_id, port, created, last_updated) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, type.toString());
            stmt.setString(2, channelId);
            stmt.setString(3, messageId);
            stmt.setString(4, port);
            stmt.setString(5, created);
            stmt.setString(6, lastUpdated);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLastUpdated(String port, String lastUpdated) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE playerlist SET last_updated = ? WHERE port = ?")) {
            stmt.setString(1, lastUpdated);
            stmt.setString(2, port);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFromDatabase(String port) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM playerlist WHERE port = ?")) {
            stmt.setString(1, port);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StatusDatabaseEntry> getAllEntries() {
        List<StatusDatabaseEntry> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM playerlist");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new StatusDatabaseEntry(
                        PlayerlistType.valueOf(rs.getString("type")),
                        rs.getString("channel_id"),
                        rs.getString("message_id"),
                        rs.getString("port"),
                        rs.getString("created"),
                        rs.getString("last_updated")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public PlayerlistType getType(String port) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT type, port FROM playerlist WHERE type = ?")) {
            stmt.setString(1, PlayerlistType.PRESET.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString("port").equals(port)) {
                        return PlayerlistType.valueOf(rs.getString("type"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

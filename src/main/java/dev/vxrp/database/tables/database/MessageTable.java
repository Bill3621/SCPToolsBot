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

import dev.vxrp.bot.application.enums.MessageType;
import dev.vxrp.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageTable {

    public static class MessageTableData {
        public final String id;
        public final MessageType type;
        public final String channelId;

        public MessageTableData(String id, MessageType type, String channelId) {
            this.id = id;
            this.type = type;
            this.channelId = channelId;
        }
    }

    public void insertIfNotExists(String id, MessageType type, String channelId) {
        if (!exists(id)) {
            try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                            "INSERT INTO messages (id, type, channelId) VALUES (?, ?, ?)")) {
                stmt.setString(1, id);
                stmt.setString(2, type.toString());
                stmt.setString(3, channelId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public MessageTableData queryFromTable(MessageType type) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT id, channelId FROM messages WHERE type = ?")) {
            stmt.setString(1, type.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MessageTableData(rs.getString("id"), type, rs.getString("channelId"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void delete(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "DELETE FROM messages WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean exists(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT id FROM messages WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

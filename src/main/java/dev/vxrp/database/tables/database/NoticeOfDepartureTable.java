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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NoticeOfDepartureTable {

    public void addToDatabase(String id, boolean active, String handlerId, String channelId,
                              String messageId, String beginDate, String endDate) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO notice_of_departures (id, active, handler_id, channel_id, message_id, begin_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, id);
            stmt.setBoolean(2, active);
            stmt.setString(3, handlerId);
            stmt.setString(4, channelId);
            stmt.setString(5, messageId);
            stmt.setString(6, beginDate);
            stmt.setString(7, endDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteEntry(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "DELETE FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> retrieveAllIds() {
        List<String> list = new ArrayList<>();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id FROM notice_of_departures");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public String retrieveHandler(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT handler_id FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("handler_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String retrieveChannel(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT channel_id FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("channel_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String retrieveMessage(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT message_id FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("message_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String retrieveBeginDate(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT begin_date FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("begin_date");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String retrieveEndDate(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT end_date FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("end_date");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public long retrieveSerial() {
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notice_of_departures")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public boolean exists(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT id FROM notice_of_departures WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

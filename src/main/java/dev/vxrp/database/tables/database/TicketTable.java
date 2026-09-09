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

import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.database.DatabaseManager;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TicketTable {

    public void addToDatabase(String id, TicketType type, TicketStatus status, String creationDate,
                              String creator, User handler, String logMessage, String message, String statusMessage) {
        try {
            addToDatabase(DatabaseManager.getConnection(), id, type, status, creationDate, creator,
                    handler != null ? handler.getId() : null, logMessage, message, statusMessage);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToDatabase(Connection connection, String id, TicketType type, TicketStatus status, String creationDate,
                              String creator, String handlerId, String logMessage, String message, String statusMessage)
            throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO tickets (id, type, status, creation_date, creator, handler, log_message, status_message, message) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, id);
            stmt.setString(2, type.toString());
            stmt.setString(3, status.toString());
            stmt.setString(4, creationDate);
            stmt.setString(5, creator);
            stmt.setString(6, handlerId);
            stmt.setString(7, logMessage);
            stmt.setString(8, statusMessage);
            stmt.setString(9, message);
            stmt.executeUpdate();
        }
    }

    public TicketType determineTicketType(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT type FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return TicketType.valueOf(rs.getString("type"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("No ticket type found for id: " + id);
    }

    public void updateTicketStatus(String id, TicketStatus ticketStatus) {
        updateTicketStatus(DatabaseManager.getConnection(), id, ticketStatus);
    }

    public void updateTicketStatus(Connection connection, String id, TicketStatus ticketStatus) {
        try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE tickets SET status = ? WHERE id = ?")) {
            stmt.setString(1, ticketStatus.toString());
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLogMessage(String id, String logMessage) {
        updateLogMessage(DatabaseManager.getConnection(), id, logMessage);
    }

    public void updateLogMessage(Connection connection, String id, String logMessage) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE tickets SET log_message = ? WHERE id = ?")) {
            statement.setString(1, logMessage);
            statement.setString(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTicketLogMessage(String id, String logMessage) {
        updateLogMessage(id, logMessage);
    }

    public void updateTicketLogMessage(Connection connection, String id, String logMessage) {
        updateLogMessage(connection, id, logMessage);
    }

    public String getTicketCreator(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT creator FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("creator");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getTicketHandler(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT handler FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("handler");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getLogMessage(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT log_message FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("log_message");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getMessage(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT message FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("message");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public TicketStatus getTicketStatus(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT status FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return TicketStatus.valueOf(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public TicketType getTicketType(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT type FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return TicketType.valueOf(rs.getString("type"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updateTicketHandler(String ticketId, String userId) {
        updateTicketHandler(DatabaseManager.getConnection(), ticketId, userId);
    }

    public void updateTicketHandler(Connection connection, String ticketId, String userId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE tickets SET handler = ? WHERE id = ?")) {
            stmt.setString(1, userId);
            stmt.setString(2, ticketId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long retrieveSerial() {
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tickets")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public boolean determineHandler(String id) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT handler FROM tickets WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("handler") == null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

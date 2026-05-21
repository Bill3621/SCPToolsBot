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

package dev.vxrp.database;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.database.enums.AuthType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XPDatabaseHandler {
    private Connection database;
    private final Config config;
    private final Logger logger = LoggerFactory.getLogger(XPDatabaseHandler.class);

    public XPDatabaseHandler(Config config) {
        this.config = config;
    }

    public Connection getDatabase() {
        return database;
    }

    public void setDatabase(Connection database) {
        this.database = database;
    }

    public Connection connectToDatabase() {
        if (!config.settings().xp().active()) return null;

        if (config.settings().database().dataUsePredefined().equals("NONE")
                && config.settings().database().customUrl().equals(config.settings().xp().databaseAddress())) {
            logger.warn("Found Custom database and Xp database to be the same. Cancelling connection to xp database");
            return null;
        }

        String url = "jdbc:mysql://" + config.settings().xp().databaseAddress();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, config.settings().xp().databaseUser(), config.settings().xp().databasePassword());
        } catch (SQLException e) {
            logger.error("Could not connect to XP database, all xp database action will fall back to main database, please try fixing your connection");
            return null;
        } catch (ClassNotFoundException e) {
            logger.error("MySQL driver not found for XP database");
            return null;
        }
    }

    public int queryExperience(AuthType authType, long userId) {
        switch (authType) {
            case STEAM:
                return steamTableTransaction(userId);
            case DISCORD:
                return discordTableTransaction(userId);
            default:
                throw new IllegalArgumentException("Unknown auth type: " + authType);
        }
    }

    private int steamTableTransaction(long userId) {
        try (PreparedStatement stmt = database.prepareStatement("SELECT xp FROM playerinfo_Steam WHERE id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("xp");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query steam XP", e);
        }
        throw new RuntimeException("No XP entry found for steam user: " + userId);
    }

    private int discordTableTransaction(long userId) {
        try (PreparedStatement stmt = database.prepareStatement("SELECT xp FROM playerinfo_Discord WHERE id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("xp");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query discord XP", e);
        }
        throw new RuntimeException("No XP entry found for discord user: " + userId);
    }
}

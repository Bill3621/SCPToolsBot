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
import dev.vxrp.configuration.data.Settings;
import dev.vxrp.database.enums.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static Connection connection;
    private final String dir = System.getProperty("user.dir");
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final Config config;
    private final String path;

    public DatabaseManager(Config config, String path) {
        this.config = config;
        this.path = path;

        if (!connectToDatabase()) {
            logger.error("Failed to connect to default database, exiting...");
            System.exit(1);
        } else {
            logger.info("Connection to default database fully established");

            if (config.settings().xp().active()) {
                connectToXPDatabase();
            }

            createTables();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    private void connectToXPDatabase() {
        XPDatabaseHandler xpHandler = new XPDatabaseHandler(config);
        Connection xpConnection = xpHandler.connectToDatabase();

        if (xpConnection == null) {
            xpHandler.setDatabase(connection);
        } else {
            xpHandler.setDatabase(xpConnection);
        }
    }

    private boolean connectToDatabase() {
        Settings settings = config.settings();

        if (settings.database().dataUsePredefined().equals("SQLITE")) {
            try {
                Files.createDirectories(Paths.get(dir + "/SCPToolsBot/database/"));
                File dbFile = new File(dir + path);
                if (!dbFile.exists()) {
                    dbFile.createNewFile();
                }

                String url = "jdbc:sqlite:" + dir + "/" + path;
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(url);
                return true;
            } catch (SQLException e) {
                logger.error("Could not connect to default sqlite database");
                return false;
            } catch (Exception e) {
                logger.error("Could not connect to default sqlite database");
                return false;
            }
        }

        DatabaseType type;
        try {
            type = DatabaseType.valueOf(settings.database().customType());
        } catch (IllegalArgumentException e) {
            type = DatabaseType.SQlITE;
        }

        try {
            switch (type) {
                case SQlITE: {
                    String url = "jdbc:sqlite://" + settings.database().customUrl();
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection(url, settings.database().customUsername(), settings.database().customPassword());
                    return true;
                }
                case MYSQL: {
                    String url = "jdbc:mysql://" + settings.database().customUrl();
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(url, settings.database().customUsername(), settings.database().customPassword());
                    return true;
                }
                case POSTGRESQL: {
                    String url = "jdbc:postgresql://" + settings.database().customUrl();
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, settings.database().customUsername(), settings.database().customPassword());
                    return true;
                }
                case MARiADB: {
                    String url = "jdbc:mariadb://" + settings.database().customUrl();
                    Class.forName("org.mariadb.jdbc.Driver");
                    connection = DriverManager.getConnection(url, settings.database().customUsername(), settings.database().customPassword());
                    return true;
                }
                default:
                    return false;
            }
        } catch (SQLException e) {
            logger.error("Could not connect to {} database", type.name().toLowerCase());
            return false;
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found for {} database", type.name().toLowerCase());
            return false;
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tickets (" +
                    "id TEXT PRIMARY KEY, " +
                    "type TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'OPEN', " +
                    "creation_date TEXT NOT NULL, " +
                    "creator TEXT NOT NULL, " +
                    "handler TEXT, " +
                    "log_message TEXT NOT NULL, " +
                    "status_message TEXT NOT NULL, " +
                    "message TEXT NOT NULL)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS notice_of_departures (" +
                    "id TEXT PRIMARY KEY, " +
                    "active BOOLEAN NOT NULL, " +
                    "handler_id TEXT NOT NULL, " +
                    "channel_id TEXT NOT NULL, " +
                    "message_id TEXT NOT NULL, " +
                    "begin_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS regulars (" +
                    "id TEXT PRIMARY KEY, " +
                    "active BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "\"group\" TEXT NOT NULL, " +
                    "group_id TEXT, " +
                    "role_id TEXT NOT NULL, " +
                    "playtime DOUBLE NOT NULL, " +
                    "level INTEGER NOT NULL, " +
                    "last_checked_date TEXT)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS playerlist (" +
                    "type TEXT NOT NULL, " +
                    "channel_id TEXT NOT NULL, " +
                    "message_id TEXT NOT NULL, " +
                    "port TEXT NOT NULL, " +
                    "created TEXT NOT NULL, " +
                    "last_updated TEXT NOT NULL)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS connections (" +
                    "id TEXT PRIMARY KEY, " +
                    "status BOOLEAN, " +
                    "maintenance BOOLEAN)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS application_types (" +
                    "roleId TEXT PRIMARY KEY, " +
                    "active BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "members INTEGER, " +
                    "initializer TEXT)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS applications (" +
                    "id TEXT PRIMARY KEY, " +
                    "roleId TEXT NOT NULL, " +
                    "state BOOLEAN NOT NULL, " +
                    "result BOOLEAN NOT NULL, " +
                    "issuer TEXT NOT NULL, " +
                    "handler TEXT)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages (" +
                    "id TEXT PRIMARY KEY, " +
                    "type TEXT NOT NULL, " +
                    "channelId TEXT NOT NULL)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "verify_time TEXT NOT NULL, " +
                    "steam_id TEXT NOT NULL)"
            );
        } catch (SQLException e) {
            logger.error("Failed to create tables", e);
        }
    }
}

/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
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

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationAnswerTable {
    public void replace(String applicationId, List<ApplicationAnswer> answers) {
        DatabaseManager.transaction(connection -> {
            replace(connection, applicationId, answers);
            return null;
        });
    }

    public void replace(Connection connection, String applicationId, List<ApplicationAnswer> answers) throws SQLException {
        delete(connection, applicationId);
        if (answers == null || answers.isEmpty()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO application_answers (application_id, field_key, answer, position) VALUES (?, ?, ?, ?)")) {
            for (int position = 0; position < answers.size(); position++) {
                ApplicationAnswer answer = answers.get(position);
                statement.setString(1, applicationId);
                statement.setString(2, answer.fieldKey());
                statement.setString(3, answer.value());
                statement.setInt(4, position);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public void delete(String applicationId) {
        DatabaseManager.transaction(connection -> {
            delete(connection, applicationId);
            return null;
        });
    }

    public void delete(Connection connection, String applicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM application_answers WHERE application_id = ?")) {
            statement.setString(1, applicationId);
            statement.executeUpdate();
        }
    }

    public List<ApplicationAnswer> list(String applicationId) {
        return list(DatabaseManager.getConnection(), applicationId);
    }

    public List<ApplicationAnswer> list(Connection connection, String applicationId) {
        List<ApplicationAnswer> answers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT field_key, answer FROM application_answers WHERE application_id = ? ORDER BY position, field_key")) {
            statement.setString(1, applicationId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    answers.add(new ApplicationAnswer(applicationId, results.getString("field_key"),
                            results.getString("answer")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return answers;
    }
}

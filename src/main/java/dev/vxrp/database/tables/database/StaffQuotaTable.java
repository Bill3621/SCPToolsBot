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
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.database.tables.database;

import dev.vxrp.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;

public class StaffQuotaTable {

    public void recordLeaveDay(String userId, LocalDate day) {
        try (PreparedStatement select = DatabaseManager.getConnection().prepareStatement(
                "SELECT 1 FROM staff_quota_leave WHERE user_id = ? AND day = ?")) {
            select.setString(1, userId);
            select.setString(2, day.toString());
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (PreparedStatement insert = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO staff_quota_leave (user_id, day) VALUES (?, ?)")) {
            insert.setString(1, userId);
            insert.setString(2, day.toString());
            insert.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<LocalDate> leaveDays(String userId, LocalDate from, LocalDate to) {
        Set<LocalDate> days = new HashSet<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT day FROM staff_quota_leave WHERE user_id = ? AND day >= ? AND day <= ?")) {
            stmt.setString(1, userId);
            stmt.setString(2, from.toString());
            stmt.setString(3, to.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) days.add(LocalDate.parse(rs.getString("day")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return days;
    }

    public void pruneLeaveDaysBefore(LocalDate day) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM staff_quota_leave WHERE day < ?")) {
            stmt.setString(1, day.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReported(LocalDate weekStart) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT 1 FROM staff_quota_reports WHERE week_start = ?")) {
            stmt.setString(1, weekStart.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markReported(LocalDate weekStart) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO staff_quota_reports (week_start, reported_at) VALUES (?, ?)")) {
            stmt.setString(1, weekStart.toString());
            stmt.setString(2, Instant.now().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OptionalLong baseline(String userId, LocalDate weekStart) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT seconds FROM staff_quota_baselines WHERE user_id = ? AND week_start = ?")) {
            stmt.setString(1, userId);
            stmt.setString(2, weekStart.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? OptionalLong.of(rs.getLong("seconds")) : OptionalLong.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasBaselines(LocalDate weekStart) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT 1 FROM staff_quota_baselines WHERE week_start = ?")) {
            stmt.setString(1, weekStart.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void recordBaseline(String userId, LocalDate weekStart, long seconds) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO staff_quota_baselines (user_id, week_start, seconds) VALUES (?, ?, ?)")) {
            stmt.setString(1, userId);
            stmt.setString(2, weekStart.toString());
            stmt.setLong(3, seconds);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void pruneBaselinesBefore(LocalDate weekStart) {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM staff_quota_baselines WHERE week_start < ?")) {
            stmt.setString(1, weekStart.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.regulars.handler;

import dev.vxrp.bot.regulars.data.RegularDatabaseEntry;
import dev.vxrp.bot.regulars.data.RegularsConfigRole;
import dev.vxrp.bot.regulars.enums.RequirementType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.XPDatabaseHandler;
import dev.vxrp.database.enums.AuthType;
import dev.vxrp.database.tables.database.RegularsTable;
import dev.vxrp.database.tables.database.UserTable;
import io.github.vxrpenter.cedmod.Cedmod;
import net.dv8tion.jda.api.JDA;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegularsCheckerHandler {
    private final JDA api;
    private final Config config;
    private final Translation translation;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(RegularsCheckerHandler.class);

    public RegularsCheckerHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void checkerTask() {
        logger.info("Starting regulars checker, processing units starting...");

        for (RegularDatabaseEntry regular : new RegularsTable().getAllEntrys()) {
            if (!checkRegular(regular)) continue;
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public boolean checkRegular(RegularDatabaseEntry regular) {
        LocalDate lastCheckedDate = null;
        if (regular.lastCheckedDate() != null) {
            lastCheckedDate = LocalDate.parse(regular.lastCheckedDate());
        }

        UserTable userTable = new UserTable();
        if (!userTable.exists(regular.id())) {
            new RegularsTable().delete(regular.id());
            logger.warn("Could not retrieve user: {}'s verification data, deleting their regular data because of it being invalid", regular.id());
            return false;
        }

        RegularsConfigRole role = getRole(regular);
        if (role == null) {
            new RegularsTable().delete(regular.id());
            logger.error("Regular user: {}'s role entries do not match up to the regulars config, their entry will be removed", regular.id());
            return false;
        }

        String steamId = userTable.getSteamId(regular.id());
        if (steamId == null) {
            new RegularsTable().delete(regular.id());
            logger.error("Could not find user entry of regular user: {}, deleting entry", regular.id());
            return false;
        }

        RequirementType reqType = RequirementType.valueOf(role.requirementType());

        if (reqType == RequirementType.PLAYTIME) {
            if (!config.settings().cedmod().active()) {
                logger.error("Could not correctly process regulars with setting 'PLAYTIME', activate cedmod integration!");
                return false;
            }

            if (!checkPlaytime(regular, steamId, lastCheckedDate)) return false;

            checkRoles(regular.id(), regular.groupRoleId(), regular.roleId());
        } else if (reqType == RequirementType.XP) {
            if (!config.settings().xp().active()) {
                logger.error("Could not correctly process regulars with setting 'XP', activate xp integration!");
                return false;
            }

            if (!checkLevel(regular, steamId, role)) return false;

            checkRoles(regular.id(), regular.groupRoleId(), regular.roleId());
        } else if (reqType == RequirementType.BOTH) {
            if (!config.settings().cedmod().active() || !config.settings().xp().active()) {
                logger.error("Could not correctly process regulars with setting 'BOTH', activate cedmod and xp integration!");
                return false;
            }

            if (!checkPlaytime(regular, steamId, lastCheckedDate) && !checkLevel(regular, steamId, role)) return false;

            checkRoles(regular.id(), regular.groupRoleId(), regular.roleId());
        }

        new RegularsTable().setLastCheckedDate(regular.id(), LocalDate.now().toString());
        return true;
    }

    private boolean checkPlaytime(RegularDatabaseEntry regular, String steamId, LocalDate lastCheckedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (lastCheckedDate != null && lastCheckedDate.format(formatter).equals(LocalDate.now().format(formatter))) return false;

        Cedmod cedmod = new Cedmod(config.settings().cedmod().instance(), config.settings().cedmod().api(), 60, 60);

        try {
            if (regular.playtime() == 0.0) {
                var player = cedmod.playerQuery(steamId + "@steam", 10, 0, false, false, "id_field", null, 365, true, false);

                RegularsTable table = new RegularsTable();
                table.setPlaytime(regular.id(), player.getPlayers().get(0).getActivity());
                table.setLastCheckedDate(regular.id(), LocalDate.now().toString());
                logger.info("Updated user: {}'s regular playtime data for the first time, new playtime: {}", regular.id(), player.getPlayers().get(0).getActivity());

                return true;
            }

            if (lastCheckedDate == null) return false;
            int activityMin = lastCheckedDate.until(LocalDate.now()).getDays();

            var player = cedmod.playerQuery(steamId + "@steam", 10, 0, false, false, "id_field", null, activityMin, true, false);
            RegularsTable table = new RegularsTable();
            double currentPlaytime = table.getPlaytime(regular.id());

            double newPlaytime = currentPlaytime + player.getPlayers().get(0).getActivity();

            table.setPlaytime(regular.id(), newPlaytime);
            logger.info("Updated user: {}'s regular playtime data by adding: {} to their already existing playtime of: {}, result: {}", regular.id(), player.getPlayers().get(0).getActivity(), currentPlaytime, newPlaytime);
            return true;
        } catch (Exception e) {
            logger.error("Could not correctly execute cedmod call {}", e.getMessage());
            return false;
        }
    }

    private boolean checkLevel(RegularDatabaseEntry regular, String steamId, RegularsConfigRole role) {
        String discordId = regular.id();

        AuthType authType = AuthType.valueOf(config.settings().xp().authType());
        int xp;
        if (authType == AuthType.STEAM) {
            xp = new XPDatabaseHandler(config).queryExperience(AuthType.STEAM, Long.parseLong(steamId));
        } else {
            xp = new XPDatabaseHandler(config).queryExperience(AuthType.DISCORD, Long.parseLong(discordId));
        }

        double level = (-50 + Math.sqrt(((4 * xp / (double) config.settings().xp().additionalParameter()) + 9500)) / 2);

        if (level >= role.xpRequirements()) return true;
        new RegularsTable().setLevel(regular.id(), (int) level);
        logger.info("Updated user: {}'s regular xp data setting their level to: {}", regular.id(), level);
        return false;
    }

    private void checkRoles(String userId, String groupRoleId, String roleId) {
        var guild = api.getGuildById(config.settings().guildId());
        if (guild == null) return;

        var member = guild.retrieveMemberById(userId).complete();
        if (member == null) {
            logger.error("Could not grant user: {}'s regular role, do they exist?", userId);
            return;
        }

        boolean containGroupRole = false;
        boolean containsRole = false;

        for (var r : member.getRoles()) {
            if (groupRoleId != null && r.getId().equals(groupRoleId)) containGroupRole = true;
            if (r.getId().equals(roleId)) containsRole = true;
        }

        if (!containGroupRole && groupRoleId != null) {
            var groupRole = api.getRoleById(groupRoleId);
            if (groupRole == null) {
                logger.error("Could not correctly find group role: {}, does it exist?", groupRoleId);
                return;
            }

            guild.addRoleToMember(member, groupRole).queue();
            logger.info("Updated regular group role of user: {} to {}", userId, groupRole);
        }

        if (!containsRole) {
            var role = api.getRoleById(roleId);
            if (role == null) {
                logger.error("Could not correctly find role: {}, does it exist?", roleId);
                return;
            }

            guild.addRoleToMember(member, role).queue();
            logger.info("Updated regular role of user: {} to {}", userId, roleId);
        }
    }

    private RegularsConfigRole getRole(RegularDatabaseEntry regular) {
        var configQuery = new RegularsFileHandler(config).query();

        for (var cfg : configQuery) {
            if (!cfg.manifest().name().equals(regular.group())) continue;

            for (var role : cfg.config().roles()) {
                if (!role.id().equals(regular.roleId())) continue;

                return role;
            }
        }

        return null;
    }
}

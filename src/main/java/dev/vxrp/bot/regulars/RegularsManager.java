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

package dev.vxrp.bot.regulars;

import dev.vxrp.bot.regulars.handler.RegularsCheckerHandler;
import dev.vxrp.bot.regulars.handler.RegularsFileHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.RegularsTable;
import dev.vxrp.util.coroutines.ExecutorScopes;
import dev.vxrp.util.coroutines.Timer;
import net.dv8tion.jda.api.JDA;

import java.time.Duration;

public class RegularsManager {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public RegularsManager(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;

        new RegularsFileHandler(config);
    }

    public void syncRegulars(String userId, String group) {
        var configQuery = new RegularsFileHandler(config).query();

        String groupRoleId = null;
        String roleId = null;

        for (var folderGroup : configQuery) {
            if (!folderGroup.manifest().name().equals(group)) continue;

            if (folderGroup.manifest().customRole().use()) {
                groupRoleId = folderGroup.manifest().customRole().id();
            }

            for (var role : folderGroup.config().roles()) {
                roleId = role.id();
                break;
            }
            break;
        }

        RegularsTable table = new RegularsTable();
        table.addToDatabase(userId, true, group, groupRoleId, roleId, 0.0, 0, null);
        new RegularsCheckerHandler(api, config, translation).checkRegular(table.getEntry(userId));
    }

    public void reactivateSync(String userId) {
        new RegularsTable().setActive(userId, true);
    }

    public void deactivateSync(String userId) {
        new RegularsTable().setActive(userId, false);
    }

    public void removeSync(String userId) {
        new RegularsTable().delete(userId);
    }

    public void spinUpChecker() {
        if (!config.settings().regulars().active() || new RegularsTable().retrieveSerial() == 0L) return;

        new Timer().runWithTimer(
                Duration.ofHours(2),
                ExecutorScopes.regularsScope,
                () -> new RegularsCheckerHandler(api, config, translation).checkerTask()
        );
    }
}

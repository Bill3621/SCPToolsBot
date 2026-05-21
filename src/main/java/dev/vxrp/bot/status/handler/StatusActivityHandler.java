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

package dev.vxrp.bot.status.handler;

import dev.vxrp.bot.status.data.Instance;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ConnectionTable;
import io.github.vxrpenter.secretlab.data.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;

public class StatusActivityHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(StatusActivityHandler.class);
    private final Translation translation;
    private final Config config;

    public StatusActivityHandler(Translation translation, Config config) {
        this.translation = translation;
        this.config = config;
    }

    public void updateStatus(JDA api, Server server, Instance instance, String instanceKey) {
        logger.debug("Updating status of bot: {} ({}) for server - {}", api.getSelfUser().getName(), api.getSelfUser().getId(), server.getPort());
        boolean currentMaintenance = new ConnectionTable().queryFromTable(instanceKey).maintenance();

        manageStatus(server, currentMaintenance, api);
        manageActivity(server, currentMaintenance, api);
    }

    private void manageStatus(Server server, boolean maintenance, JDA api) {
        if (maintenance) {
            api.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            return;
        }

        if (!server.getOnline()) {
            api.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            return;
        }

        if (server.getPlayers() != null) {
            String[] parts = server.getPlayers().split("/");
            if (parts.length > 0 && parts[0].equals("0")) {
                api.getPresence().setStatus(OnlineStatus.IDLE);
                return;
            }
        }

        api.getPresence().setStatus(OnlineStatus.ONLINE);
    }

    private void manageActivity(Server server, boolean maintenance, JDA api) {
        if (maintenance) {
            api.getPresence().setActivity(Activity.customStatus(translation.status().activityMaintenance()));
            return;
        }

        if (!server.getOnline()) {
            api.getPresence().setActivity(Activity.customStatus(translation.status().activityOffline()));
            return;
        }

        if (server.getPlayers() != null) {
            api.getPresence().setActivity(Activity.playing(server.getPlayers()));
        }
    }
}

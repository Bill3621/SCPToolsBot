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
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.status.handler;

import dev.vxrp.bot.status.data.Instance;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ConnectionTable;
import dev.vxrp.util.GlobalVariables;
import io.github.vxrpenter.secretlab.data.Server;
import io.github.vxrpenter.secretlab.data.ServerInfo;
import net.dv8tion.jda.api.JDA;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class StatusConnectionHandler {
    private static final Map<Integer, Integer> reconnectAttempt = new HashMap<>();
    private static int retryFetchData = 0;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(StatusConnectionHandler.class);
    private final Translation translation;
    private final Config config;

    public StatusConnectionHandler(Translation translation, Config config) {
        this.translation = translation;
        this.config = config;
    }

    public void postApiConnectionUpdate(JDA api, ServerInfo info) {
        boolean apiStatus = Boolean.TRUE.equals(new ConnectionTable().queryFromTable("api").status());

        if (info != null && info.getSuccess()) {
            new ConnectionTable().databaseNotExists("api", true);
        } else {
            new ConnectionTable().databaseNotExists("api", false);
        }

        if (info != null && info.getSuccess()) {
            logger.debug("Connection to api successful");
            if (apiStatus && GlobalVariables.statusApiSessionStatus) return;
            if (config.status().postServerStatus() && !apiStatus) {
                new StatusMessageHandler(config, translation).postConnectionEstablished(api, info);
            }
            new ConnectionTable().postConnectionToDatabase("api", true);
            retryFetchData = 0;
            GlobalVariables.statusApiSessionStatus = true;
            logger.info("Regained connection to secretlab api");
        } else {
            logger.debug("Connection to api failed");
            if (!apiStatus) return;
            if (retryFetchData == config.status().retryToFetchData() + 1) return;
            if (retryFetchData == config.status().retryToFetchData()) {
                String errorMessage = "No json body was returned for serialization";
                if (info != null && info.getError() != null) errorMessage = info.getError();
                logger.error("SecretLabApi connection lost, request returned unsuccessful, {}", errorMessage);
                if (config.status().postServerStatus()) {
                    new StatusMessageHandler(config, translation).postConnectionLost(api, config.status().retryToFetchData());
                }
                retryFetchData += 1;
                new ConnectionTable().postConnectionToDatabase("api", false);
                return;
            }
            if (retryFetchData >= config.status().suspectRateLimitUntil() && retryFetchData != config.status().retryToFetchData() + 1) {
                logger.warn("Failed {} consecutive times to connect to the api. Suspecting an api outage or invalid key. Retrying {} more times",
                        retryFetchData, config.status().retryToFetchData() - retryFetchData);
            }
            if (retryFetchData < config.status().suspectRateLimitUntil()) {
                logger.warn("Failed to access the secretlab api, suspecting rate limiting or small outage, retrying in {} seconds",
                        config.status().checkRate());
            }
            retryFetchData += 1;
            GlobalVariables.statusApiSessionStatus = false;
        }
    }

    public void postStatusUpdate(Server server, JDA api, Instance instance, ServerInfo info) {
        new ConnectionTable().databaseNotExists(String.valueOf(server.getPort()), server.getOnline());
        reconnectAttempt.putIfAbsent(server.getPort(), 0);

        boolean serverStatus = Boolean.TRUE.equals(new ConnectionTable().queryFromTable(String.valueOf(server.getPort())).status());
        GlobalVariables.statusServerSessionStatus.putIfAbsent(server.getPort(), serverStatus);

        if (server.getOnline()) {
            logger.debug("Connection to server {} ({}), established and returned online", instance.name(), instance.serverPort());
            if (serverStatus && Boolean.TRUE.equals(GlobalVariables.statusServerSessionStatus.get(server.getPort()))) return;
            if (config.status().postServerStatus() && !serverStatus) {
                new StatusMessageHandler(config, translation).postConnectionOnline(api, instance, info);
            }
            reconnectAttempt.put(server.getPort(), 0);
            GlobalVariables.statusServerSessionStatus.put(server.getPort(), true);
            new ConnectionTable().postConnectionToDatabase(String.valueOf(server.getPort()), true);
            logger.info("Connection to server {} ({}) regained", instance.name(), instance.serverPort());
        } else {
            logger.debug("Connection to server {} ({}), not established and returned offline", instance.name(), instance.serverPort());
            if (!serverStatus) return;

            int currentRetry = reconnectAttempt.getOrDefault(server.getPort(), 0);
            if (currentRetry == instance.retries() + 1) return;
            if (currentRetry == instance.retries()) {
                logger.warn("Completely lost connection to server - {}. Server is probably offline/unreachable", instance.name());

                if (config.status().postServerStatus()) {
                    new StatusMessageHandler(config, translation).postConnectionOffline(api, instance, info);
                }
                reconnectAttempt.put(server.getPort(), instance.retries() + 1);
                new ConnectionTable().postConnectionToDatabase(String.valueOf(server.getPort()), false);
                return;
            }
            logger.warn("Failed to query data from \"{}\", trying to reconnect {} more times",
                    instance.name(), instance.retries() - currentRetry);
            reconnectAttempt.put(server.getPort(), currentRetry + 1);
            GlobalVariables.statusServerSessionStatus.put(server.getPort(), false);
        }
    }
}

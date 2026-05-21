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

package dev.vxrp.bot.status;

import dev.vxrp.bot.commands.CommandManager;
import dev.vxrp.bot.commands.listeners.StatusCommandListener;
import dev.vxrp.bot.status.data.Instance;
import dev.vxrp.bot.status.data.Status;
import dev.vxrp.bot.status.handler.StatusActivityHandler;
import dev.vxrp.bot.status.handler.StatusConnectionHandler;
import dev.vxrp.bot.status.handler.StatusPlayerlistHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ConnectionTable;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.coroutines.ExecutorScopes;
import dev.vxrp.util.coroutines.Timer;
import io.github.vxrpenter.secretlab.SecretLab;
import io.github.vxrpenter.secretlab.data.Server;
import io.github.vxrpenter.secretlab.data.ServerInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatusManager {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(StatusManager.class);
    private final JDA globalApi;
    private final Config config;
    private final Translation translation;
    private final File currentFile;

    private volatile int secondsWithoutNewData = 0;
    private volatile boolean nonChangedData = false;
    private volatile Map<String, Server> instanceToServerMap = null;

    public StatusManager(JDA globalApi, Config config, Translation translation, String file) {
        this.globalApi = globalApi;
        this.config = config;
        this.translation = translation;
        this.currentFile = new File(System.getProperty("user.dir"), file);

        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
                InputStream content = StatusManager.class.getResourceAsStream("/" + file);
                if (content != null) {
                    Files.write(currentFile.toPath(), content.readAllBytes());
                }
            } catch (IOException e) {
                logger.error("Failed to create status file", e);
            }
        }
    }

    public void initialize(CommandManager commandManager) {
        if (!config.status().active()) return;

        ExecutorScopes.defaultStatusScope.submit(() -> initializeBots(config.status(), commandManager));
    }

    private void initializeBots(Status status, CommandManager commandManager) {
        if (status.instances().isEmpty()) return;

        Map<Instance, JDA> instanceApiMapping = new HashMap<>();

        for (Instance instance : status.instances()) {
            String key = instance.instanceKey(status.api(), status.accountId());
            new ConnectionTable().insertIfNotExists(key, true, false);

            try {
                JDA newApi = JDABuilder.createLight(instance.token(), EnumSet.noneOf(GatewayIntent.class))
                        .setActivity(Activity.playing("pending..."))
                        .build()
                        .awaitReady();

                logger.info("Starting up status-bot: {}", newApi.getSelfUser().getId());

                GlobalVariables.statusMappedBots.put(newApi.getSelfUser().getId(), key);
                GlobalVariables.statusInstances.put(key, instance);

                newApi.addEventListener(new StatusCommandListener(newApi, config, translation));

                initializeCommands(commandManager, newApi);
                instanceApiMapping.put(instance, newApi);
            } catch (Exception e) {
                logger.error("Failed to start status bot for instance: {}", instance.name(), e);
            }
        }

        initializeTimers(status, instanceApiMapping);
    }

    private void initializeCommands(CommandManager commandManager, JDA api) {
        commandManager.registerSpecificCommands(config.extra().commands().statusCommands(), api);
    }

    private void initializeTimers(Status status, Map<Instance, JDA> instanceApiMap) {
        new Timer().runWithTimer(Duration.ofSeconds(1), ExecutorScopes.statusbotScope, () -> {
            if (nonChangedData && status.idleAfter() != secondsWithoutNewData) {
                secondsWithoutNewData += 1;
            }
        });

        new Timer().runLooped(ExecutorScopes.statusbotScope, () -> runTimer(status, instanceApiMap));
    }

    private void runTimer(Status status, Map<Instance, JDA> instanceApiMap) {
        try {
            if (secondsWithoutNewData == status.idleAfter()) {
                logger.debug("Data hasn't changed for the last {} seconds, using check rate of {} seconds", status.idleAfter(), status.idleCheckRate());
                runStatusChange(status, instanceApiMap);
                Thread.sleep(status.idleCheckRate() * 1000L);
            } else {
                runStatusChange(status, instanceApiMap);
                Thread.sleep(status.checkRate() * 1000L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void runStatusChange(Status status, Map<Instance, JDA> instanceApiMap) {
        Map<String, List<Instance>> credentialGroups = new LinkedHashMap<>();
        for (Instance instance : status.instances()) {
            String credKey = instance.credentialKey(status.api(), status.accountId());
            credentialGroups.computeIfAbsent(credKey, k -> new ArrayList<>()).add(instance);
        }

        Map<String, Server> currentInstanceToServerMap = new HashMap<>();
        Map<String, ServerInfo> instanceKeyToInfo = new HashMap<>();
        boolean anyDataReceived = false;

        for (Map.Entry<String, List<Instance>> entry : credentialGroups.entrySet()) {
            Instance first = entry.getValue().get(0);
            String effectiveApi = first.effectiveApi(status.api());
            String effectiveAccountId = first.effectiveAccountId(status.accountId());
            List<Integer> ports = entry.getValue().stream().map(Instance::serverPort).toList();

            ApiFetchResult content = fetchData(effectiveApi, effectiveAccountId, ports);

            String credKey = entry.getKey();
            new StatusConnectionHandler(translation, config).postApiConnectionUpdate(
                    globalApi, content != null ? content.info : null, credKey);

            if (content != null) {
                for (Instance instance : entry.getValue()) {
                    String instKey = instance.instanceKey(status.api(), status.accountId());
                    Server server = content.portToServerMap.get(instance.serverPort());
                    GlobalVariables.statusMappedServers.put(instKey, server);
                    currentInstanceToServerMap.put(instKey, server);
                    instanceKeyToInfo.put(instKey, content.info);
                }
                anyDataReceived = true;
            }
        }

        if (!anyDataReceived) {
            logger.error("Could not receive data for status-bots, skipping iteration");
            return;
        }

        if (mapsEqual(this.instanceToServerMap, currentInstanceToServerMap)) {
            nonChangedData = true;
        } else {
            secondsWithoutNewData = 0;
            this.instanceToServerMap = currentInstanceToServerMap;
            nonChangedData = false;
        }

        if (status.checkPlayerlist()) {
            new StatusPlayerlistHandler(config, translation).updatePlayerLists(
                    currentInstanceToServerMap, status.instances(), instanceApiMap, status.api(), status.accountId());
        }

        for (Instance instance : status.instances()) {
            JDA api = instanceApiMap.get(instance);
            if (api == null) continue;

            api.getPresence().setStatus(OnlineStatus.IDLE);

            String instKey = instance.instanceKey(status.api(), status.accountId());
            Server server = currentInstanceToServerMap.get(instKey);
            ServerInfo info = instanceKeyToInfo.get(instKey);
            if (server != null && info != null) {
                spinUpChecker(api, server, instance, info, instKey);
            }
        }
    }

    private boolean mapsEqual(Map<String, Server> a, Map<String, Server> b) {
        if (a == null || b == null) return a == b;
        return a.equals(b);
    }

    private static class ApiFetchResult {
        final ServerInfo info;
        final Map<Integer, Server> portToServerMap;

        ApiFetchResult(ServerInfo info, Map<Integer, Server> portToServerMap) {
            this.info = info;
            this.portToServerMap = portToServerMap;
        }
    }

    private ApiFetchResult fetchData(String api, String accountId, List<Integer> ports) {
        SecretLab secretLab = new SecretLab(api, accountId, 60, 60);

        Map<Integer, Server> portToServerMap = new HashMap<>();
        try {
            ServerInfo info = secretLab.serverInfo(false, true, true, false, false, false, false, false, false);
            for (int port : ports) {
                Server server = serverByPort(port, info);
                portToServerMap.put(port, server);
            }
            return new ApiFetchResult(info, portToServerMap);
        } catch (Exception e) {
            logger.error("Could not process secret lab request correctly", e.getCause());
            return null;
        }
    }

    private Server serverByPort(int port, ServerInfo info) {
        if (info == null || info.getServers() == null) return null;
        for (Server server : info.getServers()) {
            if (server.getPort() == port) return server;
        }
        return null;
    }

    private void spinUpChecker(JDA api, Server server, Instance instance, ServerInfo info, String instanceKey) {
        new StatusActivityHandler(translation, config).updateStatus(api, server, instance, instanceKey);
        new StatusConnectionHandler(translation, config).postStatusUpdate(server, api, instance, info, instanceKey);
    }
}

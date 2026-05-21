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
import dev.vxrp.bot.status.enums.PlayerlistType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.data.StatusDatabaseEntry;
import dev.vxrp.database.tables.database.StatusTable;
import dev.vxrp.util.GlobalVariables;
import io.github.vxrpenter.secretlab.data.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusPlayerlistHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(StatusPlayerlistHandler.class);
    private final Config config;
    private final Translation translation;

    public StatusPlayerlistHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void updatePlayerLists(Map<Integer, Server> portToServerMap, List<Instance> instances, Map<Instance, JDA> instanceApiMap) {
        for (Map.Entry<Integer, Server> server : portToServerMap.entrySet()) {
            if (server.getValue() == null) {
                Instance inst = GlobalVariables.statusInstances.get(server.getKey());
                logger.debug("No data for servers received, skipping message for server {} ({})",
                        inst != null ? inst.name() : "unknown", server.getKey());
                return;
            }

            JDA api = null;
            for (Instance instance : instances) {
                if (instance.serverPort() != server.getKey()) continue;
                api = instanceApiMap.get(instance);
                if (api == null) {
                    logger.error("Could not retrieve mapped bot for port: {}", server);
                    return;
                }
                break;
            }
            if (api == null) continue;

            createPresetMessage(api, server);
            updateMessage(api, server);
        }
    }

    private void updateMessage(JDA api, Map.Entry<Integer, Server> port) {
        StatusTable statusTable = new StatusTable();
        for (StatusDatabaseEntry entry : statusTable.getAllEntries()) {
            List<MessageEmbed> embeds = new ArrayList<>();

            if (GlobalVariables.statusMappedServers.get(port.getKey()) != null) {
                MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler()
                        .getEmbed(api.getSelfUser().getId(), translation);
                if (playerListEmbed != null) {
                    embeds.add(playerListEmbed);
                }
            }

            try {
                if (api.getTextChannelById(entry.channelId()) != null) {
                    api.getTextChannelById(entry.channelId())
                            .editMessageEmbedsById(entry.messageId(), embeds)
                            .complete();
                }
            } catch (ErrorResponseException e) {
                statusTable.deleteFromDatabase(String.valueOf(port.getKey()));
            }

            logger.debug("Updated playerlist with message id: {} in channel {} part of server {}", entry.messageId(), entry.channelId(), port.getKey());
        }

        statusTable.updateLastUpdated(String.valueOf(port.getKey()), String.valueOf(System.currentTimeMillis()));
    }

    private void createPresetMessage(JDA api, Map.Entry<Integer, Server> port) {
        for (Instance instance : config.status().instances()) {
            if (instance.serverPort() != port.getKey()) continue;
            if (!instance.playerlist().active()) continue;

            PlayerlistType playerlistType = new StatusTable().getType(String.valueOf(port.getKey()));
            if (playerlistType != PlayerlistType.PRESET) {
                logger.debug("Skipping over preset creation for server '{}'", instance.name());
                return;
            }

            for (String channelId : instance.playerlist().channelId()) {
                var channel = api.getTextChannelById(channelId);
                if (channel == null) {
                    logger.error("Could not find channel '{}' to paste preset playerlist of server '{}'", channelId, instance.name());
                    return;
                }

                List<MessageEmbed> embeds = new ArrayList<>();
                if (GlobalVariables.statusMappedServers.get(port.getKey()) != null) {
                    MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler()
                            .getEmbed(api.getSelfUser().getId(), translation);
                    if (playerListEmbed != null) {
                        embeds.add(playerListEmbed);
                    }
                }

                var message = channel.sendMessageEmbeds(embeds).complete();

                new StatusTable().addToDatabase(PlayerlistType.PRESET, channelId, message.getId(),
                        String.valueOf(port.getKey()), LocalDate.now().toString(), String.valueOf(System.currentTimeMillis()));
            }
            break;
        }
    }
}

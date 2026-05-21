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
import java.util.concurrent.ConcurrentHashMap;

public class StatusPlayerlistHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(StatusPlayerlistHandler.class);
    private final Config config;
    private final Translation translation;
    private static final int MAX_CONSECUTIVE_ERRORS = 3;
    private final Map<String, Integer> consecutiveErrors = new ConcurrentHashMap<>();

    public StatusPlayerlistHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void updatePlayerLists(Map<String, Server> instanceToServerMap, List<Instance> instances,
                                  Map<Instance, JDA> instanceApiMap, String globalApi, String globalAccountId) {
        for (Instance instance : instances) {
            String instKey = instance.instanceKey(globalApi, globalAccountId);
            Server server = instanceToServerMap.get(instKey);

            if (server == null) {
                logger.debug("No data for servers received, skipping message for server {} ({})",
                        instance.name(), instKey);
                continue;
            }

            JDA api = instanceApiMap.get(instance);
            if (api == null) {
                logger.error("Could not retrieve mapped bot for key: {}", instKey);
                continue;
            }

            createPresetMessage(api, instKey, instance);
            updateMessage(api, instKey, instance);
        }
    }

    private void updateMessage(JDA api, String instanceKey, Instance instance) {
        StatusTable statusTable = new StatusTable();
        for (StatusDatabaseEntry entry : statusTable.getAllEntries()) {
            if (!entry.port().equals(instanceKey)) continue;
            List<MessageEmbed> embeds = new ArrayList<>();

            if (GlobalVariables.statusMappedServers.get(instanceKey) != null) {
                MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler()
                        .getEmbed(api.getSelfUser().getId(), translation);
                if (playerListEmbed != null) {
                    embeds.add(playerListEmbed);
                }
            }

            String errorKey = entry.channelId() + ":" + entry.messageId();

            try {
                if (api.getTextChannelById(entry.channelId()) != null) {
                    api.getTextChannelById(entry.channelId())
                            .editMessageEmbedsById(entry.messageId(), embeds)
                            .complete();
                }
                consecutiveErrors.remove(errorKey);
            } catch (ErrorResponseException e) {
                int errors = consecutiveErrors.merge(errorKey, 1, Integer::sum);
                if (errors >= MAX_CONSECUTIVE_ERRORS) {
                    logger.warn("Failed to update playerlist message {} in channel {} for server {} {} times consecutively, removing entry from database",
                            entry.messageId(), entry.channelId(), instanceKey, errors, e);
                    statusTable.deleteEntry(entry.channelId(), entry.messageId());
                    consecutiveErrors.remove(errorKey);
                } else {
                    logger.warn("Failed to update playerlist message {} in channel {} for server {} (attempt {}/{})",
                            entry.messageId(), entry.channelId(), instanceKey, errors, MAX_CONSECUTIVE_ERRORS, e);
                }
                continue;
            }

            logger.debug("Updated playerlist with message id: {} in channel {} part of server {}", entry.messageId(), entry.channelId(), instanceKey);
        }

        statusTable.updateLastUpdated(instanceKey, String.valueOf(System.currentTimeMillis()));
    }

    private void createPresetMessage(JDA api, String instanceKey, Instance instance) {
        if (!instance.playerlist().active()) return;

        PlayerlistType playerlistType = new StatusTable().getType(instanceKey);
        if (playerlistType == PlayerlistType.PRESET) {
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
            if (GlobalVariables.statusMappedServers.get(instanceKey) != null) {
                MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler()
                        .getEmbed(api.getSelfUser().getId(), translation);
                if (playerListEmbed != null) {
                    embeds.add(playerListEmbed);
                }
            }

            var message = channel.sendMessageEmbeds(embeds).complete();

            new StatusTable().addToDatabase(PlayerlistType.PRESET, channelId, message.getId(),
                    instanceKey, LocalDate.now().toString(), String.valueOf(System.currentTimeMillis()));
        }
    }
}

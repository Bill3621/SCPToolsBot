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
import dev.vxrp.bot.status.data.PlayerList;
import dev.vxrp.bot.status.enums.PlayerlistType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.data.StatusDatabaseEntry;
import dev.vxrp.database.tables.database.StatusTable;
import dev.vxrp.util.GlobalVariables;
import io.github.vxrpenter.secretlab.data.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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
    private static final String GLOBAL_PLAYERLIST_KEY = "__global_playerlist__";
    private final Map<String, Integer> consecutiveErrors = new ConcurrentHashMap<>();

    public StatusPlayerlistHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void updatePlayerLists(Map<String, Server> instanceToServerMap, List<Instance> instances, Map<Instance, JDA> instanceApiMap, JDA globalBotApi, String globalApi,
            String globalAccountId) {
        PlayerList globalPlayerlist = config.status().globalPlayerlist();
        if (globalPlayerlist != null && globalPlayerlist.active()) {
            updateGlobalPlayerList(globalBotApi, instanceToServerMap, instances, globalPlayerlist, globalApi, globalAccountId);
            return;
        }

        for (Instance instance : instances) {
            String instKey = instance.instanceKey(globalApi, globalAccountId);
            Server server = instanceToServerMap.get(instKey);

            if (server == null) {
                logger.debug("No data for servers received, skipping message for server {} ({})", instance.name(), instKey);
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

    private void updateGlobalPlayerList(JDA api, Map<String, Server> servers, List<Instance> instances, PlayerList playerlist, String globalApi, String globalAccountId) {
        StatusTable statusTable = new StatusTable();
        MessageCreateData data = new MessageCreateBuilder().useComponentsV2().setComponents(globalPlayerListComponents(servers, instances, globalApi, globalAccountId)).build();
        var entries = statusTable.getAllEntries().stream().filter(entry -> entry.port().equals(GLOBAL_PLAYERLIST_KEY)).toList();

        for (String channelId : playerlist.channelId()) {
            if (entries.stream().anyMatch(entry -> entry.channelId().equals(channelId)))
                continue;
            var channel = api.getTextChannelById(channelId);
            if (channel == null) {
                logger.error("Could not find channel '{}' to paste the global playerlist", channelId);
                continue;
            }

            var message = channel.sendMessage(data).complete();
            statusTable.addToDatabase(PlayerlistType.PRESET, channelId, message.getId(), GLOBAL_PLAYERLIST_KEY, LocalDate.now().toString(),
                    String.valueOf(System.currentTimeMillis()));
        }

        var editData = MessageEditBuilder.fromCreateData(data).build();
        for (StatusDatabaseEntry entry : entries) {
            String errorKey = entry.channelId() + ":" + entry.messageId();

            try {
                var channel = api.getTextChannelById(entry.channelId());
                if (channel != null) {
                    channel.editMessageById(entry.messageId(), editData).complete();
                }
                consecutiveErrors.remove(errorKey);
            } catch (ErrorResponseException e) {
                int errors = consecutiveErrors.merge(errorKey, 1, Integer::sum);
                if (errors >= MAX_CONSECUTIVE_ERRORS) {
                    logger.warn("Failed to update global playerlist message {} in channel {} {} times consecutively, removing entry from database", entry.messageId(),
                            entry.channelId(), errors, e);
                    statusTable.deleteEntry(entry.channelId(), entry.messageId());
                    consecutiveErrors.remove(errorKey);
                } else {
                    logger.warn("Failed to update global playerlist message {} in channel {} (attempt {}/{})", entry.messageId(), entry.channelId(), errors, MAX_CONSECUTIVE_ERRORS,
                            e);
                }
            }
        }

        statusTable.updateLastUpdated(GLOBAL_PLAYERLIST_KEY, String.valueOf(System.currentTimeMillis()));
    }

    static List<MessageTopLevelComponent> globalPlayerListComponents(Map<String, Server> servers, List<Instance> instances, String globalApi, String globalAccountId) {
        String header = "## Server Network\nUpdated <t:" + Instant.now().getEpochSecond() + ":R>";
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of(header));

        int treeSize = 2;
        int textLength = header.length();
        int rendered = 0;
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            Server server = servers.get(instance.instanceKey(globalApi, globalAccountId));
            boolean online = server != null && server.getOnline();
            String version = server != null && server.getVersion() != null ? server.getVersion() : "Not fetched";
            String players = server != null && server.getPlayers() != null ? server.getPlayers() : "0/0";
            String summary = "### " + instance.name() + "\n" + (online ? "🟢 Online" : "🔴 Unavailable") + " · Version `" + version + "`";

            String names = playerNames(server, Math.min(500, 3900 - textLength - summary.length()));
            int componentCost = names.isEmpty() ? 4 : 5;
            if (treeSize + componentCost > 39 || textLength + summary.length() + names.length() > 3900)
                break;

            content.add(Separator.createDivider(Separator.Spacing.SMALL));
            Section section = Section.of(Button.secondary("global-playerlist-" + i, players).asDisabled(), TextDisplay.of(summary));
            content.add(section);
            if (!names.isEmpty()) {
                content.add(TextDisplay.of("**Players online**\n> " + names.replace("\n", "\n> ")));
            }
            treeSize += componentCost;
            textLength += summary.length() + names.length() + (names.isEmpty() ? 0 : 21);
            rendered++;
        }

        if (rendered < instances.size()) {
            content.add(TextDisplay.of("*" + (instances.size() - rendered) + " more server(s) omitted by Discord's message limits.*"));
        }
        return List.of(Container.of(content));
    }

    private static String playerNames(Server server, int maxLength) {
        if (server == null || server.getPlayerList() == null || maxLength < 4)
            return "";

        StringBuilder names = new StringBuilder();
        for (var player : server.getPlayerList()) {
            String name = String.valueOf(player.getNickname()).replace('`', '\'');
            int required = name.length() + (names.isEmpty() ? 0 : 1);
            if (names.length() + required > maxLength) {
                if (names.length() + 4 <= maxLength)
                    names.append("\n...");
                break;
            }
            if (!names.isEmpty())
                names.append('\n');
            names.append(name);
        }
        return names.toString();
    }

    private void updateMessage(JDA api, String instanceKey, Instance instance) {
        StatusTable statusTable = new StatusTable();
        for (StatusDatabaseEntry entry : statusTable.getAllEntries()) {
            if (!entry.port().equals(instanceKey))
                continue;
            List<MessageEmbed> embeds = new ArrayList<>();

            if (GlobalVariables.statusMappedServers.get(instanceKey) != null) {
                MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler().getEmbed(api.getSelfUser().getId(), translation);
                if (playerListEmbed != null) {
                    embeds.add(playerListEmbed);
                }
            }

            String errorKey = entry.channelId() + ":" + entry.messageId();

            try {
                if (api.getTextChannelById(entry.channelId()) != null) {
                    api.getTextChannelById(entry.channelId()).editMessageEmbedsById(entry.messageId(), embeds).complete();
                }
                consecutiveErrors.remove(errorKey);
            } catch (ErrorResponseException e) {
                int errors = consecutiveErrors.merge(errorKey, 1, Integer::sum);
                if (errors >= MAX_CONSECUTIVE_ERRORS) {
                    logger.warn("Failed to update playerlist message {} in channel {} for server {} {} times consecutively, removing entry from database", entry.messageId(),
                            entry.channelId(), instanceKey, errors, e);
                    statusTable.deleteEntry(entry.channelId(), entry.messageId());
                    consecutiveErrors.remove(errorKey);
                } else {
                    logger.warn("Failed to update playerlist message {} in channel {} for server {} (attempt {}/{})", entry.messageId(), entry.channelId(), instanceKey, errors,
                            MAX_CONSECUTIVE_ERRORS, e);
                }
                continue;
            }

            logger.debug("Updated playerlist with message id: {} in channel {} part of server {}", entry.messageId(), entry.channelId(), instanceKey);
        }

        statusTable.updateLastUpdated(instanceKey, String.valueOf(System.currentTimeMillis()));
    }

    private void createPresetMessage(JDA api, String instanceKey, Instance instance) {
        if (!instance.playerlist().active())
            return;

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
                MessageEmbed playerListEmbed = new dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler().getEmbed(api.getSelfUser().getId(), translation);
                if (playerListEmbed != null) {
                    embeds.add(playerListEmbed);
                }
            }

            var message = channel.sendMessageEmbeds(embeds).complete();

            new StatusTable().addToDatabase(PlayerlistType.PRESET, channelId, message.getId(), instanceKey, LocalDate.now().toString(), String.valueOf(System.currentTimeMillis()));
        }
    }
}

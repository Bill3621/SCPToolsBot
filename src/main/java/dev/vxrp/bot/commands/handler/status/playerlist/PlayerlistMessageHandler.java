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

package dev.vxrp.bot.commands.handler.status.playerlist;

import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import io.github.vxrpenter.secretlab.data.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;

public class PlayerlistMessageHandler {
    public MessageEmbed getEmbed(String botId, Translation translation) {
        StringBuilder builder = new StringBuilder();
        String instanceKey = GlobalVariables.statusMappedBots.get(botId);
        Server server = instanceKey != null ? GlobalVariables.statusMappedServers.get(instanceKey) : null;

        if (server != null && server.getPlayerList() != null) {
            var list = server.getPlayerList();
            if (list.isEmpty()) builder.append(translation.status().embedPlayerlistEmpty());
            for (var player : list) {
                builder.append(
                        new ColorTool().parse(translation.status().embedPlayerlistPlayer()
                                .replace("%nickname%", String.valueOf(player.getNickname()))));
            }
        } else {
            builder.append(new ColorTool().parse(translation.status().embedPlayerlistCouldntFetch()).trim());
        }

        int embedColor = 0xE74D3C;
        if (server != null && server.getOnline()) {
            embedColor = 0x2ECC70;
        }

        String version = server != null ? server.getVersion() : "&red&&bold&Not Fetched";
        String players = server != null && server.getPlayers() != null ? server.getPlayers().split("/")[0] : "0";
        String ff = server != null ? String.valueOf(server.getFf()) : "&red&&bold&Not Fetched";
        String wl = server != null ? String.valueOf(server.getWl()) : "&red&&bold&Not Fetched";
        String modded = server != null ? String.valueOf(server.getModded()) : "&red&&bold&Not Fetched";

        String description = translation.status().embedPlayerlistBody()
                .replace("%players%", builder.toString())
                .replace("%version%", version)
                .replace("%player_number%", players)
                .replace("%ff%", ff)
                .replace("%wl%", wl)
                .replace("%modded%", modded)
                .replace("true", "&green&&bold&true")
                .replace("false", "&red&&bold&false");

        return new EmbedBuilder()
                .setColor(embedColor)
                .setTitle(new ColorTool().parse(translation.status().embedPlayerlistTitle()).trim())
                .setDescription(new ColorTool().parse(description))
                .setTimestamp(Instant.now())
                .build();
    }
}

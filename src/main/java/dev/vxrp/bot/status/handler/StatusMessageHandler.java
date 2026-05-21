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
import dev.vxrp.util.color.ColorTool;
import io.github.vxrpenter.secretlab.data.ServerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public class StatusMessageHandler {
    private final Config config;
    private final Translation translation;

    public StatusMessageHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void postConnectionEstablished(JDA api, ServerInfo info) {
        ColorTool colorTool = new ColorTool();

        var embed = new EmbedBuilder()
                .setColor(0x2ECC70)
                .setUrl(config.status().pageUrl())
                .setTitle(colorTool.parse(translation.status().embedEstablishedTitle()).replace("%instance%", "Status Server System"))
                .setDescription(colorTool.parse(translation.status().embedEstablishedBody()))
                .addField(
                        colorTool.parse(translation.status().embedEstablishedResponseFieldName()),
                        colorTool.parse(translation.status().embedEstablishedResponseFieldValue().replace("%time%", String.valueOf(info.getResponse()))),
                        false
                )
                .addField(
                        colorTool.parse(translation.status().embedEstablishedReasonFieldName()),
                        colorTool.parse(translation.status().embedEstablishedReasonFieldValue()),
                        false
                )
                .build();

        if (api.getTextChannelById(config.status().postChannel()) != null) {
            api.getTextChannelById(config.status().postChannel()).sendMessageEmbeds(embed).queue();
        }
    }

    public void postConnectionLost(JDA api, int retry) {
        ColorTool colorTool = new ColorTool();

        var embed = new EmbedBuilder()
                .setColor(0xE74D3C)
                .setUrl(config.status().pageUrl())
                .setTitle(colorTool.parse(translation.status().embedLostTitle()).replace("%instance%", "Status Server System"))
                .setDescription(colorTool.parse(translation.status().embedLostBody().replace("%retries%", String.valueOf(retry))))
                .addField(
                        colorTool.parse(translation.status().embedEstablishedResponseFieldName()),
                        colorTool.parse(translation.status().embedEstablishedResponseFieldValue().replace("%time%", "Unknown")),
                        false
                )
                .addField(
                        colorTool.parse(translation.status().embedLostReasonFieldName()),
                        colorTool.parse(translation.status().embedLostReasonFieldValue()),
                        false
                )
                .build();

        if (api.getTextChannelById(config.status().postChannel()) != null) {
            api.getTextChannelById(config.status().postChannel()).sendMessageEmbeds(embed).queue();
        }
    }

    public void postConnectionOnline(JDA api, Instance instance, ServerInfo info) {
        ColorTool colorTool = new ColorTool();

        String responseTime = info != null ? String.valueOf(info.getResponse()) : "Unknown";

        var embed = new EmbedBuilder()
                .setColor(0x2ECC70)
                .setUrl(config.status().pageUrl())
                .setTitle(colorTool.parse(translation.status().embedOnlineTitle()).replace("%instance%", instance.name()))
                .setDescription(colorTool.parse(translation.status().embedOnlineBody()))
                .addField(
                        colorTool.parse(translation.status().embedOnlineResponseFieldName()),
                        colorTool.parse(translation.status().embedOnlineResponseFieldValue().replace("%time%", responseTime)),
                        false
                )
                .addField(
                        colorTool.parse(translation.status().embedOnlineReasonFieldName()),
                        colorTool.parse(translation.status().embedOnlineReasonFieldValue()),
                        false
                )
                .build();

        if (api.getTextChannelById(config.status().postChannel()) != null) {
            api.getTextChannelById(config.status().postChannel()).sendMessageEmbeds(embed).queue();
        }
    }

    public void postConnectionOffline(JDA api, Instance instance, ServerInfo info) {
        ColorTool colorTool = new ColorTool();

        String responseTime = info != null ? String.valueOf(info.getResponse()) : "Unknown";

        var embed = new EmbedBuilder()
                .setColor(0xE74D3C)
                .setUrl(config.status().pageUrl())
                .setTitle(colorTool.parse(translation.status().embedOfflineTitle()).replace("%instance%", instance.name()))
                .setDescription(colorTool.parse(translation.status().embedOfflineBody().replace("%retries%", String.valueOf(instance.retries()))))
                .addField(
                        colorTool.parse(translation.status().embedOfflineResponseFieldName()),
                        colorTool.parse(translation.status().embedOfflineResponseFieldValue().replace("%time%", responseTime)),
                        false
                )
                .addField(
                        colorTool.parse(translation.status().embedOfflineReasonFieldName()),
                        colorTool.parse(translation.status().embedOfflineReasonFieldValue()),
                        false
                )
                .build();

        if (api.getTextChannelById(config.status().postChannel()) != null) {
            api.getTextChannelById(config.status().postChannel()).sendMessageEmbeds(embed).queue();
        }
    }
}

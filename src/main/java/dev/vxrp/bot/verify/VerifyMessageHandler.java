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

package dev.vxrp.bot.verify;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.LoggerFactory;

public class VerifyMessageHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(VerifyMessageHandler.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public VerifyMessageHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void sendTemplate(TextChannel channel, Guild guild) {
        ColorTool colorTool = new ColorTool();
        MessageEmbed embed = new EmbedBuilder()
                .setThumbnail(guild.getIconUrl())
                .setTitle(colorTool.parse(translation.verify().embedTemplateTitle()))
                .setDescription(colorTool.parse(translation.verify().embedTemplateBody()))
                .build();

        channel.sendMessageEmbeds(embed).setActionRow(
                Button.link(config.settings().verify().oauthLink(), translation.buttons().textVerifyVerify()),
                Button.secondary("verify_show_data", translation.buttons().textVerifyShowData()),
                Button.danger("verify_delete", translation.buttons().textVerifyDelete())
        ).queue();
    }

    public void sendVerificationMessage(User user) {
        ColorTool colorTool = new ColorTool();
        MessageEmbed embed = new EmbedBuilder()
                .setColor(0x2ECC70)
                .setThumbnail(user.getAvatarUrl())
                .setTitle(colorTool.parse(translation.verify().embedLogVerifiedTitle()
                        .replace("%name%", String.valueOf(user.getGlobalName()))))
                .setDescription(colorTool.parse(translation.verify().embedLogVerifiedBody()))
                .build();

        sendMessage(embed);
    }

    public void sendDeletionMessage(User user) {
        ColorTool colorTool = new ColorTool();
        MessageEmbed embed = new EmbedBuilder()
                .setColor(0xE74D3C)
                .setThumbnail(user.getAvatarUrl())
                .setTitle(colorTool.parse(translation.verify().embedLogDeletedTitle()
                        .replace("%name%", String.valueOf(user.getGlobalName()))))
                .setDescription(colorTool.parse(translation.verify().embedLogDeletedBody()))
                .build();

        sendMessage(embed);
    }

    private void sendMessage(MessageEmbed embed) {
        TextChannel channel = api.getTextChannelById(config.settings().verify().verifyLogChannel());
        if (channel == null) {
            logger.error("Could not correctly retrieve verify log channel, does it exist?");
            return;
        }
        channel.sendMessageEmbeds(embed).queue();
    }
}

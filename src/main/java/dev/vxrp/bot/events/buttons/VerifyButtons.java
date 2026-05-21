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

package dev.vxrp.bot.events.buttons;

import dev.vxrp.bot.verify.VerifyMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.UserTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class VerifyButtons {
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public VerifyButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        var noDataEmbed = new EmbedBuilder()
                .setColor(0xE74D3C)
                .setTitle(new ColorTool().parse(translation.verify().embedNoDataTitle()))
                .setDescription(new ColorTool().parse(translation.verify().embedNoDataBody()))
                .build();

        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        if (buttonId.startsWith("verify_show_data")) {
            if (!new UserTable().exists(event.getUser().getId())) {
                event.replyEmbeds(noDataEmbed).setEphemeral(true).queue();
                return;
            }

            String verified = String.valueOf(new UserTable().exists(event.getUser().getId()));
            if ("true".equals(verified)) {
                verified = "🟢 " + verified;
            } else {
                verified = "🔴 " + verified;
            }

            String steamId = new UserTable().getSteamId(event.getUser().getId());
            String timestamp = new UserTable().getVerifyTime(event.getUser().getId());

            var embed = new EmbedBuilder()
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setTitle(new ColorTool().parse(translation.verify().embedDataTitle()))
                    .setDescription(new ColorTool().parse(translation.verify().embedDataBody()))
                    .addField(translation.verify().embedDataFieldVerifiedTitle(), verified, true)
                    .addField(translation.verify().embedDataFieldSteamIdTitle(), steamId != null ? steamId : "Unknown", true)
                    .addField(translation.verify().embedDataFieldTimestampTitle(), timestamp != null ? timestamp : "Unknown", true)
                    .addField("", translation.verify().embedDataFieldDeleteValue(), false)
                    .build();

            event.replyEmbeds(embed).setEphemeral(true).queue();
        }

        if (buttonId.startsWith("verify_delete")) {
            if (!new UserTable().exists(event.getUser().getId())) {
                event.replyEmbeds(noDataEmbed).setEphemeral(true).queue();
                return;
            }

            new UserTable().delete(event.getUser().getId());

            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.verify().embedDeletionSentTitle()))
                    .setDescription(new ColorTool().parse(translation.verify().embedDeletionSentBody()))
                    .build();

            event.replyEmbeds(embed).setEphemeral(true).queue();
            new VerifyMessageHandler(event.getJDA(), config, translation).sendDeletionMessage(event.getUser());
        }
    }
}

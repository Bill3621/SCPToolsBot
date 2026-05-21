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

package dev.vxrp.bot.commands.handler.status.status;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ConnectionTable;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StatusCommand {
    private final Config config;
    private final Translation translation;

    public StatusCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void changeMaintenanceState(SlashCommandInteractionEvent event) {
        String instanceKey = GlobalVariables.statusMappedBots.get(event.getJDA().getSelfUser().getId());
        var entry = new ConnectionTable().queryFromTable(instanceKey);
        boolean currentMaintenance = entry.maintenance();

        if (currentMaintenance) {
            String reason = translation.status().embedMaintenanceOffReasonFieldValue();
            if (event.getOption("reason") != null && event.getOption("reason").getAsString() != null) {
                reason = event.getOption("reason").getAsString();
            }

            new ConnectionTable().setMaintenance(instanceKey, false);
            var deactivatedEmbed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.status().embedStatusDeactivatedTitle()))
                    .setDescription(new ColorTool().parse(translation.status().embedStatusDeactivatedBody()))
                    .build();

            event.replyEmbeds(deactivatedEmbed).setEphemeral(true).queue();

            var instance = GlobalVariables.statusInstances.get(instanceKey);
            String instanceName = instance != null ? instance.name() : "Unknown";

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setUrl(config.status().pageUrl())
                    .setTitle(new ColorTool().parse(
                            translation.status().embedMaintenanceOffTitle()
                                    .replace("%instance%", instanceName)).trim())
                    .setDescription(new ColorTool().parse(translation.status().embedMaintenanceOffBody()).trim())
                    .addField(
                            new ColorTool().parse(translation.status().embedMaintenanceOffReasonFieldName()).trim(),
                            new ColorTool().parse(reason).trim(),
                            false)
                    .build();

            var channel = event.getJDA().getTextChannelById(config.status().postChannel());
            if (channel != null) {
                channel.sendMessageEmbeds(embed).queue();
            }
        } else {
            String reason = translation.status().embedMaintenanceOnReasonFieldValue();
            if (event.getOption("reason") != null && event.getOption("reason").getAsString() != null) {
                reason = event.getOption("reason").getAsString();
            }

            new ConnectionTable().setMaintenance(instanceKey, true);
            var activatedEmbed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.status().embedStatusActivatedTitle()))
                    .setDescription(new ColorTool().parse(translation.status().embedStatusActivatedBody()))
                    .build();

            event.replyEmbeds(activatedEmbed).setEphemeral(true).queue();

            var instance = GlobalVariables.statusInstances.get(instanceKey);
            String instanceName = instance != null ? instance.name() : "Unknown";

            var embed = new EmbedBuilder()
                    .setColor(0xf1c40f)
                    .setUrl(config.status().pageUrl())
                    .setTitle(new ColorTool().parse(
                            translation.status().embedMaintenanceOnTitle()
                                    .replace("%instance%", instanceName)).trim())
                    .setDescription(new ColorTool().parse(translation.status().embedMaintenanceOnBody()).trim())
                    .addField(
                            new ColorTool().parse(translation.status().embedMaintenanceOnReasonFieldName()).trim(),
                            new ColorTool().parse(reason).trim(),
                            false)
                    .build();

            var channel = event.getJDA().getTextChannelById(config.status().postChannel());
            if (channel != null) {
                channel.sendMessageEmbeds(embed).queue();
            }
        }
    }
}

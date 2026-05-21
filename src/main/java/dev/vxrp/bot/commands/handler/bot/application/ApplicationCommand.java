/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.commands.handler.bot.application;

import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ApplicationCommand {
    private final Config config;
    private final Translation translation;

    public ApplicationCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void sendActivationMessage(SlashCommandInteractionEvent event) {
        var valuePair = new ApplicationMessageHandler(config, translation)
                .getActivationMenu(event.getUser().getId());
        event.replyEmbeds(valuePair.first).setComponents(valuePair.second).queue();
    }

    public void sendDeactivationMessage(SlashCommandInteractionEvent event) {
        var valuePair = new ApplicationMessageHandler(config, translation).getDeactivationMenu();
        event.replyEmbeds(valuePair.first).setComponents(valuePair.second).queue();
    }
}

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

import dev.vxrp.bot.commands.handler.bot.help.HelpCommand;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class HelpButtons {
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public HelpButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        if (buttonId.startsWith("help_first_page")) {
            event.deferEdit().queue(hook -> {
                event.getChannel().editMessageEmbedsById(event.getMessageId(), new HelpCommand(translation).pages().get(0))
                        .setActionRow(new HelpCommand(translation).actionRow(0)).queue();
            });
        }

        if (buttonId.startsWith("help_last_page")) {
            event.deferEdit().queue(hook -> {
                event.getChannel().editMessageEmbedsById(event.getMessageId(), new HelpCommand(translation).pages().get(5))
                        .setActionRow(new HelpCommand(translation).actionRow(5)).queue();
            });
        }

        if (buttonId.startsWith("help_go_back")) {
            String[] parts = event.getComponentId().split(":");
            int page = Integer.parseInt(parts[1]) - 1;
            event.deferEdit().queue(hook -> {
                event.getChannel().editMessageEmbedsById(event.getMessageId(), new HelpCommand(translation).pages().get(page))
                        .setActionRow(new HelpCommand(translation).actionRow(page)).queue();
            });
        }

        if (buttonId.startsWith("help_go_forward")) {
            String[] parts = event.getComponentId().split(":");
            int page = Integer.parseInt(parts[1]) + 1;
            event.deferEdit().queue(hook -> {
                event.getChannel().editMessageEmbedsById(event.getMessageId(), new HelpCommand(translation).pages().get(page))
                        .setActionRow(new HelpCommand(translation).actionRow(page)).queue();
            });
        }
    }
}

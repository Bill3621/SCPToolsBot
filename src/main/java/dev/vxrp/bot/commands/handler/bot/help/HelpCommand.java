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

package dev.vxrp.bot.commands.handler.bot.help;

import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpCommand {
    private final Translation translation;

    public HelpCommand(Translation translation) {
        this.translation = translation;
    }

    public void pasteHelpMenu(SlashCommandInteractionEvent event) {
        event.replyEmbeds(pages().get(0)).setComponents(actionRow(0)).queue();
    }

    public List<MessageEmbed> pages() {
        List<MessageEmbed> pages = new ArrayList<>();

        ColorTool colorTool = new ColorTool();

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageOneTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageOneBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageTwoTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageTwoBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageThreeTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageThreeBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageFourTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageFourBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageFiveTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageFiveBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        pages.add(new EmbedBuilder().setTitle(translation.help().embedPageSixTitle())
                .setColor(0x000000)
                .setDescription(colorTool.parse(translation.help().embedPageSixBody()).trim())
                .setTimestamp(Instant.now()).setFooter(translation.help().embedFooterText(),
                        translation.help().embedFooterImg())
                .build());

        return pages;
    }

    public ActionRow actionRow(int page) {
        Collection<ActionRowChildComponent> rows = new ArrayList<>();
        if (page == 0) {
            rows.add(Button.danger("help_first_page", "|<").asDisabled());
            rows.add(Button.secondary("help_go_back:0", "<").asDisabled());
            rows.add(Button.link("https://github.com/Vxrpenter/SCPToolsBot/wiki",
                    "📝 Documentation"));
            rows.add(Button.primary("help_go_forward:0", ">"));
            rows.add(Button.success("help_last_page:0", ">|"));
        }
        if (page == 5) {
            rows.add(Button.success("help_first_page", "|<"));
            rows.add(Button.primary("help_go_back:" + page, "<"));
            rows.add(Button.link("https://github.com/Vxrpenter/SCPToolsBot/wiki",
                    "📝 Documentation"));
            rows.add(Button.secondary("help_go_forward:" + page, ">").asDisabled());
            rows.add(Button.danger("help_last_page:" + page, ">|").asDisabled());
        }
        if (page != 0 && page != 5) {
            rows.add(Button.success("help_first_page", "|<"));
            rows.add(Button.primary("help_go_back:" + page, "<"));
            rows.add(Button.link("https://github.com/Vxrpenter/SCPToolsBot/wiki",
                    "📝 Documentation"));
            rows.add(Button.primary("help_go_forward:" + page, ">"));
            rows.add(Button.success("help_last_page:" + page, ">|"));
        }
        return ActionRow.of(rows);
    }
}

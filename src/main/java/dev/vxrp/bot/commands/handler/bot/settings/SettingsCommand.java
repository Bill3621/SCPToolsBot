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

package dev.vxrp.bot.commands.handler.bot.settings;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import io.github.vxrpenter.cedmod.Cedmod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Properties;

public class SettingsCommand {
    private final Config config;
    private final Translation translation;

    public SettingsCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void pasteSettingsMenu(SlashCommandInteractionEvent event) {
        String cedmod = translation.settings().textCedmodOffline();
        int currentColor = 0x00DC82;
        if (checkCedmod()) cedmod = translation.settings().textCedmodOnline();

        ColorTool colorTool = new ColorTool();

        var builder = new EmbedBuilder()
                .setAuthor(event.getGuild() != null ? event.getGuild().getName() : null,
                        null,
                        event.getGuild() != null ? event.getGuild().getIconUrl() : null)
                .setColor(currentColor)
                .setTitle(colorTool.parse(translation.settings().embedSettingsTitle().trim()))
                .setTimestamp(Instant.now())
                .setDescription(colorTool.parse(translation.settings().embedSettingsBody().trim()))
                .addField(colorTool.parse(translation.settings().embedSettingsFieldLanguageTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldLanguageValue()
                                .replace("%language%", config.settings().loadTranslation()).trim()),
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldGuildTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldGuildValue()
                                .replace("%isAvailable%", String.valueOf(event.getGuild() != null && event.getJDA().isUnavailable(event.getGuild().getIdLong()))).trim()),
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldDatabaseTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldDatabaseValue()
                                .replace("%state%", translation.settings().textDatabaseOnline()).trim()),
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldCedmodTitle().trim()),
                        cedmod,
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldVersionTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldVersionValue()
                                .replace("%version%", version()).trim()),
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldBuildTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldBuildValue()
                                .replace("%build%", GlobalVariables.upstreamVersion).trim()),
                        true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldGatewayTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldGatewayValue()
                                .replace("%time%", String.valueOf(event.getJDA().getGatewayPing())).trim()),
                        true)
                .addField("\u200E \n\u200E", "\u200E", true)
                .addField(colorTool.parse(translation.settings().embedSettingsFieldRestTitle().trim()),
                        colorTool.parse(translation.settings().embedSettingsFieldRestValue()
                                .replace("%time%", String.valueOf(event.getJDA().getRestPing().complete())).trim()),
                        true);

        event.replyEmbeds(builder.build()).setActionRow(
                Button.secondary("start_page", colorTool.parse(translation.buttons().textSettingsStart())).asDisabled(),
                Button.primary("configure_page", colorTool.parse(translation.buttons().textSettingsConfigure())),
                Button.primary("current_settings", colorTool.parse(translation.buttons().textSettingsCurrent())),
                Button.primary("bot_info", colorTool.parse(translation.buttons().textSettingsInformation())),
                Button.link("https://github.com/Vxrpenter/SCPToolsBot", colorTool.parse(translation.buttons().textSettingsNews())).withEmoji(Emoji.fromFormatted("🗞️"))
        ).setEphemeral(true).queue();
    }

    private boolean checkCedmod() {
        if (config.settings().cedmod().active()) {
            try {
                new Cedmod(config.settings().cedmod().instance(), config.settings().cedmod().api(), 60, 60).changelogGet();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private String version() {
        var properties = new Properties();

        try (var stream = SettingsCommand.class.getResourceAsStream("/dev/vxrp/version.properties")) {
            if (stream == null) throw new RuntimeException("Version properties file does not exist");
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return properties.getProperty("version");
    }
}

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

package dev.vxrp.bot;

import dev.vxrp.bot.commands.CommandManager;
import dev.vxrp.bot.commands.listeners.CommandListener;
import dev.vxrp.bot.events.ButtonListener;
import dev.vxrp.bot.events.EntitySelectListener;
import dev.vxrp.bot.events.ModalListener;
import dev.vxrp.bot.events.StringSelectListener;
import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureManager;
import dev.vxrp.bot.regulars.RegularsManager;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.updates.handler.UpdatesFileHandler;
import dev.vxrp.util.GlobalVariables;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class BotManager {
    private final Config config;
    private final Translation translation;

    public BotManager(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void init() throws Exception {
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS
        );

        ActivityType activityType = resolveActivityType(config.settings().activityType());
        Activity activity = Activity.of(activityType, config.settings().activityContent());

        JDA api = JDABuilder.createLight(config.settings().token(), intents)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .setActivity(activity)
                .build();

        api.addEventListener(new CommandListener(api, config, translation));
        api.addEventListener(new ButtonListener(api, config, translation));
        api.addEventListener(new StringSelectListener(api, config, translation));
        api.addEventListener(new EntitySelectListener(api, config, translation));
        api.addEventListener(new ModalListener(api, config, translation));
        new NoticeOfDepartureManager(api, config, translation).spinUpChecker();
        new RegularsManager(api, config, translation).spinUpChecker();

        CommandManager commandManager = new CommandManager(config);
        commandManager.registerSpecificCommands(config.extra().commands().commands(), api);

        GlobalVariables.mainCommandManager = commandManager;
        GlobalVariables.mainApi = api;

        new UpdatesFileHandler().override(System.getProperty("user.dir"));
    }

    private ActivityType resolveActivityType(String name) {
        try {
            return ActivityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return ActivityType.PLAYING;
        }
    }
}

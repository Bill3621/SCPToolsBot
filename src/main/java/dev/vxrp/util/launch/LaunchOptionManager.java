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

package dev.vxrp.util.launch;

import dev.vxrp.bot.BotManager;
import dev.vxrp.bot.status.StatusManager;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.launch.data.LaunchArguments;
import dev.vxrp.util.launch.data.LaunchConfigurationOrder;
import dev.vxrp.util.launch.enums.LaunchOptionSectionType;
import dev.vxrp.util.launch.enums.LaunchOptionType;
import dev.vxrp.web.WebServerManager;
import org.slf4j.LoggerFactory;

public class LaunchOptionManager {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(LaunchOptionManager.class);
    private final Config config;
    private final Translation translation;

    public LaunchOptionManager(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void startupBots() {
        logger.info("Launch configuration available, proceeding with startup...");

        LaunchArguments botOptions = checkLaunchOption(LaunchOptionType.BOT);
        LaunchArguments clusterOptions = checkLaunchOption(LaunchOptionType.STATUS_CLUSTER);

        if (botOptions.broken() || clusterOptions.broken()) {
            logger.error("Bot will not start unless configuration is fixed or you turn 'ignore_broken_entries' in 'launch-configuration.json' to true");
            logger.error("Shutting down...");
            return;
        }

        BotManager botManager = new BotManager(config, translation);

        if (botOptions.engage()) {
            try {
                botManager.init();
            } catch (Exception e) {
                logger.error("Failed to initialize bot manager", e);
                return;
            }
        } else {
            logger.error("Because main bot is disabled, any other launches will be canceled");
        }

        if (GlobalVariables.mainCommandManager == null) {
            logger.error("Command Manager must be engaged for Status bots to work");
            return;
        }
        if (clusterOptions.engage() && botOptions.engage()) {
            StatusManager statusManager = new StatusManager(GlobalVariables.mainApi, config, translation, "SCPToolsBot/configs/status-settings.yml");
            statusManager.initialize(GlobalVariables.mainCommandManager);
        }

        if (config.settings().webserver().active()) {
            new WebServerManager(GlobalVariables.mainApi, config, translation);
        }
    }

    public LaunchArguments checkSectionOption(LaunchOptionType type, LaunchOptionSectionType sectionType) {
        LaunchArguments optionCheck = checkLaunchOption(type);
        if (!optionCheck.engage() || optionCheck.broken()) return new LaunchArguments(false, false);

        LaunchConfigurationOrder currentLaunchOption = null;

        for (LaunchConfigurationOrder launchOption : config.extra().launchConfiguration().order()) {
            if (launchOption.id().split(":")[1].equals(type.toString())) currentLaunchOption = launchOption;
        }

        for (var sectionOption : currentLaunchOption.sections()) {
            if (sectionOption.id().split(":")[1].equals(sectionType.toString())) {
                if (sectionOption.logAction()) logger.debug("Launching section {} of {}", sectionType, type);
                return new LaunchArguments(false, true);
            }
        }

        if (!config.extra().launchConfiguration().options().ignoreBrokenEntries()) {
            logger.error("Could not find {} section in {} missing entry. This could be a result of a broken launch configuration. Delete current configuration for it to be regenerated", sectionType, type);
            return new LaunchArguments(true, false);
        }

        return new LaunchArguments(false, false);
    }

    public LaunchArguments checkLaunchOption(LaunchOptionType type) {
        for (LaunchConfigurationOrder launchOption : config.extra().launchConfiguration().order()) {
            if (launchOption.id().split(":")[1].equals(type.toString())) return new LaunchArguments(false, launchOption.engage());
        }

        if (!config.extra().launchConfiguration().options().ignoreBrokenEntries()) {
            logger.error("Could not find {}, missing entry. This could be a result of a broken launch configuration. Delete current configuration for it to be regenerated", type);
            return new LaunchArguments(true, false);
        }

        return new LaunchArguments(false, false);
    }
}

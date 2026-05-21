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

package dev.vxrp;

import dev.vxrp.configuration.ConfigurationManager;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.updates.UpdateManager;
import dev.vxrp.util.launch.LaunchOptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {
        logger.info("Starting up...");
        new UpdateManager().checkUpdated();

        var configurationManager = new ConfigurationManager();

        Config config = configurationManager.initializeConfigs();

        Translation translation = configurationManager.initializeTranslations(config);
        configurationManager.initializeDatabase(config);
        configurationManager.setLoggingLevel(config);

        new UpdateManager().spinUpChecker(config);
        new ScpToolsBot(config, translation);
    }
}

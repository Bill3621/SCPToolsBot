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

package dev.vxrp.configuration.storage;

import java.nio.file.Path;

public class ConfigPaths {
    public Path configPath = Path.of("/SCPToolsBot/configs/config.yml");
    public Path ticketPath = Path.of("/SCPToolsBot/configs/ticket-settings.yml");
    public Path statusPath = Path.of("/SCPToolsBot/configs/status-settings.yml");
    public Path commandsPath = Path.of("/SCPToolsBot/configs/extra/commands.json");
    public Path launchConfigurationPath = Path.of("/SCPToolsBot/configs/extra/launch-configuration.json");

    public Path enUsPath = Path.of("/SCPToolsBot/lang/en_US.yml");
    public Path deDePath = Path.of("/SCPToolsBot/lang/de_DE.yml");
}

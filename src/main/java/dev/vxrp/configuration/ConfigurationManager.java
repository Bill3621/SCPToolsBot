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

package dev.vxrp.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import dev.vxrp.bot.commands.data.CommandList;
import dev.vxrp.bot.status.data.Status;
import dev.vxrp.bot.ticket.data.Ticket;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.ConfigExtra;
import dev.vxrp.configuration.data.Settings;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.configuration.handler.ConfigFileHandler;
import dev.vxrp.configuration.handler.TranslationFileHandler;
import dev.vxrp.configuration.storage.ConfigPaths;
import dev.vxrp.database.DatabaseManager;
import dev.vxrp.util.launch.data.LaunchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationManager {
    private final ConfigFileHandler configFileHandler = new ConfigFileHandler();
    private final TranslationFileHandler translationFileHandler = new TranslationFileHandler();

    private final String workDir = System.getProperty("user.dir");

    public Config initializeConfigs() {
        ConfigPaths paths = new ConfigPaths();

        registerConfig(paths.configPath);
        registerConfig(paths.ticketPath);
        registerConfig(paths.statusPath);
        registerConfig(paths.commandsPath);
        registerConfig(paths.launchConfigurationPath);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Settings settings = loadYamlConfig(yamlMapper, paths.configPath, Settings.class);
        Status status = loadYamlConfig(yamlMapper, paths.statusPath, Status.class);
        Ticket ticket = loadYamlConfig(yamlMapper, paths.ticketPath, Ticket.class);
        CommandList commands = loadJsonConfig(paths.commandsPath, CommandList.class);
        LaunchConfiguration launchConfiguration = loadJsonConfig(paths.launchConfigurationPath, LaunchConfiguration.class);

        return new Config(settings, status, ticket, new ConfigExtra(commands, launchConfiguration));
    }

    public Translation initializeTranslations(Config config) {
        createTranslations(translationFileHandler);
        return translationFileHandler.query(System.getProperty("user.dir"), config.settings().loadTranslation());
    }

    public void initializeDatabase(Config config) {
        new DatabaseManager(config, "/SCPToolsBot/database/data.db");
        configFileHandler.databaseManagement(config);
    }

    public void setLoggingLevel(Config config) {
        Level level = Level.INFO;
        if (config.settings().debug()) {
            level = Level.DEBUG;
        }
        if (config.settings().advancedDebug()) {
            level = Level.TRACE;
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger log = loggerContext.exists(Logger.ROOT_LOGGER_NAME);
        log.setLevel(level);
    }

    private void createTranslations(TranslationFileHandler handler) {
        ConfigPaths paths = new ConfigPaths();
        List<Path> translations = new ArrayList<>();
        translations.add(paths.enUsPath);
        translations.add(paths.deDePath);

        handler.create(workDir, translations);
    }

    private void registerConfig(Path configPath) {
        try {
            Path fullPath = Path.of(workDir + configPath.toString());
            if (!Files.exists(fullPath)) {
                Files.createDirectories(fullPath.getParent());
                try (InputStream is = ConfigurationManager.class.getResourceAsStream(configPath.toString())) {
                    if (is != null) {
                        Files.copy(is, fullPath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.createFile(fullPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to register config file: " + configPath, e);
        }
    }

    private <T> T loadYamlConfig(ObjectMapper mapper, Path configPath, Class<T> type) {
        try {
            Path fullPath = Path.of(workDir + configPath.toString());
            return mapper.readValue(fullPath.toFile(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + configPath, e);
        }
    }

    private <T> T loadJsonConfig(Path configPath, Class<T> type) {
        try {
            Path fullPath = Path.of(workDir + configPath.toString());
            Gson gson = new Gson();
            String json = Files.readString(fullPath);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + configPath, e);
        }
    }
}

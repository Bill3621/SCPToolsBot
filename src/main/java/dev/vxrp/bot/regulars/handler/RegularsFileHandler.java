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

package dev.vxrp.bot.regulars.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.bot.regulars.data.Regulars;
import dev.vxrp.bot.regulars.data.RegularsConfig;
import dev.vxrp.bot.regulars.data.RegularsConfigRole;
import dev.vxrp.bot.regulars.data.RegularsManifest;
import dev.vxrp.configuration.data.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegularsFileHandler {
    private final String workingDirectory = System.getProperty("user.dir");
    private final Config config;
    private final ObjectMapper yamlMapper;

    public RegularsFileHandler(Config config) {
        this.config = config;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());

        try {
            Files.createDirectories(Paths.get(workingDirectory + "/SCPToolsBot/regulars/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (config.settings().regulars().createExample()) {
            createExamples();
        }
    }

    private void createExamples() {
        try {
            Files.createDirectories(Paths.get(workingDirectory + "/SCPToolsBot/regulars/example/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File configFile = new File(workingDirectory + "/SCPToolsBot/regulars/example/config.yml");
        File manifestFile = new File(workingDirectory + "/SCPToolsBot/regulars/example/manifest.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream content = RegularsFileHandler.class.getResourceAsStream("/SCPToolsBot/regulars/example/config.yml")) {
                    if (content != null) {
                        Files.write(configFile.toPath(), content.readAllBytes());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!manifestFile.exists()) {
            try {
                manifestFile.createNewFile();
                try (InputStream content = RegularsFileHandler.class.getResourceAsStream("/SCPToolsBot/regulars/example/manifest.yml")) {
                    if (content != null) {
                        Files.write(manifestFile.toPath(), content.readAllBytes());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Regulars> query() {
        List<Regulars> regulars = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(Paths.get(workingDirectory + "/SCPToolsBot/regulars/"))) {
            List<Path> folders = walk
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            for (Path folder : folders) {
                String folderName = folder.getFileName().toString();
                if (folderName.equals("regulars")) continue;

                RegularsConfig regConfig = queryConfig(folderName);
                RegularsManifest manifest = queryManifest(folderName);

                if (regConfig != null && manifest != null) {
                    regulars.add(new Regulars(folderName, regConfig, manifest));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return regulars;
    }

    private RegularsConfig queryConfig(String folder) {
        File configFile = new File(workingDirectory + "/SCPToolsBot/regulars/" + folder + "/config.yml");
        if (!configFile.exists()) return null;

        try {
            return yamlMapper.readValue(configFile, RegularsConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RegularsManifest queryManifest(String folder) {
        File manifestFile = new File(workingDirectory + "/SCPToolsBot/regulars/" + folder + "/manifest.yml");
        if (!manifestFile.exists()) return null;

        try {
            return yamlMapper.readValue(manifestFile, RegularsManifest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RegularsConfigRole queryRoleFromConfig(String name, String roleId) {
        for (Regulars regular : query()) {
            if (!regular.manifest().name().equals(name)) continue;

            for (RegularsConfigRole role : regular.config().roles()) {
                if (!role.id().equals(roleId)) continue;
                return role;
            }
            break;
        }
        return null;
    }
}

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

package dev.vxrp.updates.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.vxrp.configuration.storage.ConfigPaths;
import dev.vxrp.updates.data.Updates;
import dev.vxrp.updates.data.UpdatesConfigurationSegment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UpdatesFileHandler {
    private final Path file = Path.of("/SCPToolsBot/configs/extra/updates.json");
    private final Gson gson = new Gson();

    public void create(String dir) {
        try {
            Files.createDirectories(Path.of(dir + "/SCPToolsBot/configs/extra/"));

            InputStream content = UpdatesFileHandler.class.getResourceAsStream(file.toString());
            Path currentFilePath = Path.of(dir + file.toString());
            java.io.File currentFile = currentFilePath.toFile();

            if (currentFile.exists()) return;
            currentFile.createNewFile();

            if (content != null) {
                Files.write(currentFilePath, content.readAllBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create updates file", e);
        }
    }

    public void delete(String dir) {
        java.io.File currentFile = Path.of(dir + file.toString()).toFile();
        currentFile.delete();
    }

    public void override(String dir) {
        java.io.File currentFile = Path.of(dir + file.toString()).toFile();

        Updates content = queryNew();
        Updates currentContent = queryOld(dir);

        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

        Updates merged = new Updates(
                content.version(),
                updateList(new ArrayList<>(content.configurationUpdate()), new ArrayList<>(currentContent.configurationUpdate())),
                updateList(new ArrayList<>(content.translationUpdates()), new ArrayList<>(currentContent.translationUpdates())),
                updateList(new ArrayList<>(content.regularsUpdate()), new ArrayList<>(currentContent.regularsUpdate())),
                content.additionalInformation()
        );

        String json = prettyGson.toJson(merged);
        try {
            Files.write(currentFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write updates file", e);
        }
    }

    public void setConfigPaths(Updates updates) {
        List<UpdatesConfigurationSegment> communalList = new ArrayList<>();
        communalList.addAll(updates.configurationUpdate());
        communalList.addAll(updates.translationUpdates());
        communalList.addAll(updates.regularsUpdate());

        for (UpdatesConfigurationSegment config : communalList) {
            if (config.filename().equals("config.yml")) new ConfigPaths().configPath = Path.of(config.location());
            if (config.filename().equals("ticket-settings.yml")) new ConfigPaths().ticketPath = Path.of(config.location());
            if (config.filename().equals("status-settings.yml")) new ConfigPaths().statusPath = Path.of(config.location());
            if (config.filename().equals("commands.json")) new ConfigPaths().commandsPath = Path.of(config.location());
            if (config.filename().equals("launch-configuration.json")) new ConfigPaths().launchConfigurationPath = Path.of(config.location());
            if (config.filename().equals("en_US.yml")) new ConfigPaths().enUsPath = Path.of(config.location());
            if (config.filename().equals("de_DE.yml")) new ConfigPaths().deDePath = Path.of(config.location());
        }
    }

    public Updates queryNew() {
        try (InputStream is = UpdatesFileHandler.class.getResourceAsStream(file.toString())) {
            if (is == null) throw new RuntimeException("Resource not found: " + file);
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(json, Updates.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to query new updates", e);
        }
    }

    public Updates queryOld(String dir) {
        try {
            String json = Files.readString(Path.of(dir + file.toString()));
            return gson.fromJson(json, Updates.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to query old updates", e);
        }
    }

    private List<UpdatesConfigurationSegment> updateList(List<UpdatesConfigurationSegment> newList, List<UpdatesConfigurationSegment> oldList) {
        List<UpdatesConfigurationSegment> list = new ArrayList<>();

        List<UpdatesConfigurationSegment> extraElements = new ArrayList<>();
        List<UpdatesConfigurationSegment> currentExtraElements = new ArrayList<>();

        if (newList.size() > oldList.size()) {
            for (UpdatesConfigurationSegment seg : newList) {
                if (!oldList.contains(seg)) extraElements.add(seg);
            }
        }
        if (newList.size() < oldList.size()) {
            for (UpdatesConfigurationSegment seg : oldList) {
                if (!oldList.contains(seg)) {
                    oldList.remove(seg);
                }
            }
        }

        for (UpdatesConfigurationSegment extra : extraElements) {
            newList.remove(extra);
        }
        for (UpdatesConfigurationSegment extra : currentExtraElements) {
            oldList.remove(extra);
        }

        for (int i = 0; i < Math.min(newList.size(), oldList.size()); i++) {
            UpdatesConfigurationSegment config = newList.get(i);
            UpdatesConfigurationSegment currentConfig = oldList.get(i);
            list.add(new UpdatesConfigurationSegment(
                    config.changed(),
                    currentConfig.regenerate(),
                    config.type(),
                    config.filename(),
                    config.location(),
                    config.upstream()
            ));
        }

        list.addAll(extraElements);
        return list;
    }
}

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

package dev.vxrp.updates;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.updates.handler.UpdateHandler;
import dev.vxrp.updates.handler.UpdatesFileHandler;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateManager {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateManager.class);
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Vxrpenter/SCPToolsBot/releases/latest";

    public void checkUpdated() {
        String dir = System.getProperty("user.dir");
        UpdatesFileHandler fileHandler = new UpdatesFileHandler();
        fileHandler.create(dir);

        try {
            fileHandler.queryOld(dir);
        } catch (Exception e) {
            fileHandler.delete(dir);
            fileHandler.create(dir);
        }

        fileHandler.setConfigPaths(fileHandler.queryNew());
        new UpdateHandler().checkUpdated(fileHandler.queryOld(dir), fileHandler.queryNew());
    }

    public void spinUpChecker(Config config) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkForUpdate(config);
            } catch (Exception e) {
                logger.error("Failed to check for updates", e);
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    private void checkForUpdate(Config config) throws IOException, InterruptedException {
        Properties properties = new Properties();
        try (InputStream versionPropertiesStream = UpdateManager.class.getResourceAsStream("/dev/vxrp/version.properties")) {
            Objects.requireNonNull(versionPropertiesStream, "Version properties file does not exist");
            properties.load(new InputStreamReader(versionPropertiesStream, StandardCharsets.UTF_8));
        }
        String currentVersion = properties.getProperty("version");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
            String tagName = release.get("tag_name").getAsString();
            String htmlUrl = release.has("html_url") ? release.get("html_url").getAsString() : "https://github.com/Vxrpenter/SCPToolsBot/releases/latest";

            if (!tagName.equals("v" + currentVersion)) {
                boolean shouldIgnore = false;
                if (tagName.contains("-alpha") && config.settings().updates().ignoreAlpha()) {
                    shouldIgnore = true;
                }
                if (tagName.contains("-beta") && config.settings().updates().ignoreBeta()) {
                    shouldIgnore = true;
                }

                if (!shouldIgnore) {
                    logger.info("A new version has been found, you can download Version {} here {}", tagName, htmlUrl);
                }
            }
        }
    }
}

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

package dev.vxrp.api.discord;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.vxrp.api.discord.data.DiscordConnection;
import dev.vxrp.api.discord.data.DiscordTokenResponse;
import dev.vxrp.api.discord.data.DiscordUser;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Discord {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(Discord.class);
    private final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final HttpClient client = HttpClient.newHttpClient();

    public DiscordTokenResponse getAccessToken(String clientId, String clientSecret, String authorizationCode, String uri) throws IOException, InterruptedException {
        String formData = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(uri, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.error("Failed to retrieve access token from Discord OAuth Api");
        }

        return mapper.readValue(response.body(), DiscordTokenResponse.class);
    }

    public DiscordUser getUser(DiscordTokenResponse tokenResponse) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/users/@me"))
                .header("Authorization", "Bearer " + tokenResponse.accessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.error("Failed to retrieve user data from Discord OAuth Api");
        }

        return mapper.readValue(response.body(), DiscordUser.class);
    }

    public List<DiscordConnection> getConnections(DiscordTokenResponse tokenResponse) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/users/@me/connections"))
                .header("Authorization", "Bearer " + tokenResponse.accessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            logger.error("Failed to retrieve connection data from Discord OAuth Api");
        }

        return mapper.readValue(response.body(),
                mapper.getTypeFactory().constructCollectionType(List.class, DiscordConnection.class));
    }
}

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

package dev.vxrp.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dev.vxrp.api.discord.data.DiscordConnection;
import dev.vxrp.api.discord.data.DiscordTokenResponse;
import dev.vxrp.api.discord.data.DiscordUser;
import dev.vxrp.bot.verify.VerifyMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.UserTable;
import dev.vxrp.util.coroutines.ExecutorScopes;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class WebServerManager {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(WebServerManager.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WebServerManager(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
        ExecutorScopes.webServerScope.submit(this::startWebServer);
    }

    private void startWebServer() {
        logger.info("Starting up webserver");

        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(config.settings().webserver().port()), 0);
            server.createContext(config.settings().webserver().redirectUri(), createOAuthHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            logger.error("Failed to start webserver", e);
        }
    }

    private HttpHandler createOAuthHandler() {
        return (HttpExchange exchange) -> {
            String responseText = "You were verified successfully, you can close this webpage now.";
            exchange.sendResponseHeaders(200, responseText.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }

            String query = exchange.getRequestURI().getQuery();
            String authorizationCode = getQueryParam(query, "code");

            if (authorizationCode != null) {
                DiscordTokenResponse tokenResponse = getAccessToken(
                        api.getSelfUser().getId(),
                        config.settings().clientSecret(),
                        authorizationCode,
                        config.settings().webserver().uri());
                DiscordUser user = getUser(tokenResponse);
                List<DiscordConnection> connections = getConnections(tokenResponse);
                writeToDatabase(user, connections);
            }
        };
    }

    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private DiscordTokenResponse getAccessToken(String clientId, String clientSecret,
                                                 String authorizationCode, String uri) throws IOException {
        String formBody = "client_id=" + java.net.URLEncoder.encode(clientId, java.nio.charset.StandardCharsets.UTF_8)
                + "&client_secret=" + java.net.URLEncoder.encode(clientSecret, java.nio.charset.StandardCharsets.UTF_8)
                + "&grant_type=authorization_code"
                + "&code=" + java.net.URLEncoder.encode(authorizationCode, java.nio.charset.StandardCharsets.UTF_8)
                + "&redirect_uri=" + java.net.URLEncoder.encode(uri, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error("Failed to retrieve access token from Discord OAuth Api: {}", response.body());
            }
            return gson.fromJson(response.body(), DiscordTokenResponse.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while getting access token", e);
        }
    }

    private DiscordUser getUser(DiscordTokenResponse tokenResponse) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/users/@me"))
                .header("Authorization", "Bearer " + tokenResponse.accessToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error("Failed to retrieve user data from Discord OAuth Api: {}", response.body());
            }
            return gson.fromJson(response.body(), DiscordUser.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while getting user", e);
        }
    }

    private List<DiscordConnection> getConnections(DiscordTokenResponse tokenResponse) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/users/@me/connections"))
                .header("Authorization", "Bearer " + tokenResponse.accessToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error("Failed to retrieve connection data from Discord OAuth Api: {}", response.body());
            }
            return gson.fromJson(response.body(), new TypeToken<List<DiscordConnection>>() {}.getType());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while getting connections", e);
        }
    }

    private void writeToDatabase(DiscordUser user, List<DiscordConnection> connections) {
        for (DiscordConnection connection : connections) {
            if (!connection.type().equals("steam")) continue;

            logger.info("Received connection data from user: {}", user.id());
            new UserTable().addToDatabase(user.id(), LocalDate.now().toString(), connection.id());

            try {
                User currentUser = api.retrieveUserById(user.id()).complete();
                new VerifyMessageHandler(api, config, translation).sendVerificationMessage(currentUser);
            } catch (Exception e) {
                logger.error("Failed to send verification message to user: {}", user.id(), e);
            }
        }
    }
}

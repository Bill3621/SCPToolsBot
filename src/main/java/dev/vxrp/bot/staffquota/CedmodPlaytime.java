/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.staffquota;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dev.vxrp.configuration.data.ConfigCedmod;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

final class CedmodPlaytime {
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();
    private static final Gson gson = new Gson();
    static final int LIFETIME_DAYS = 3650;

    private final ConfigCedmod config;

    CedmodPlaytime(ConfigCedmod config) {
        this.config = config;
    }

    OptionalLong secondsPlayed(String discordId, int windowDays) throws IOException, InterruptedException {
        String query = URLEncoder.encode(discordId + "@discord", StandardCharsets.UTF_8);
        String base = config.instance().replaceAll("/+$", "");
        var request = HttpRequest.newBuilder(URI.create(base + "/Api/Player/Query?q=" + query + "&discordQuery=true&max=1&page=0&staffOnly=false&create=false&sortLabel=id_field&activityMin=" + windowDays + "&basicStats=true&moderationData=false"))
                .header("Authorization", "Bearer " + config.api())
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("CedMod player query returned HTTP " + response.statusCode());

        try {
            var players = gson.fromJson(response.body(), PlayerQuery.class).players();
            if (players == null || players.isEmpty()) return OptionalLong.empty();
            return OptionalLong.of(Math.round(players.getFirst().activity()));
        } catch (JsonParseException e) {
            throw new IOException("CedMod player query returned unreadable JSON", e);
        }
    }

    private record PlayerQuery(List<Player> players) {
        private record Player(double activity) {}
    }
}

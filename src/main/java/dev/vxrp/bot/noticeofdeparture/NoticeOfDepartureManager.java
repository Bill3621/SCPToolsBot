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

package dev.vxrp.bot.noticeofdeparture;

import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureCheckerHandler;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.util.coroutines.ExecutorScopes;
import dev.vxrp.util.coroutines.Timer;
import dev.vxrp.util.duration.DurationParser;
import dev.vxrp.util.duration.enums.DurationType;
import net.dv8tion.jda.api.JDA;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NoticeOfDepartureManager {
    private final JDA api;
    private final Config config;
    private final Translation translation;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(NoticeOfDepartureManager.class);

    public NoticeOfDepartureManager(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void createNotice(String reason, String handler, String userId, String date, String startDate) {
        new NoticeOfDepartureMessageHandler(api, config, translation).sendNoticeMessage(reason, handler, userId, date, startDate);
        var formatter = DateTimeFormatter.ofPattern(config.settings().noticeOfDeparture().dateFormatting());
        if (!LocalDate.parse(startDate, formatter).isAfter(LocalDate.now())) updateNickname(userId, true);
    }

    public void revokeNotice(String reason, String userId, String date) {
        NoticeOfDepartureTable table = new NoticeOfDepartureTable();
        new NoticeOfDepartureMessageHandler(api, config, translation).sendRevokedMessage(reason, userId, table.retrieveBeginDate(userId), date);
        updateNickname(userId, false);

        String channelId = table.retrieveChannel(userId);
        if (channelId != null) {
            var channel = api.getTextChannelById(channelId);
            String messageId = table.retrieveMessage(userId);
            if (channel != null && messageId != null) {
                channel.retrieveMessageById(messageId).queue(msg -> msg.delete().queue());
            }
        }

        table.deleteEntry(userId);
    }

    public void spinUpChecker() {
        if (!config.settings().noticeOfDeparture().active()) return;

        new Timer().runWithTimer(
                new DurationParser().parse(
                        config.settings().noticeOfDeparture().checkRate(),
                        DurationType.valueOf(config.settings().noticeOfDeparture().checkUnit())),
                ExecutorScopes.noticeOfDepartureScope,
                () -> new NoticeOfDepartureCheckerHandler(api, config, translation).checkerTask()
        );
    }

    public void updateNickname(String userId, boolean away) {
        var guild = api.getGuildById(config.settings().guildId());
        if (guild == null) {
            logger.warn("Could not update notice of departure nickname because the configured guild was not found");
            return;
        }

        guild.retrieveMemberById(userId).queue(member -> {
            String prefix = config.settings().noticeOfDeparture().nicknamePrefix();
            String name = member.getEffectiveName();
            String nickname = name;
            if (away && !name.startsWith(prefix)) {
                nickname = prefix + name;
            } else if (!away && name.startsWith(prefix)) {
                nickname = name.substring(prefix.length());
            }
            if (nickname.length() > 32) nickname = nickname.substring(0, 32);
            if (!nickname.equals(name)) {
                guild.modifyNickname(member, nickname).queue(null,
                        error -> logger.warn("Could not update notice of departure nickname for {}", userId, error));
            }
        }, error -> logger.warn("Could not retrieve member {} to update their notice of departure nickname", userId, error));
    }
}

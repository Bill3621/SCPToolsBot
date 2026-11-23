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

package dev.vxrp.bot.commands.handler.bot.quota;

import dev.vxrp.bot.staffquota.QuotaMessages;
import dev.vxrp.bot.staffquota.QuotaRoster;
import dev.vxrp.bot.staffquota.QuotaWeek;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.coroutines.ExecutorScopes;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.LocalDate;

public class QuotaReportCommand {
    private final Config config;
    private final QuotaMessages messages;

    public QuotaReportCommand(Config config, Translation translation) {
        this.config = config;
        this.messages = new QuotaMessages(translation, config.settings().noticeOfDeparture().dateFormatting());
    }

    public void view(SlashCommandInteractionEvent event) {
        var invoker = event.getMember();
        var guild = event.getGuild();
        if (invoker == null || guild == null) return;
        if (!QuotaCommand.canViewOthers(config, invoker)) {
            event.replyEmbeds(messages.noPermission()).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue(hook -> ExecutorScopes.staffQuotaScope.execute(() -> {
            LocalDate today = LocalDate.now();
            QuotaWeek week = QuotaWeek.containing(today);
            var rows = new QuotaRoster(config).evaluate(guild, week, today);
            hook.editOriginal(messages.currentReport(week, today, rows)).queue();
        }));
    }
}

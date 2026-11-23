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

import dev.vxrp.bot.staffquota.QuotaEvaluator;
import dev.vxrp.bot.staffquota.QuotaMessages;
import dev.vxrp.bot.staffquota.QuotaWeek;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.coroutines.ExecutorScopes;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.time.LocalDate;

public class QuotaCommand {
    private final Config config;
    private final QuotaMessages messages;

    public QuotaCommand(Config config, Translation translation) {
        this.config = config;
        this.messages = new QuotaMessages(translation, config.settings().noticeOfDeparture().dateFormatting());
    }

    public void view(SlashCommandInteractionEvent event) {
        Member invoker = event.getMember();
        if (invoker == null) return;

        Member target = event.getOption("user", invoker, OptionMapping::getAsMember);
        if (target == null) {
            event.replyEmbeds(messages.notStaff(event.getOption("user", OptionMapping::getAsUser).getName())).setEphemeral(true).queue();
            return;
        }
        if (!target.equals(invoker) && !canViewOthers(config, invoker)) {
            event.replyEmbeds(messages.noPermission()).setEphemeral(true).queue();
            return;
        }

        var evaluator = new QuotaEvaluator(config);
        var weeklyHours = evaluator.weeklyHours(target);
        if (weeklyHours.isEmpty()) {
            event.replyEmbeds(messages.notStaff(target.getEffectiveName())).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue(hook -> ExecutorScopes.staffQuotaScope.execute(() -> {
            LocalDate today = LocalDate.now();
            QuotaWeek week = QuotaWeek.containing(today);
            var standing = evaluator.evaluate(target.getId(), weeklyHours.getAsDouble(), week, today);
            hook.editOriginal(messages.view(target.getEffectiveName(), week, today, standing)).queue();
        }));
    }

    static boolean canViewOthers(Config config, Member member) {
        var allowed = config.settings().staffQuota().rolesViewOthers();
        return member.hasPermission(Permission.ADMINISTRATOR) || member.getRoles().stream().anyMatch(role -> allowed.contains(role.getId()));
    }
}

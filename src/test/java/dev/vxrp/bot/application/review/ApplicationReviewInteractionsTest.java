/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license with this file, you may obtain a copy
 * of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.review;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.configuration.data.TranslationApplicationReview;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationReviewInteractionsTest {
    @Test
    void voteFeedbackRendersEnglishChoices() throws IOException {
        var review = reviewTranslation("en_US");

        assertEquals("Your vote was recorded as Approve.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.APPROVE, false));
        assertEquals("Your vote was changed to Deny.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.DENY, true));
        assertEquals("Your vote was recorded as Abstain.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.ABSTAIN, false));
    }

    @Test
    void voteFeedbackRendersGermanChoices() throws IOException {
        var review = reviewTranslation("de_DE");

        assertEquals("Deine Stimme wurde als Annehmen gespeichert.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.APPROVE, false));
        assertEquals("Deine Stimme wurde zu Ablehnen geändert.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.DENY, true));
        assertEquals("Deine Stimme wurde als Enthalten gespeichert.",
                ApplicationReviewInteractions.voteFeedback(review, ApplicationVoteChoice.ABSTAIN, false));
    }

    @Test
    void closeSuccessRendersLocalizedDecisions() throws IOException {
        var english = reviewTranslation("en_US");
        var german = reviewTranslation("de_DE");

        assertEquals("Application closed as approved.",
                ApplicationReviewInteractions.closeSuccess(english, ApplicationDecision.APPROVED));
        assertEquals("Application closed as denied.",
                ApplicationReviewInteractions.closeSuccess(english, ApplicationDecision.DENIED));
        assertEquals("Bewerbung als angenommen geschlossen.",
                ApplicationReviewInteractions.closeSuccess(german, ApplicationDecision.APPROVED));
        assertEquals("Bewerbung als abgelehnt geschlossen.",
                ApplicationReviewInteractions.closeSuccess(german, ApplicationDecision.DENIED));
    }

    @Test
    void closeSuccessSupportsExistingTranslationFilesWithoutDecisionLabels() throws IOException {
        var review = new ObjectMapper(new YAMLFactory()).readValue(
                "TEXT_CLOSE_SUCCESS: Application closed as %decision%.", TranslationApplicationReview.class);

        assertEquals("Application closed as approved.",
                ApplicationReviewInteractions.closeSuccess(review, ApplicationDecision.APPROVED));
    }

    @Test
    void closingApplicationLocksAndArchivesTicketAndStaffDiscussion() {
        var states = new HashMap<String, ThreadState>();
        states.put("ticket", new ThreadState());
        states.put("discussion", new ThreadState());
        var api = (JDA) Proxy.newProxyInstance(JDA.class.getClassLoader(), new Class<?>[]{JDA.class},
                (proxy, method, args) -> "getThreadChannelById".equals(method.getName())
                        ? thread(states.get(args[0])) : null);
        var application = new ApplicationRecord("ticket", "role", "applicant", null, ApplicationLifecycle.CLOSED,
                ApplicationDecision.APPROVED, null, null, null, null, null, null, "discussion", 1);

        ApplicationReviewInteractions.archiveThreads(api, application);

        for (var entry : states.entrySet()) {
            assertTrue(entry.getValue().locked, entry.getKey() + " should be locked");
            assertTrue(entry.getValue().archived, entry.getKey() + " should be archived");
            assertTrue(entry.getValue().queued, entry.getKey() + " should be queued");
        }
    }

    @Test
    void approvalAssignsTheAppliedForRoleOnlyWhenNeeded() {
        var role = (Role) Proxy.newProxyInstance(Role.class.getClassLoader(), new Class<?>[]{Role.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getId" -> "123";
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
        var assignments = new AtomicInteger();
        var existingRoles = new AtomicReference<List<Role>>(List.of());
        var member = (Member) Proxy.newProxyInstance(Member.class.getClassLoader(), new Class<?>[]{Member.class},
                (proxy, method, args) -> "getRoles".equals(method.getName()) ? existingRoles.get() : null);
        var guild = (Guild) Proxy.newProxyInstance(Guild.class.getClassLoader(), new Class<?>[]{Guild.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRoleById" -> {
                        assertEquals("123", args[0]);
                        yield role;
                    }
                    case "retrieveMemberById" -> action(method.getReturnType(), member);
                    case "addRoleToMember" -> {
                        assertSame(member, args[0]);
                        assertSame(role, args[1]);
                        assignments.incrementAndGet();
                        yield action(method.getReturnType(), null);
                    }
                    default -> null;
                });
        var application = new ApplicationRecord("ticket", "123", "applicant", null, ApplicationLifecycle.CLOSED,
                ApplicationDecision.APPROVED, null, null, null, null, null, null, null, 1);
        var result = new AtomicReference<Boolean>();

        ApplicationReviewInteractions.grantApprovedRole(guild, application, result::set);
        assertEquals(true, result.get());
        assertEquals(1, assignments.get());

        existingRoles.set(List.of(role));
        ApplicationReviewInteractions.grantApprovedRole(guild, application, result::set);
        assertEquals(true, result.get());
        assertEquals(1, assignments.get());
    }

    @Test
    void approvalReportsMissingRoleRatherThanClaimingItWasAssigned() {
        var guild = (Guild) Proxy.newProxyInstance(Guild.class.getClassLoader(), new Class<?>[]{Guild.class},
                (proxy, method, args) -> null);
        var application = new ApplicationRecord("ticket", "123", "applicant", null, ApplicationLifecycle.CLOSED,
                ApplicationDecision.APPROVED, null, null, null, null, null, null, null, 1);
        var result = new AtomicReference<Boolean>();

        ApplicationReviewInteractions.grantApprovedRole(guild, application, result::set);

        assertEquals(false, result.get());
    }

    @SuppressWarnings("unchecked")
    private static <T> Object action(Class<?> type, T response) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if ("queue".equals(method.getName())) {
                ((Consumer<T>) args[0]).accept(response);
            }
            return null;
        });
    }

    private static ThreadChannel thread(ThreadState state) {
        if (state == null) {
            return null;
        }
        var manager = (ThreadChannelManager) Proxy.newProxyInstance(ThreadChannelManager.class.getClassLoader(),
                new Class<?>[]{ThreadChannelManager.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setLocked" -> state.locked = (boolean) args[0];
                        case "setArchived" -> state.archived = (boolean) args[0];
                        case "queue" -> state.queued = true;
                    }
                    return method.getReturnType() == void.class ? null : proxy;
                });
        return (ThreadChannel) Proxy.newProxyInstance(ThreadChannel.class.getClassLoader(),
                new Class<?>[]{ThreadChannel.class}, (proxy, method, args) ->
                        "getManager".equals(method.getName()) ? manager : null);
    }

    private static final class ThreadState {
        boolean locked;
        boolean archived;
        boolean queued;
    }

    private static TranslationApplicationReview reviewTranslation(String locale) throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory()).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (var input = ApplicationReviewInteractionsTest.class.getResourceAsStream("/SCPToolsBot/lang/" + locale + ".yml")) {
            assertNotNull(input);
            return mapper.readValue(input, Translation.class).applicationReview();
        }
    }
}

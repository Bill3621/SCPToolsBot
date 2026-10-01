/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.view;

import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.data.ApplicationTally;
import dev.vxrp.bot.application.data.ApplicationVote;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationReviewViewTest {
    @Test
    void openReviewUsesComponentsV2WithoutLegacyContent() {
        var application = application("review-1");
        var message = ApplicationReviewView.openReview(application, "Supporter", List.of(),
                new ApplicationTally(1, 0, 0, 3), "https://discord.com/channels/1/2", null);

        assertTrue(message.isUsingComponentsV2());
        assertEquals("", message.getContent());
        assertEquals(1, message.getComponents().size());
        assertInstanceOf(Container.class, message.getComponents().getFirst());
    }

    @Test
    void onlyPersonalReviewDisplaysTheCurrentVoter() {
        var application = application("review-1");
        var tally = new ApplicationTally(1, 0, 0, 3);
        var publicReview = ApplicationReviewView.openReview(application, "Supporter", List.of(), tally, null, null);
        var personalReview = ApplicationReviewView.personalReview(application, "Supporter", List.of(), tally,
                new ApplicationVote(application.id(), "42", ApplicationVoteChoice.APPROVE, Instant.parse("2026-08-12T10:00:00Z")), null, null);
        var notVotedReview = ApplicationReviewView.personalReview(application, "Supporter", List.of(), tally, null, null, null);
        var lockedReview = ApplicationReviewView.lockedReview(application, "Supporter", List.of(), tally, List.of(), null, null);
        var closeConfirmation = ApplicationReviewView.closeConfirmation(application, "Supporter", tally);

        assertFalse(text(publicReview).contains("your vote"));
        assertFalse(text(lockedReview).contains("your vote"));
        assertFalse(text(closeConfirmation).contains("your vote"));
        assertTrue(text(personalReview).contains("your vote: **APPROVE**"));
        assertTrue(text(notVotedReview).contains("your vote: **not cast**"));
    }

    @ParameterizedTest
    @CsvSource({"4, 0, 2, APPROVED, SUCCESS", "0, 4, 0, DENIED, DANGER", "2, 2, 1, DENIED, DANGER"})
    void closeConfirmationEmphasizesRecommendedDecisionIncludingDenialOnTies(int approve, int deny, int abstain, ApplicationDecision recommendation, ButtonStyle style) {
        var application = application("review-1");
        var message = ApplicationReviewView.closeConfirmation(application, "Moderator", new ApplicationTally(approve, deny, abstain, 4));
        String action = recommendation == ApplicationDecision.APPROVED ? "Approve" : "Deny";
        ApplicationDecision alternative = recommendation == ApplicationDecision.APPROVED ? ApplicationDecision.DENIED : ApplicationDecision.APPROVED;

        assertTrue(text(message).startsWith("## " + action + " application?\n<@456> · Moderator\n"));
        assertTrue(text(message).contains("**" + approve + "** approve · **" + deny + "** deny · **" + abstain + "** abstain"));
        assertTrue(text(message).contains("Quorum met · **4/4** counted"));
        assertTrue(text(message).contains("Choose a decision, then add a reason."));
        var decisions = buttons(message);
        assertEquals(2, decisions.size());
        assertEquals(action, decisions.getFirst().getLabel());
        assertEquals(style, decisions.getFirst().getStyle());
        assertEquals("application_review_close_decision:review-1:" + recommendation, decisions.getFirst().getCustomId());
        assertEquals(recommendation == ApplicationDecision.APPROVED ? "Deny" : "Approve", decisions.getLast().getLabel());
        assertEquals(ButtonStyle.SECONDARY, decisions.getLast().getStyle());
        assertEquals("application_review_close_decision:review-1:" + alternative, decisions.getLast().getCustomId());
    }

    @Test
    void reviewShowsDiscussionLinkOnceThreadExists() {
        var application = application("review-1");
        var tally = new ApplicationTally(0, 0, 0, 3);
        var pending = ApplicationReviewView.openReview(application, "Supporter", List.of(), tally, null, null);
        assertEquals("application_review_discussion:review-1", buttons(pending).stream()
                .filter(button -> "Start discussion".equals(button.getLabel())).findFirst().orElseThrow().getCustomId());

        String discussionUrl = "https://discord.com/channels/1/30";
        var canonical = ApplicationReviewView.openReview(application, "Supporter", List.of(), tally, null, discussionUrl);
        var command = ApplicationReviewView.personalReview(application, "Supporter", List.of(), tally, null, null, discussionUrl);
        var closed = ApplicationReviewView.lockedReview(application, "Supporter", List.of(), tally, List.of(), null, discussionUrl);
        for (var message : List.of(canonical, command, closed)) {
            assertEquals(discussionUrl, buttons(message).stream()
                    .filter(button -> "Staff discussion".equals(button.getLabel())).findFirst().orElseThrow().getUrl());
            assertFalse(buttons(message).stream().anyMatch(button -> "Start discussion".equals(button.getLabel())));
        }
        var withoutReviewMessage = new ApplicationRecord(application.id(), application.roleId(), application.issuer(),
                application.handler(), application.lifecycle(), application.decision(), application.createdAt(),
                application.closedAt(), application.closedBy(), application.closeReason(), application.reviewChannelId(),
                null, null, application.quorumRequired());
        assertFalse(buttons(ApplicationReviewView.openReview(withoutReviewMessage, "Supporter", List.of(), tally, null, null))
                .stream().anyMatch(button -> "Start discussion".equals(button.getLabel())));
    }

    @Test
    void queueCapsCompleteEntriesWithinComponentBudget() {
        var entries = java.util.stream.IntStream.range(0, 10)
                .mapToObj(index -> new ApplicationReviewView.QueueEntry(application("queue-" + index),
                        "Supporter", "100" + index, new ApplicationTally(0, 0, 0, 3)))
                .toList();
        var message = ApplicationReviewView.queue("42", ApplicationQueueFilter.OPEN, 0, 8, entries, 10);

        assertTrue(message.isUsingComponentsV2());
        var container = message.getComponents().getFirst().asContainer();
        int componentCount = 1 + container.getComponents().stream()
                .mapToInt(component -> component.getType() == net.dv8tion.jda.api.components.Component.Type.ACTION_ROW
                        ? 1 + component.asActionRow().getComponents().size()
                        : component.getType() == net.dv8tion.jda.api.components.Component.Type.SECTION
                                ? 2
                                : 1)
                .sum();
        assertTrue(componentCount <= 40);
    }

    @Test
    void queuePaginationRendersEveryEntryIncludingTheEighth() {
        var entries = java.util.stream.IntStream.range(0, 16)
                .mapToObj(index -> new ApplicationReviewView.QueueEntry(application("queue-" + index),
                        "Supporter", "100" + index, new ApplicationTally(0, 0, 0, 3)))
                .toList();
        int pageSize = ApplicationReviewView.DEFAULT_PAGE_SIZE;
        var renderedIds = new java.util.ArrayList<String>();
        for (int offset = 0; offset < entries.size(); offset += pageSize) {
            var page = entries.subList(offset, Math.min(offset + pageSize, entries.size()));
            var message = ApplicationReviewView.queue("42", ApplicationQueueFilter.OPEN, offset / pageSize,
                    pageSize, page, entries.size());
            message.getComponents().getFirst().asContainer().getComponents().stream()
                    .filter(component -> component.getType() == net.dv8tion.jda.api.components.Component.Type.SECTION)
                    .forEach(component -> renderedIds.add(component.asSection().getAccessory().asButton().getCustomId()));
        }
        assertEquals(entries.stream().map(entry -> "application_review:" + entry.application().id()).toList(), renderedIds);
    }

    @Test
    void customIdsAreDeterministic() {
        assertEquals("application_review_vote:abc", ApplicationReviewView.voteId("abc"));
        assertEquals("applications_page:42:READY:2",
                ApplicationReviewView.pageId("42", ApplicationQueueFilter.READY, 2));
        assertEquals("application_review_history:abc:3", ApplicationReviewView.historyId("abc", 3));
    }

    private static List<Button> buttons(MessageCreateData message) {
        return message.getComponents().getFirst().asContainer().getComponents().stream()
                .filter(component -> component instanceof ActionRow)
                .flatMap(component -> ((ActionRow) component).getComponents().stream())
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .toList();
    }

    private static String text(MessageCreateData message) {
        return message.getComponents().getFirst().asContainer().getComponents().stream()
                .filter(component -> component instanceof TextDisplay)
                .map(component -> ((TextDisplay) component).getContent())
                .collect(Collectors.joining("\n"));
    }

    private static ApplicationRecord application(String id) {
        return new ApplicationRecord(id, "123", "456", null, ApplicationLifecycle.OPEN, null,
                Instant.parse("2026-08-12T10:00:00Z"), null, null, null, "10", "20", null, 3);
    }
}

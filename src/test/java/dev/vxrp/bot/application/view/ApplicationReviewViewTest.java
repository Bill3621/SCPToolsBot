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
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import net.dv8tion.jda.api.components.container.Container;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationReviewViewTest {
    @Test
    void openReviewUsesComponentsV2WithoutLegacyContent() {
        var application = application("review-1");
        var message = ApplicationReviewView.openReview(application, "Supporter", List.of(),
                new ApplicationTally(1, 0, 0, 3), null, "https://discord.com/channels/1/2");

        assertTrue(message.isUsingComponentsV2());
        assertEquals("", message.getContent());
        assertEquals(1, message.getComponents().size());
        assertInstanceOf(Container.class, message.getComponents().getFirst());
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
    void customIdsAreDeterministic() {
        assertEquals("application_review_vote:abc", ApplicationReviewView.voteId("abc"));
        assertEquals("applications_page:42:READY:2",
                ApplicationReviewView.pageId("42", ApplicationQueueFilter.READY, 2));
        assertEquals("application_review_history:abc:3", ApplicationReviewView.historyId("abc", 3));
    }

    private static ApplicationRecord application(String id) {
        return new ApplicationRecord(id, "123", "456", null, ApplicationLifecycle.OPEN, null,
                Instant.parse("2026-08-12T10:00:00Z"), null, null, null, "10", "20", 3);
    }
}

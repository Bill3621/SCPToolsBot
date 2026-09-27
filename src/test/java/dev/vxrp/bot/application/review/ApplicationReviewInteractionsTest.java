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
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.configuration.data.TranslationApplicationReview;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private static TranslationApplicationReview reviewTranslation(String locale) throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory()).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (var input = ApplicationReviewInteractionsTest.class.getResourceAsStream("/SCPToolsBot/lang/" + locale + ".yml")) {
            assertNotNull(input);
            return mapper.readValue(input, Translation.class).applicationReview();
        }
    }
}

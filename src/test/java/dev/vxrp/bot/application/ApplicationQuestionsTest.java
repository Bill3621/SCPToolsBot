package dev.vxrp.bot.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.bot.ticket.data.ApplicationQuestion;
import dev.vxrp.bot.ticket.data.Ticket;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationQuestionsTest {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    void bundledQuestionsBuildTheApplicationModal() throws IOException {
        Ticket ticket;
        Translation translation;
        try (var input = getClass().getResourceAsStream("/SCPToolsBot/configs/ticket-settings.yml");
             var language = getClass().getResourceAsStream("/SCPToolsBot/lang/en_US.yml")) {
            assertNotNull(input);
            assertNotNull(language);
            ticket = mapper.readValue(input, Ticket.class);
            translation = mapper.readValue(language, Translation.class);
        }

        var modal = new TicketTemplateModals(translation).supportApplicationModal("123", ticket.questionsForRole(ticket.applicationTypes().getFirst().roleID()));
        assertEquals("ticket_application:123", modal.getId());
        assertEquals(ticket.applicationQuestions().size(), modal.getComponents().size());
        for (int index = 0; index < ticket.applicationQuestions().size(); index++) {
            var question = ticket.applicationQuestions().get(index);
            var label = modal.getComponents().get(index).asLabel();
            var input = label.getChild().asTextInput();
            assertEquals(question.label(), label.getLabel());
            assertEquals("application_question_" + index, input.getCustomId());
            assertEquals(question.style(), input.getStyle());
            assertEquals(question.minLength(), input.getMinLength());
            assertEquals(question.maxLength(), input.getMaxLength());
        }
    }

    @Test
    void positionQuestionsOverrideSharedQuestionsAndWorkWithoutDefaults() throws IOException {
        var ticket = mapper.readValue("""
                application_questions:
                  - { label: Discord name, placeholder: Your name, style: SHORT, min_length: 2, max_length: 100 }
                application_types:
                  - name: Event Host
                    role_id: '1'
                    questions:
                      - { label: Steam profile, placeholder: Profile link or Steam ID, style: SHORT, min_length: 4, max_length: 100 }
                  - name: SL Staff
                    role_id: '2'
                    questions:
                      - { label: Steam ID, placeholder: Your Steam ID, style: SHORT, min_length: 4, max_length: 100 }
                  - name: Discord Staff
                    role_id: '3'
                """, Ticket.class);

        assertEquals("Steam profile", ticket.questionsForRole("1").getFirst().label());
        assertEquals("Steam ID", ticket.questionsForRole("2").getFirst().label());
        assertEquals("Discord name", ticket.questionsForRole("3").getFirst().label());
        assertNull(ticket.questionsForRole("4"));
        assertEquals("Steam profile", ApplicationSubmissionService.answers(ticket.questionsForRole("1"),
                List.of(mapping("application_question_0", "https://steamcommunity.com/id/example"))).getFirst().fieldKey());

        var withoutShared = mapper.readValue("""
                application_types:
                  - name: Event Host
                    role_id: '1'
                    questions:
                      - { label: Steam profile, placeholder: Profile link, style: SHORT, min_length: 4, max_length: 100 }
                """, Ticket.class);
        assertEquals("Steam profile", withoutShared.questionsForRole("1").getFirst().label());
    }

    @Test
    void sevenQuestionsSplitAcrossTwoModalsAndPreserveAnswerOrder() throws IOException {
        var questions = IntStream.range(0, 7)
                .mapToObj(index -> new ApplicationQuestion("Question " + index, "Answer here", TextInputStyle.SHORT, 1, 100, false))
                .toList();
        ApplicationQuestion.validate(questions, "application_questions");
        Translation translation;
        try (var input = getClass().getResourceAsStream("/SCPToolsBot/lang/en_US.yml")) {
            translation = mapper.readValue(input, Translation.class);
        }
        var modals = new TicketTemplateModals(translation);
        var first = modals.supportApplicationModal("123", questions);
        var second = modals.applicationContinuationModal("token", questions);

        assertEquals(5, first.getComponents().size());
        assertEquals(2, second.getComponents().size());
        assertEquals("ticket_application_final:token", second.getId());
        assertEquals("application_question_5", second.getComponents().getFirst().asLabel().getChild().asTextInput().getCustomId());
        assertEquals("Question 6", second.getComponents().getLast().asLabel().getLabel());

        var firstAnswers = ApplicationSubmissionService.answers(questions.subList(0, 5), IntStream.range(0, 5)
                .mapToObj(index -> mapping("application_question_" + index, "Answer " + index)).toList());
        var secondAnswers = ApplicationSubmissionService.answers(questions.subList(5, 7), List.of(
                mapping("application_question_6", "Answer 6"), mapping("application_question_5", "Answer 5")), 5);
        assertNotNull(firstAnswers);
        assertNotNull(secondAnswers);
        assertEquals(List.of("Question 0", "Question 1", "Question 2", "Question 3", "Question 4", "Question 5", "Question 6"),
                java.util.stream.Stream.concat(firstAnswers.stream(), secondAnswers.stream())
                        .map(answer -> answer.fieldKey()).toList());
        assertNull(ApplicationSubmissionService.answers(questions.subList(5, 7), List.of(
                mapping("application_question_0", "Answer 5"), mapping("application_question_6", "Answer 6")), 5));
        assertThrows(IllegalArgumentException.class, () -> ApplicationQuestion.validate(IntStream.range(0, 11)
                .mapToObj(index -> new ApplicationQuestion("Question " + index, "Answer", TextInputStyle.SHORT, 1, 100, false))
                .toList(), "application_questions"));
    }

    @Test
    void answersUseConfiguredLabelsAndRejectMissingFields() {
        var questions = List.of(
                new ApplicationQuestion("What would you host?", "Describe your event", TextInputStyle.PARAGRAPH, 2, 200, false),
                new ApplicationQuestion("Age", "Your age", TextInputStyle.SHORT, 2, 3, true));
        var answers = ApplicationSubmissionService.answers(questions, List.of(
                mapping("application_question_1", "25"), mapping("application_question_0", "Game night")));

        assertNotNull(answers);
        assertEquals(List.of("What would you host?", "Age"), answers.stream().map(answer -> answer.fieldKey()).toList());
        assertEquals(List.of("Game night", "25"), answers.stream().map(answer -> answer.value()).toList());
        assertNull(ApplicationSubmissionService.answers(questions, List.of(mapping("application_question_0", "Game night"))));
        assertNull(ApplicationSubmissionService.answers(questions, List.of(
                mapping("application_question_0", "Game night"), mapping("application_question_0", "Again"))));
        assertNull(ApplicationSubmissionService.answers(questions, List.of(
                mapping("application_question_0", "Game night"), mapping("application_question_1", ""))));
    }

    @Test
    void rejectsInvalidQuestionListsAndLengths() throws IOException {
        var empty = "application_questions: []\n";
        assertThrows(IOException.class, () -> mapper.readValue(empty, Ticket.class));
        assertThrows(IOException.class, () -> mapper.readValue("""
                application_types:
                  - { name: Discord Staff, role_id: '3' }
                """, Ticket.class));
        assertThrows(IOException.class, () -> mapper.readValue("""
                application_questions:
                  - { label: Age, placeholder: Your age, style: SHORT, min_length: 2, max_length: 3 }
                  - { label: Age, placeholder: Your age, style: SHORT, min_length: 2, max_length: 3 }
                """, Ticket.class));
        assertThrows(IllegalArgumentException.class, () -> new ApplicationQuestion("Age", "Age", TextInputStyle.SHORT, 3, 2, true));
    }

    private static ModalMapping mapping(String id, String value) {
        return new ModalMapping(null, null, DataObject.empty()
                .put("id", 1).put("custom_id", id).put("type", 4).put("value", value));
    }
}

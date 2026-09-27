package dev.vxrp.bot.application.view;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationViewTest {
    @Test
    void publicPanelShowsLiveCountsAndDirectButtonsInPositionOrder() throws IOException {
        var message = ApplicationView.publicPanel("Applications", "Choose a position to apply.", List.of(
                type(2, "3", "Event Host", false, 0),
                type(0, "1", "SL Staff", true, 3),
                type(1, "2", "Discord Staff", false, 0)), Map.of("1", 1L, "2", 0L, "3", 0L), true, translation());
        var components = message.getComponents().getFirst().asContainer().getComponents();
        var sections = components.stream().filter(component -> component instanceof Section)
                .map(component -> (Section) component).toList();

        assertTrue(message.isUsingComponentsV2());
        assertEquals(3, sections.size());
        assertEquals(List.of("application_apply:1", "application_apply:2", "application_apply:3"),
                sections.stream().map(section -> section.getAccessory().asButton().getCustomId()).toList());
        assertEquals("Apply", sections.getFirst().getAccessory().asButton().getLabel());
        assertFalse(sections.getFirst().getAccessory().asButton().isDisabled());
        assertTrue(sections.get(1).getAccessory().asButton().isDisabled());
        assertEquals("Closed", sections.get(1).getAccessory().asButton().getLabel());
        assertTrue(sections.get(2).getAccessory().asButton().isDisabled());
        assertTrue(sections.getFirst().getContentComponents().getFirst().asTextDisplay().getContent()
                .contains("**🟢 SL Staff**\n> Role description\n1 candidate"));
        assertTrue(sections.get(1).getContentComponents().getFirst().asTextDisplay().getContent()
                .contains("**⚫ Discord Staff**\n> Role description\nNo candidates"));
        String header = components.getFirst().asTextDisplay().getContent();
        assertEquals("## Applications\nChoose a position to apply.", header);
        assertFalse(header.contains("Current status"));
    }

    @Test
    void filledPositionAndDeactivatedPhaseHaveNoApplyActions() throws IOException {
        var type = type(0, "1", "SL Staff", true, 1);
        var filled = ApplicationView.publicPanel("Applications", "Choose a position to apply.",
                List.of(type), Map.of("1", 1L), true, translation());
        var deactivated = ApplicationView.publicPanel("Applications", "Choose a position to apply.",
                List.of(type), Map.of("1", 0L), false, translation());

        assertTrue(section(filled).getAccessory().asButton().isDisabled());
        assertTrue(section(deactivated).getAccessory().asButton().isDisabled());
        assertEquals("Closed", section(filled).getAccessory().asButton().getLabel());
    }

    @Test
    void twelvePositionsFitWithinTheComponentBudget() throws IOException {
        var types = IntStream.range(0, 12)
                .mapToObj(index -> type(index, String.valueOf(index + 1), "Role " + index, true, 2)).toList();
        var message = ApplicationView.publicPanel("Applications", "Choose a position to apply.",
                types, Map.of(), true, translation());

        assertEquals(12, message.getComponents().getFirst().asContainer().getComponents().stream()
                .filter(component -> component.getType() == Component.Type.SECTION).count());
    }

    @Test
    void manyPositionsFallBackWithoutExceedingComponentBudget() throws IOException {
        var types = IntStream.range(0, 13)
                .mapToObj(index -> type(index, String.valueOf(index + 1), "Role " + index, true, 2)).toList();
        var message = ApplicationView.publicPanel("Applications", "Choose a position to apply.",
                types, Map.of(), true, translation());
        var components = message.getComponents().getFirst().asContainer().getComponents();

        assertTrue(components.stream().noneMatch(component -> component.getType() == Component.Type.SECTION));
        assertTrue(components.stream().anyMatch(component -> component instanceof TextDisplay
                && component.asTextDisplay().getContent().contains("Role 12")));
        assertTrue(components.stream().anyMatch(component -> component.getType() == Component.Type.ACTION_ROW));
    }

    private static Section section(net.dv8tion.jda.api.utils.messages.MessageCreateData message) {
        return message.getComponents().getFirst().asContainer().getComponents().stream()
                .filter(component -> component instanceof Section).findFirst().orElseThrow().asSection();
    }

    private static ApplicationType type(int position, String id, String name, boolean active, int limit) {
        return new ApplicationType(position, id, name, "Role description", "", active, null, limit);
    }

    private static Translation translation() throws IOException {
        try (var input = ApplicationViewTest.class.getResourceAsStream("/SCPToolsBot/lang/en_US.yml")) {
            return new ObjectMapper(new YAMLFactory()).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(input, Translation.class);
        }
    }
}

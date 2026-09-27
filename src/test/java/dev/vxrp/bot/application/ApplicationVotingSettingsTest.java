package dev.vxrp.bot.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.bot.ticket.data.ApplicationVotingSettings;
import dev.vxrp.bot.ticket.data.Ticket;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationVotingSettingsTest {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Test
    void fixedQuorumRemainsDefault() throws IOException {
        var voting = mapper.readValue("quorum: 4\nsenior_closer_roles: []", ApplicationVotingSettings.class);

        assertNull(voting.quorumPercent());
        assertEquals(4, voting.requiredVotes(100));
        assertEquals(3, new ApplicationVotingSettings().requiredVotes(0));
    }

    @Test
    void percentageRoundsUpAndNeverAllowsZeroVotes() throws IOException {
        var voting = mapper.readValue("quorum: 4\nquorum_percent: 60\nsenior_closer_roles: []",
                ApplicationVotingSettings.class);

        assertEquals(1, voting.requiredVotes(0));
        assertEquals(1, voting.requiredVotes(1));
        assertEquals(2, voting.requiredVotes(3));
        assertEquals(3, voting.requiredVotes(4));
        assertEquals(3, voting.requiredVotes(5));
        assertEquals(60, voting.requiredVotes(100));
    }

    @Test
    void percentageCountsDistinctMembersWithEitherVotingRole() throws IOException {
        var ticket = mapper.readValue("""
                settings:
                  application_voting:
                    quorum_percent: 50
                    senior_closer_roles: ['200', '300']
                application_types:
                  - name: Supporter
                    role_id: '1'
                    questions:
                      - { label: Name, placeholder: Your name, style: SHORT, min_length: 1, max_length: 100 }
                types:
                  - type: support::APPLICATION
                    log_permissions_roles: ['100', '200']
                """, Ticket.class);

        assertEquals(Set.of("100", "200", "300"), ticket.applicationVoterRoleIds());
        assertEquals(3, ticket.settings().applicationVoting().requiredVotes(5));
    }

    @Test
    void rejectsInvalidPercentages() {
        assertThrows(IllegalArgumentException.class, () -> new ApplicationVotingSettings(3, 0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ApplicationVotingSettings(3, 101, List.of()));
    }
}

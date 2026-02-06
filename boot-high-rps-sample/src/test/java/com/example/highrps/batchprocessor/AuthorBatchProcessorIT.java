package com.example.highrps.batchprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.entities.AuthorEntity;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthorBatchProcessorIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        // clean DB and Redis markers
        authorRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void processUpserts_insertsNewAuthors_and_updatesExisting() {
        String payload1 =
                "{\"firstName\":\"John\",\"middleName\":\"M\",\"lastName\":\"Doe\",\"mobile\":1234567890,\"email\":\"John@Example.com\"}";
        String payload2 =
                "{\"firstName\":\"Jane\",\"middleName\":\"A\",\"lastName\":\"Smith\",\"mobile\":9876543210,\"email\":\"jane@example.com\"}";

        processor.processUpserts(List.of(payload1, payload2));

        List<AuthorEntity> all = authorRepository.findAll();
        assertThat(all).hasSize(2);

        Map<String, AuthorEntity> byEmail =
                all.stream().collect(Collectors.toMap(a -> a.getEmail().toLowerCase(), a -> a));

        assertThat(byEmail).containsKeys("john@example.com", "jane@example.com");
        assertThat(byEmail.get("john@example.com").getFirstName()).isEqualTo("John");

        // Now update John
        String updateJohn =
                "{\"firstName\":\"Johnny\",\"middleName\":\"M\",\"lastName\":\"Doe\",\"mobile\":1234567890,\"email\":\"john@example.com\"}";
        processor.processUpserts(List.of(updateJohn));

        AuthorEntity updated = authorRepository.findAll().stream()
                .filter(a -> "john@example.com".equalsIgnoreCase(a.getEmail()))
                .findFirst()
                .orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Johnny");
    }

    @Test
    void processUpserts_skipsWhenTombstoneExists() {
        String email = "tomb@example.com";
        String payload =
                "{\"firstName\":\"Tomb\",\"middleName\":null,\"lastName\":\"Stone\",\"mobile\":1111111111,\"email\":\""
                        + email + "\"}";

        // mark tombstone in Redis
        redisTemplate.opsForValue().set("deleted:authors:" + email, "1", Duration.ofSeconds(60));

        processor.processUpserts(List.of(payload));

        boolean exists = authorRepository.existsByEmailIgnoreCase(email);
        assertThat(exists).isFalse();
    }

    @Test
    void processDeletes_removesAuthors() {
        // create authors
        AuthorEntity a1 = new AuthorEntity()
                .setFirstName("A")
                .setLastName("LA")
                .setMobile(9876543210L)
                .setEmail("d1@example.com");
        AuthorEntity a2 = new AuthorEntity()
                .setFirstName("B")
                .setLastName("LB")
                .setMobile(9087654321L)
                .setEmail("d2@example.com");
        authorRepository.saveAll(List.of(a1, a2));

        assertThat(authorRepository.findAll()).hasSize(2);

        processor.processDeletes(List.of("d1@example.com", "D2@Example.com"));

        assertThat(authorRepository.findAll()).isEmpty();
    }
}

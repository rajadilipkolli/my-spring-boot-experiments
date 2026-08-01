package com.example.highrps.infrastructure.kafka.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.author.domain.AuthorEntity;
import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.post.domain.PostEntity;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchProcessorIdempotencyIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        super.clearDatabase();
    }

    @Test
    void testPostBatchProcessorIdempotency() {
        // Prepare author
        AuthorEntity author = new AuthorEntity("Test", "Author", "test@example.com", 1234567890L);
        authorRepository.saveAndFlush(author);

        String postPayload1 = """
            {
                "__entity": "post",
                "postId": "1001",
                "title": "Idempotent Title",
                "content": "Content 1",
                "email": "test@example.com",
                "details": {
                    "detailsKey": "testKey"
                }
            }
            """;

        String postPayload2 = """
            {
                "__entity": "post",
                "postId": "1001",
                "title": "Idempotent Title",
                "content": "Content 2",
                "email": "test@example.com",
                "details": {
                    "detailsKey": "testKey"
                }
            }
            """;

        // Enqueue duplicate payloads (same postId)
        String queueKey = appProperties.getBatch().getQueueKey();
        redisTemplate.opsForStream().add(queueKey, Map.of("payload", postPayload1));
        redisTemplate.opsForStream().add(queueKey, Map.of("payload", postPayload2));

        // Verify that only one record was inserted, and it's the latter one (upserted)
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<PostEntity> posts = postRepository.findByPostRefIdIn(List.of(1001L));
            assertThat(posts).hasSize(1);
            assertThat(posts.getFirst().getContent()).isEqualTo("Content 2");
        });
    }
}

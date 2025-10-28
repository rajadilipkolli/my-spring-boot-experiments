package com.example.learning.repository;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.ContainerConfig;
import com.example.learning.entity.Post;
import io.r2dbc.postgresql.codec.Json;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({ContainerConfig.class})
class PostRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PostRepositoryTest.class);

    @Autowired
    private R2dbcEntityTemplate template;

    @Autowired
    private PostRepository postRepository;

    @Test
    void testDatabaseClientExisted() {
        assertThat(template).isNotNull();
    }

    @Test
    void testPostRepositoryExisted() {
        assertThat(postRepository).isNotNull();
    }

    @Test
    void testQueryByExample() {
        var post = Post.builder().title("r2dbc").build();
        var exampleMatcher = ExampleMatcher.matching()
                .withMatcher("title", matcher -> matcher.ignoreCase().contains());
        var example = Example.of(post, exampleMatcher);
        var data = postRepository.findBy(
                example, postReactiveFluentQuery -> postReactiveFluentQuery.page(PageRequest.of(0, 10)));

        StepVerifier.create(data)
                .consumeNextWith(p -> {
                    log.debug("post data: {}", p.getContent());
                    assertThat(p.getTotalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void testInsertAndQuery() {
        this.template
                .insert(Post.builder()
                        .title("test title")
                        .content("content of test")
                        .metadata(Json.of("{\"tags\":[\"spring\", \"r2dbc\"]}"))
                        .build())
                .log()
                .then()
                .thenMany(this.postRepository.findByTitleContains("test%").take(1))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.getTitle()).isEqualTo("test title");
                    assertThat(p.getContent()).isEqualTo("content of test");
                    assertThat(p.getStatus()).isEqualTo(Post.Status.DRAFT);
                    assertThat(p.getMetadata()).isNotNull();
                    // As Auditing is not enabled
                    assertThat(p.getCreatedAt()).isNull();
                    assertThat(p.getUpdatedAt()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void testInsertAndCount() {
        this.template
                .insert(Post.builder()
                        .title("test title")
                        .content("content of test")
                        .build())
                .log()
                .then()
                .then(this.postRepository.countByTitleContaining("test"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void testInsertAndFindByTitleLike() {
        var data = IntStream.range(1, 101)
                .mapToObj(i -> Post.builder()
                        .title("test title#" + i)
                        .content("content of test")
                        .build())
                .collect(toList());
        this.postRepository
                .saveAll(data)
                .log()
                .then()
                .thenMany(this.postRepository.findByTitleLike("test%", PageRequest.of(0, 10)))
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }
}

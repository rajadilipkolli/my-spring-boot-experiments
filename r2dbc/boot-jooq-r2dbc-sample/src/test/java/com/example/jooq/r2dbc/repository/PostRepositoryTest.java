package com.example.jooq.r2dbc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.example.jooq.r2dbc.common.ContainerConfig;
import com.example.jooq.r2dbc.config.JooqConfiguration;
import com.example.jooq.r2dbc.config.R2dbcConfiguration;
import com.example.jooq.r2dbc.entities.Comment;
import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.entities.PostTagRelation;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.Status;
import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({
    ContainerConfig.class,
    JooqConfiguration.class,
    R2dbcConfiguration.class
})
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;

    @Autowired private TagRepository tagRepository;

    @Autowired private CommentRepository postCommentRepository;

    @Autowired private PostTagRepository postTagRepository;

    @BeforeEach
    void cleanup() {
        // Ensure existing data is deleted
        StepVerifier.create(
                        postTagRepository
                                .deleteAll()
                                .then(tagRepository.deleteAll())
                                .then(postCommentRepository.deleteAll())
                                .then(postRepository.deleteAll()))
                .expectSubscription()
                .expectComplete()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    void testInsertPostViaR2dbcAndRetrieveViaDSLContext() {

        Flux<PostResponse> postResponseFlux =
                // Step 1: Insert a new post
                createPost()
                        .flatMap(
                                post -> {
                                    UUID postId = post.getId();

                                    // Step 2: Insert a new tag
                                    return tagRepository
                                            .save(new Tags().setName("java"))
                                            .flatMap(
                                                    tag -> {
                                                        UUID tagId = tag.getId();

                                                        // Step 3: Link post and tag
                                                        return postTagRepository
                                                                .save(
                                                                        new PostTagRelation(
                                                                                postId, tagId))
                                                                .thenReturn(postId);
                                                    });
                                })

                        // Step 4: Insert comments
                        .flatMapMany(
                                postId ->
                                        createComments(postId, "test comments", "test comments 2"))
                        .thenMany(
                                // Step 5: Retrieve data using jOOQ
                                Flux.from(postRepository.retrievePostsWithCommentsAndTags()));

        StepVerifier.create(postResponseFlux)
                .expectNextMatches(
                        postResponse -> {
                            // Assertions for post data
                            verifyBasicPostResponse(postResponse);
                            assertThat(postResponse.comments()).isNotEmpty().hasSize(2);
                            assertThat(postResponse.tags()).isNotEmpty().hasSize(1);

                            // Assertions for
                            assertPostComments(postResponse);

                            // Assertions for tags
                            assertThat(postResponse.tags().getFirst()).isEqualTo("java");

                            return true;
                        })
                .expectComplete()
                .verify();
    }

    @Test
    void testInsertPostOnlyViaR2dbcAndRetrieveViaDSLContext() {

        Flux<PostResponse> postResponseFlux =
                // Step 1: Insert a new post
                createPost()
                        .thenMany(
                                // Step 2: Retrieve data using jOOQ
                                postRepository.retrievePostsWithCommentsAndTags());

        StepVerifier.create(postResponseFlux)
                .expectNextMatches(
                        postResponse -> {
                            // Assertions for post data
                            verifyBasicPostResponse(postResponse);
                            assertThat(postResponse.comments()).isEmpty();
                            assertThat(postResponse.tags()).isEmpty();

                            return true;
                        })
                .expectComplete()
                .verify();
    }

    @Test
    void testUniqueTagNameConstraintViolation() {
        // Create first tag
        Mono<Tags> firstTag = tagRepository.save(new Tags().setName("java"));

        // Try to create second tag with same name
        Mono<Tags> secondTag = firstTag.then(tagRepository.save(new Tags().setName("java")));

        StepVerifier.create(secondTag)
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof DataIntegrityViolationException
                                        && throwable.getMessage().contains("unique constraint"))
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testForeignKeyConstraintViolation() {
        // Try to save comment with non-existent post ID
        Mono<Comment> invalidComment =
                postCommentRepository.save(
                        new Comment()
                                .setPostId(UUID.randomUUID()) // Random non-existent ID
                                .setContent("test comment"));

        StepVerifier.create(invalidComment)
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof DataIntegrityViolationException
                                        && throwable
                                                .getMessage()
                                                .contains("foreign key constraint"))
                .verify();
    }

    @Test
    void testOptimisticLockingOnConcurrentPostUpdates() {
        // Create initial post
        Mono<Post> initialPost =
                postRepository.save(
                        new Post()
                                .setTitle("original")
                                .setContent("content")
                                .setVersion((short) 1));

        // Simulate concurrent updates
        Mono<Post> update1 =
                initialPost.flatMap(
                        post -> {
                            post.setTitle("update1");
                            return postRepository.save(post);
                        });

        Mono<Post> update2 =
                initialPost.flatMap(
                        post -> {
                            post.setTitle("update2");
                            return postRepository.save(post);
                        });

        StepVerifier.create(update1.then(update2))
                .expectErrorMatches(
                        throwable -> throwable instanceof OptimisticLockingFailureException)
                .verify();
    }

    @ParameterizedTest
    @MethodSource("invalidPostProvider")
    void testInsertPostWithInvalidDataShouldFail(
            String title, String content, Status status, String expectedError) {
        StepVerifier.create(
                        postRepository.save(
                                new Post().setTitle(title).setContent(content).setStatus(status)))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof DataIntegrityViolationException
                                        && throwable.getMessage().contains(expectedError))
                .verify();
    }

    private static Stream<Arguments> invalidPostProvider() {
        return Stream.of(
                Arguments.of(null, "content", Status.DRAFT, "title"),
                Arguments.of("", "content", Status.DRAFT, "title"),
                Arguments.of("   ", "content", Status.DRAFT, "title"),
                Arguments.of("title", "content", null, "status"),
                Arguments.of("title", "", Status.DRAFT, "content"),
                Arguments.of("title", "   ", Status.DRAFT, "content"),
                Arguments.of("title", null, Status.DRAFT, "content"));
    }

    private Mono<Post> createPost() {
        return postRepository.save(
                new Post().setTitle("jooq test").setContent("content of Jooq test"));
    }

    private void verifyBasicPostResponse(PostResponse postResponse) {
        assertThat(postResponse.id()).isInstanceOf(UUID.class);
        assertThat(postResponse.title()).isEqualTo("jooq test");
        assertThat(postResponse.content()).isEqualTo("content of Jooq test");
        assertThat(postResponse.createdBy()).isEqualTo("appUser");
        assertThat(postResponse.status()).isEqualTo(Status.DRAFT);
    }

    private void assertPostComments(PostResponse postResponse) {
        PostCommentResponse postCommentResponse = postResponse.comments().getFirst();
        assertThat(postCommentResponse.id()).isInstanceOf(UUID.class);
        assertThat(postCommentResponse.createdAt())
                .isNotNull()
                .isInstanceOf(LocalDateTime.class)
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(postCommentResponse.content()).isEqualTo("test comments 2");

        PostCommentResponse last = postResponse.comments().getLast();
        assertThat(last.id()).isInstanceOf(UUID.class);
        assertThat(last.createdAt())
                .isNotNull()
                .isInstanceOf(LocalDateTime.class)
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(last.createdAt()).isNotNull();
        assertThat(last.content()).isEqualTo("test comments");
    }

    private Flux<Comment> createComments(UUID postId, String... contents) {
        return Flux.fromArray(contents)
                .flatMap(
                        content ->
                                postCommentRepository.save(
                                        new Comment().setPostId(postId).setContent(content)));
    }
}

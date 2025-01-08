package com.example.jooq.r2dbc.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.ContainerConfig;
import com.example.jooq.r2dbc.config.JooqConfiguration;
import com.example.jooq.r2dbc.entities.Comment;
import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.entities.PostTagRelation;
import com.example.jooq.r2dbc.entities.Status;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({ContainerConfig.class, JooqConfiguration.class})
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;

    @Autowired private TagRepository tagRepository;

    @Autowired private CommentRepository postCommentRepository;

    @Autowired private PostTagRepository postTagRepository;

    @Autowired private DSLContext dslContext;

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
                .verify(Duration.ofSeconds(10));
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
                                Flux.from(postRepository.retrievePostsWithCommentsAndTags(null)));

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
                                postRepository.retrievePostsWithCommentsAndTags(null));

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
        assertThat(postCommentResponse.createdAt()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(postCommentResponse.content()).isEqualTo("test comments");
        PostCommentResponse last = postResponse.comments().getLast();
        assertThat(last.id()).isInstanceOf(UUID.class);
        assertThat(last.createdAt()).isNotNull();
        assertThat(last.content()).isEqualTo("test comments 2");
    }

    private Flux<Comment> createComments(UUID postId, String... contents) {
        return Flux.fromArray(contents)
                .flatMap(
                        content ->
                                postCommentRepository.save(
                                        new Comment().setPostId(postId).setContent(content)));
    }
}

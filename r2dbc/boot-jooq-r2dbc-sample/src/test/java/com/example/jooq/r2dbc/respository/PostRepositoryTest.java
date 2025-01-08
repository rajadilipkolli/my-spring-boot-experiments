package com.example.jooq.r2dbc.repository;

import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostComments.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Posts.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostsTags.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Tags.TAGS;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.ContainerConfig;
import com.example.jooq.r2dbc.config.JooqConfiguration;
import com.example.jooq.r2dbc.entities.Comment;
import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.entities.PostTagRelation;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
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
                .verifyComplete();
    }

    @Test
    void testInsertPostViaR2dbcAndRetrieveViaDSLContext() {

        Flux<PostResponse> postResponseFlux =
                // Step 1: Insert a new post
                postRepository
                        .save(new Post().setTitle("jooq test").setContent("content of Jooq test"))
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
                                        Flux.concat(
                                                postCommentRepository.save(
                                                        new Comment()
                                                                .setPostId(postId)
                                                                .setContent("test comments")),
                                                postCommentRepository.save(
                                                        new Comment()
                                                                .setPostId(postId)
                                                                .setContent("test comments 2"))))
                        .thenMany(
                                // Step 5: Retrieve data using jOOQ
                                Flux.from(
                                                dslContext
                                                        .select(
                                                                POSTS.ID,
                                                                POSTS.TITLE,
                                                                POSTS.CONTENT,
                                                                POSTS.CREATED_BY,
                                                                POSTS.STATUS,
                                                                DSL.multiset(
                                                                                DSL.select(
                                                                                                POST_COMMENTS
                                                                                                        .ID,
                                                                                                POST_COMMENTS
                                                                                                        .CONTENT,
                                                                                                POST_COMMENTS
                                                                                                        .CREATED_AT)
                                                                                        .from(
                                                                                                POST_COMMENTS)
                                                                                        .where(
                                                                                                POST_COMMENTS
                                                                                                        .POST_ID
                                                                                                        .eq(
                                                                                                                POSTS.ID)))
                                                                        .as("comments")
                                                                        .convertFrom(
                                                                                record ->
                                                                                        record.into(
                                                                                                PostCommentResponse
                                                                                                        .class)),
                                                                DSL.multiset(
                                                                                DSL.select(
                                                                                                TAGS.NAME)
                                                                                        .from(TAGS)
                                                                                        .join(
                                                                                                POSTS_TAGS)
                                                                                        .on(
                                                                                                TAGS
                                                                                                        .ID
                                                                                                        .eq(
                                                                                                                POSTS_TAGS
                                                                                                                        .TAG_ID))
                                                                                        .where(
                                                                                                POSTS_TAGS
                                                                                                        .POST_ID
                                                                                                        .eq(
                                                                                                                POSTS.ID)))
                                                                        .as("tags")
                                                                        .convertFrom(
                                                                                record ->
                                                                                        record.map(
                                                                                                Record1
                                                                                                        ::value1)))
                                                        .from(POSTS)
                                                        .orderBy(POSTS.CREATED_AT))
                                        .map(record -> record.into(PostResponse.class)));

        StepVerifier.create(postResponseFlux)
                .expectNextMatches(
                        postResponse -> {
                            // Assertions for post data
                            assertThat(postResponse.id()).isInstanceOf(UUID.class);
                            assertThat(postResponse.title()).isEqualTo("jooq test");
                            assertThat(postResponse.content()).isEqualTo("content of Jooq test");
                            assertThat(postResponse.createdBy()).isEqualTo("appUser");
                            assertThat(postResponse.comments()).isNotEmpty().hasSize(2);
                            assertThat(postResponse.tags()).isNotEmpty().hasSize(1);

                            // Assertions for
                            PostCommentResponse postCommentResponse =
                                    postResponse.comments().getFirst();
                            assertThat(postCommentResponse.id()).isInstanceOf(UUID.class);
                            assertThat(postCommentResponse.createdAt()).isNotNull();
                            assertThat(postCommentResponse.content()).isEqualTo("test comments");
                            PostCommentResponse last = postResponse.comments().getLast();
                            assertThat(last.id()).isInstanceOf(UUID.class);
                            assertThat(last.createdAt()).isNotNull();
                            assertThat(last.content()).isEqualTo("test comments 2");

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
                postRepository
                        .save(new Post().setTitle("jooq test").setContent("content of Jooq test"))
                        .thenMany(
                                // Step 2: Retrieve data using jOOQ
                                Flux.from(
                                                dslContext
                                                        .select(
                                                                POSTS.ID,
                                                                POSTS.TITLE,
                                                                POSTS.CONTENT,
                                                                POSTS.CREATED_BY,
                                                                POSTS.STATUS,
                                                                DSL.multiset(
                                                                                DSL.select(
                                                                                                POST_COMMENTS
                                                                                                        .ID,
                                                                                                POST_COMMENTS
                                                                                                        .CONTENT,
                                                                                                POST_COMMENTS
                                                                                                        .CREATED_AT)
                                                                                        .from(
                                                                                                POST_COMMENTS)
                                                                                        .where(
                                                                                                POST_COMMENTS
                                                                                                        .POST_ID
                                                                                                        .eq(
                                                                                                                POSTS.ID)))
                                                                        .as("comments")
                                                                        .convertFrom(
                                                                                record ->
                                                                                        record.into(
                                                                                                PostCommentResponse
                                                                                                        .class)),
                                                                DSL.multiset(
                                                                                DSL.select(
                                                                                                TAGS.NAME)
                                                                                        .from(TAGS)
                                                                                        .join(
                                                                                                POSTS_TAGS)
                                                                                        .on(
                                                                                                TAGS
                                                                                                        .ID
                                                                                                        .eq(
                                                                                                                POSTS_TAGS
                                                                                                                        .TAG_ID))
                                                                                        .where(
                                                                                                POSTS_TAGS
                                                                                                        .POST_ID
                                                                                                        .eq(
                                                                                                                POSTS.ID)))
                                                                        .as("tags")
                                                                        .convertFrom(
                                                                                record ->
                                                                                        record.map(
                                                                                                Record1
                                                                                                        ::value1)))
                                                        .from(POSTS)
                                                        .orderBy(POSTS.CREATED_AT))
                                        .map(record -> record.into(PostResponse.class)));

        StepVerifier.create(postResponseFlux)
                .expectNextMatches(
                        postResponse -> {
                            // Assertions for post data
                            assertThat(postResponse.id()).isInstanceOf(UUID.class);
                            assertThat(postResponse.title()).isEqualTo("jooq test");
                            assertThat(postResponse.content()).isEqualTo("content of Jooq test");
                            assertThat(postResponse.createdBy()).isEqualTo("appUser");
                            assertThat(postResponse.comments()).isEmpty();
                            assertThat(postResponse.tags()).isEmpty();

                            return true;
                        })
                .expectComplete()
                .verify();
    }
}

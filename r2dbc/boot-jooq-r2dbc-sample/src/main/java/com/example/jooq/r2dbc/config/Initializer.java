package com.example.jooq.r2dbc.config;

import static com.example.jooq.r2dbc.dbgen.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.dbgen.tables.PostComments.POST_COMMENTS;
import static com.example.jooq.r2dbc.dbgen.tables.Posts.POSTS;
import static com.example.jooq.r2dbc.dbgen.tables.Tags.TAGS;

import com.example.jooq.r2dbc.config.logging.Loggable;
import com.example.jooq.r2dbc.dbgen.tables.records.PostCommentsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.PostsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.PostsTagsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.TagsRecord;
import com.example.jooq.r2dbc.model.Status;
import com.example.jooq.r2dbc.repository.PostRepository;
import org.jooq.DSLContext;
import org.jooq.DeleteUsingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final DSLContext dslContext;
    private final PostRepository postRepository;

    public Initializer(DSLContext dslContext, PostRepository postRepository) {
        this.dslContext = dslContext;
        this.postRepository = postRepository;
    }

    @Override
    @Loggable
    public void run(String... args) {
        log.info("Running Initializer to use JOOQ only...");
        DeleteUsingStep<PostsTagsRecord> postsTagsRecordDeleteUsingStep =
                dslContext.deleteFrom(POSTS_TAGS);
        DeleteUsingStep<TagsRecord> tagsRecordDeleteUsingStep = dslContext.deleteFrom(TAGS);
        DeleteUsingStep<PostCommentsRecord> postCommentsRecordDeleteUsingStep =
                dslContext.deleteFrom(POST_COMMENTS);
        DeleteUsingStep<PostsRecord> postsRecordDeleteUsingStep = dslContext.deleteFrom(POSTS);

        Mono.from(postsTagsRecordDeleteUsingStep)
                .doOnError(e -> log.error("Failed to delete posts_tags", e))
                .then(Mono.from(tagsRecordDeleteUsingStep))
                .doOnError(e -> log.error("Failed to delete tags", e))
                .then(Mono.from(postCommentsRecordDeleteUsingStep))
                .doOnError(e -> log.error("Failed to delete post_comments", e))
                .then(Mono.from(postsRecordDeleteUsingStep))
                .doOnError(e -> log.error("Failed to delete posts", e))
                .then(
                        Mono.from(
                                dslContext
                                        .insertInto(POSTS)
                                        .columns(POSTS.TITLE, POSTS.CONTENT, POSTS.STATUS)
                                        .values(
                                                "jooq test",
                                                "content of Jooq test",
                                                Status.PUBLISHED.name())
                                        .returningResult(POSTS.ID)))
                .flatMap(
                        postId ->
                                Mono.from(
                                                dslContext
                                                        .insertInto(TAGS)
                                                        .columns(TAGS.NAME)
                                                        .values("java")
                                                        .returningResult(TAGS.ID))
                                        .flatMap(
                                                tagId ->
                                                        Mono.from(
                                                                dslContext
                                                                        .insertInto(POSTS_TAGS)
                                                                        .columns(
                                                                                POSTS_TAGS.TAG_ID,
                                                                                POSTS_TAGS.POST_ID)
                                                                        .values(
                                                                                tagId.component1(),
                                                                                postId.component1())
                                                                        .returningResult(
                                                                                POSTS_TAGS
                                                                                        .POST_ID)))
                                        .flatMapMany(
                                                pid ->
                                                        dslContext
                                                                .insertInto(POST_COMMENTS)
                                                                .columns(
                                                                        POST_COMMENTS.POST_ID,
                                                                        POST_COMMENTS.CONTENT)
                                                                .values(
                                                                        pid.component1(),
                                                                        "test comments")
                                                                .values(
                                                                        pid.component1(),
                                                                        "test comments 2")
                                                                .returningResult(POST_COMMENTS.ID))
                                        .collectList())
                .thenMany(postRepository.retrievePostsWithCommentsAndTags())
                .subscribe(
                        data -> log.debug("Retrieved data: {}", data),
                        error ->
                                log.error("Failed to retrieve posts with comments and tags", error),
                        () -> log.debug("done"));
    }
}

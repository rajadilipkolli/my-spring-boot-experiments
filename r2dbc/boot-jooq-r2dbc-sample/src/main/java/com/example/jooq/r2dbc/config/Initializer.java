package com.example.jooq.r2dbc.config;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostComments.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Posts.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Tags.TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.config.logging.Loggable;
import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final DSLContext dslContext;

    @Override
    @Loggable
    public void run(String... args) {
        log.info("Running Initializer.....");
        Mono.from(
                        dslContext
                                .insertInto(POSTS)
                                .columns(POSTS.TITLE, POSTS.CONTENT)
                                .values("jooq test", "content of Jooq test")
                                .returningResult(POSTS.ID))
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
                                        .collectList() // Collect all comment IDs into a list
                        )
                .flatMapMany(
                        it ->
                                dslContext
                                        .select(
                                                POSTS.ID,
                                                POSTS.TITLE,
                                                POSTS.CONTENT,
                                                multiset(
                                                                select(
                                                                                POST_COMMENTS.ID,
                                                                                POST_COMMENTS
                                                                                        .CONTENT,
                                                                                POST_COMMENTS
                                                                                        .CREATED_AT)
                                                                        .from(POST_COMMENTS)
                                                                        .where(
                                                                                POST_COMMENTS
                                                                                        .POST_ID.eq(
                                                                                        POSTS.ID)))
                                                        .as("comments")
                                                        .convertFrom(
                                                                record3s ->
                                                                        record3s.into(
                                                                                PostCommentResponse
                                                                                        .class)),
                                                multiset(
                                                                select(TAGS.NAME)
                                                                        .from(TAGS)
                                                                        .join(POSTS_TAGS)
                                                                        .on(
                                                                                TAGS.ID.eq(
                                                                                        POSTS_TAGS
                                                                                                .TAG_ID))
                                                                        .where(
                                                                                POSTS_TAGS.POST_ID
                                                                                        .eq(
                                                                                                POSTS.ID)))
                                                        .as("tags")
                                                        .convertFrom(
                                                                record ->
                                                                        record.map(
                                                                                Record1::value1)))
                                        .from(POSTS)
                                        .orderBy(POSTS.CREATED_AT))
                .subscribe(
                        data -> log.debug("Retrieved data: {}", data.into(PostResponse.class)),
                        error -> log.debug("error: " + error),
                        () -> log.debug("done"));
    }
}

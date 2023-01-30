package com.example.jooq.r2dbc.config;

import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostComments.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Posts.POSTS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.config.logging.Loggable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
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
                .flatMapMany(
                        id ->
                                dslContext
                                        .insertInto(POST_COMMENTS)
                                        .columns(POST_COMMENTS.POST_ID, POST_COMMENTS.CONTENT)
                                        .values(id.component1(), "test comments")
                                        .values(id.component1(), "test comments 2")
                                        .returningResult(POST_COMMENTS.ID))
                .flatMap(
                        it ->
                                dslContext
                                        .select(
                                                POSTS.TITLE,
                                                POSTS.CONTENT,
                                                multiset(
                                                                select(POST_COMMENTS.CONTENT)
                                                                        .from(POST_COMMENTS)
                                                                        .where(
                                                                                POST_COMMENTS
                                                                                        .POST_ID.eq(
                                                                                        POSTS.ID)))
                                                        .as("comments"))
                                        .from(POSTS)
                                        .orderBy(POSTS.CREATED_AT))
                .subscribe(
                        data -> log.debug("Retrieved data: {}", data.formatJSON()),
                        error -> log.debug("error: " + error),
                        () -> log.debug("done"));
    }
}

package com.example.jooq.r2dbc.config;

import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.Post.POST;
import static com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostComment.POST_COMMENT;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

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
    public void run(String... args) {
        log.info("Running Initializer.....");
        Mono.from(
                        dslContext
                                .insertInto(POST)
                                .columns(POST.TITLE, POST.CONTENT)
                                .values("jooq test", "content of Jooq test")
                                .returningResult(POST.ID))
                .flatMapMany(
                        id ->
                                dslContext
                                        .insertInto(POST_COMMENT)
                                        .columns(POST_COMMENT.POST_ID, POST_COMMENT.CONTENT)
                                        .values(id.component1(), "test comments")
                                        .values(id.component1(), "test comments 2"))
                .flatMap(
                        it ->
                                dslContext
                                        .select(
                                                POST.TITLE,
                                                POST.CONTENT,
                                                multiset(
                                                                select(POST_COMMENT.CONTENT)
                                                                        .from(POST_COMMENT)
                                                                        .where(
                                                                                POST_COMMENT.POST_ID
                                                                                        .eq(
                                                                                                POST.ID)))
                                                        .as("comments"))
                                        .from(POST)
                                        .orderBy(POST.CREATED_AT))
                .subscribe(
                        data -> log.debug("saving data: {}", data.formatJSON()),
                        error -> log.debug("error: " + error),
                        () -> log.debug("done"));
    }
}

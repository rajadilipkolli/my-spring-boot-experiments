package com.example.jooq.r2dbc.repository.custom.impl;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.repository.custom.CustomPostRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomPostRepositoryImpl extends JooqSorting implements CustomPostRepository {

    private static final Logger log = LoggerFactory.getLogger(CustomPostRepositoryImpl.class);
    private final DSLContext dslContext;

    public CustomPostRepositoryImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Mono<Page<PostResponse>> findByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching posts with keyword: {}, pageable: {}", keyword, pageable);
        // Build the where condition dynamically
        Condition condition = DSL.trueCondition();
        if (StringUtils.hasText(keyword)) {
            condition =
                    condition.and(
                            DSL.or(
                                    POSTS.TITLE.likeIgnoreCase(
                                            DSL.concat(
                                                    DSL.val("%"), DSL.val(keyword), DSL.val("%"))),
                                    POSTS.CONTENT.likeIgnoreCase(
                                            DSL.concat(
                                                    DSL.val("%"),
                                                    DSL.val(keyword),
                                                    DSL.val("%")))));
        }

        // Construct the main data SQL query
        var dataQuery =
                dslContext
                        .selectDistinct(
                                POSTS.ID,
                                POSTS.TITLE,
                                POSTS.CONTENT,
                                // Fetch comments as a multiset
                                multiset(
                                                select(
                                                                POST_COMMENTS.ID,
                                                                POST_COMMENTS.CONTENT,
                                                                POST_COMMENTS.CREATED_AT)
                                                        .from(POST_COMMENTS)
                                                        .where(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                                        .as("comments")
                                        .convertFrom(
                                                records -> records.into(PostCommentResponse.class)),
                                // Fetch tags as a multiset
                                multiset(
                                                select(TAGS.NAME)
                                                        .from(TAGS)
                                                        .join(POSTS_TAGS)
                                                        .on(TAGS.ID.eq(POSTS_TAGS.TAG_ID))
                                                        .where(POSTS_TAGS.POST_ID.eq(POSTS.ID)))
                                        .as("tags")
                                        .convertFrom(records -> records.map(Record1::value1)))
                        .from(POSTS)
                        .where(condition)
                        .orderBy(getSortFields(pageable.getSort(), POSTS))
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset());

        // Construct the count query
        var countQuery = dslContext.selectCount().from(POSTS).where(condition);

        // Execute queries reactively and build the result page
        return Mono.zip(
                        Flux.from(dataQuery)
                                .map(
                                        record ->
                                                new PostResponse(
                                                        record.value1(), // Post ID
                                                        record.value2(), // Post Title
                                                        record.value3(), // Post Content
                                                        record.value4(), // Comments
                                                        record.value5() // Tags
                                                        ))
                                .doOnError(
                                        e ->
                                                log.error(
                                                        "Error executing data query: {}",
                                                        e.getMessage()))
                                .collectList(),
                        Mono.from(countQuery).map(Record1::value1))
                .doOnError(e -> log.error("Error executing count query: {}", e.getMessage()))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}

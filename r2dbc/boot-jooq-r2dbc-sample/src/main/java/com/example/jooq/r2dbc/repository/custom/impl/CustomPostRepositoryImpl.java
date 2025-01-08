package com.example.jooq.r2dbc.repository.custom.impl;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.TAGS;

import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.repository.custom.CustomPostRepository;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        Field<String> searchValue = DSL.concat(DSL.val("%"), DSL.val(keyword), DSL.val("%"));
        Condition condition =
                DSL.or(
                        POSTS.TITLE.likeIgnoreCase(searchValue),
                        POSTS.CONTENT.likeIgnoreCase(searchValue));

        // Construct the main data SQL query
        var dataQuery =
                dslContext
                        .selectDistinct(
                                POSTS.ID, // Post ID
                                POSTS.TITLE, // Post Title
                                POSTS.CONTENT, // Post Content
                                POSTS.CREATED_BY, // Post Created By
                                // Fetch comments as a multiset
                                DSL.multiset(
                                                DSL.select(
                                                                POST_COMMENTS.ID,
                                                                POST_COMMENTS.CONTENT,
                                                                POST_COMMENTS.CREATED_AT)
                                                        .from(POST_COMMENTS)
                                                        .where(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                                        .as("comments")
                                        .convertFrom(
                                                records -> records.into(PostCommentResponse.class)),
                                // Fetch tags as a multiset
                                DSL.multiset(
                                                DSL.select(TAGS.NAME)
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

        // Execute the data and count queries reactively and build the result page
        return Mono.zip(
                        // Fetch data query
                        Flux.from(dataQuery)
                                .map(
                                        record -> {
                                            // Map each record into PostResponse
                                            return new PostResponse(
                                                    record.get(POSTS.ID), // Post ID
                                                    record.get(POSTS.TITLE), // Post Title
                                                    record.get(POSTS.CONTENT), // Post Content
                                                    record.get(POSTS.CREATED_BY), // Created By
                                                    record.get("comments", List.class), // Comments
                                                    record.get("tags", List.class) // Tags
                                                    );
                                        })
                                .doOnError(
                                        e ->
                                                log.error(
                                                        "Error fetching data query: {}",
                                                        e.getMessage()))
                                .collectList(), // Collect the result into a list
                        // Fetch count query
                        Mono.from(countQuery).map(Record1::value1) // Get the count value
                        )
                .doOnError(e -> log.error("Error executing queries: {}", e.getMessage()))
                .map(
                        tuple ->
                                new PageImpl<>(
                                        tuple.getT1(),
                                        pageable,
                                        tuple.getT2())); // Map into PageImpl
    }
}

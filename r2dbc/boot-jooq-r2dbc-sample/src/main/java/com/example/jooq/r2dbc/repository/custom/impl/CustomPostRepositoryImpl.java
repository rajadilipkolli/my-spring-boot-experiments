package com.example.jooq.r2dbc.repository.custom.impl;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.TAGS;

import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.repository.custom.CustomPostRepository;
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
        // Construct the count query
        var countQuery = dslContext.selectCount().from(POSTS).where(condition);

        // Execute the data and count queries reactively and build the result page
        return Mono.zip(
                        // Fetch data query
                        retrievePostsWithCommentsAndTags(dslContext, condition)
                                .doOnError(
                                        e ->
                                                log.error(
                                                        "Error fetching data query: {}",
                                                        e.getMessage(),
                                                        e))
                                .collectList(), // Collect the result into a list
                        // Fetch count query
                        Mono.from(countQuery).map(Record1::value1) // Get the count value
                        )
                .doOnError(e -> log.error("Error executing queries: {}", e.getMessage(), e))
                .map(
                        tuple ->
                                new PageImpl<>(
                                        tuple.getT1(),
                                        pageable,
                                        tuple.getT2())); // Map into PageImpl
    }

    public static Flux<PostResponse> retrievePostsWithCommentsAndTags(
            DSLContext dslContext, Condition condition) {
        // Start with a base condition
        Condition whereCondition = DSL.trueCondition();

        // Add the provided condition if it is not null
        if (condition != null) {
            whereCondition = whereCondition.and(condition);
        }

        // Construct the query
        return Flux.from(
                        dslContext
                                .select(
                                        POSTS.ID, // Post ID
                                        POSTS.TITLE, // Post Title
                                        POSTS.CONTENT, // Post Content
                                        POSTS.CREATED_BY, // Post Created By
                                        POSTS.STATUS, // Post status
                                        // Fetch comments as a multiset
                                        DSL.multiset(
                                                        DSL.select(
                                                                        POST_COMMENTS.ID,
                                                                        POST_COMMENTS.CONTENT,
                                                                        POST_COMMENTS.CREATED_AT)
                                                                .from(POST_COMMENTS)
                                                                .where(
                                                                        POST_COMMENTS.POST_ID.eq(
                                                                                POSTS.ID)))
                                                .as("comments")
                                                .convertFrom(
                                                        record ->
                                                                record.into(
                                                                        PostCommentResponse.class)),
                                        // Fetch tags as a multiset
                                        DSL.multiset(
                                                        DSL.select(TAGS.NAME)
                                                                .from(TAGS)
                                                                .join(POSTS_TAGS)
                                                                .on(TAGS.ID.eq(POSTS_TAGS.TAG_ID))
                                                                .where(
                                                                        POSTS_TAGS.POST_ID.eq(
                                                                                POSTS.ID)))
                                                .as("tags")
                                                .convertFrom(record -> record.map(Record1::value1)))
                                .from(POSTS)
                                .where(whereCondition) // Apply the dynamic where condition
                                .orderBy(POSTS.CREATED_AT))
                .map(record -> record.into(PostResponse.class));
    }
}

package com.example.jooq.r2dbc.repository.custom.impl;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.*;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.model.response.PostCommentResponse;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.repository.custom.CustomPostRepository;
import com.example.jooq.r2dbc.testcontainersflyway.db.tables.PostComments;
import com.example.jooq.r2dbc.testcontainersflyway.db.tables.Posts;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomPostRepositoryImpl extends JooqSorting implements CustomPostRepository {

    private final DSLContext dslContext;

    public CustomPostRepositoryImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Mono<Page<PostResponse>> findByKeyword(String keyword, Pageable pageable) {
        Condition where = DSL.trueCondition();
        if (StringUtils.hasText(keyword)) {
            where = where.and(POSTS.TITLE.likeIgnoreCase("%" + keyword + "%"));
        }
        var dataSql =
                dslContext
                        .select(
                                POSTS.ID,
                                POSTS.TITLE,
                                POSTS.CONTENT,
                                multiset(
                                                select(
                                                                PostComments.POST_COMMENTS.ID,
                                                                PostComments.POST_COMMENTS.CONTENT,
                                                                PostComments.POST_COMMENTS
                                                                        .CREATED_AT)
                                                        .from(PostComments.POST_COMMENTS)
                                                        .where(
                                                                PostComments.POST_COMMENTS.POST_ID
                                                                        .eq(Posts.POSTS.ID)))
                                        .as("comments")
                                        .convertFrom(
                                                record3s ->
                                                        record3s.into(PostCommentResponse.class)),
                                multiset(
                                                select(TAGS.NAME)
                                                        .from(TAGS)
                                                        .join(POSTS_TAGS)
                                                        .on(TAGS.ID.eq(POSTS_TAGS.TAG_ID))
                                                        .where(POSTS_TAGS.POST_ID.eq(POSTS.ID)))
                                        .as("tags")
                                        .convertFrom(record -> record.map(Record1::value1)))
                        .from(POSTS.leftJoin(POST_COMMENTS).on(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                        .where(where)
                        .groupBy(POSTS.ID)
                        .orderBy(getSortFields(pageable.getSort(), POSTS))
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset());

        var countSql = dslContext.selectCount().from(POSTS).where(where);

        return Mono.zip(
                        Flux.from(dataSql)
                                .map(
                                        r ->
                                                new PostResponse(
                                                        r.value1(),
                                                        r.value2(),
                                                        r.value3(),
                                                        r.value4(),
                                                        r.value5()))
                                .collectList(),
                        Mono.from(countSql).map(Record1::value1))
                .map(it -> new PageImpl<>(it.getT1(), pageable, it.getT2()));
    }
}

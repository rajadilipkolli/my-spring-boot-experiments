package com.example.jooq.r2dbc.service;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.model.response.PostSummary;
import com.example.jooq.r2dbc.repository.CommentRepository;
import com.example.jooq.r2dbc.repository.PostRepository;
import com.example.jooq.r2dbc.repository.PostTagRelationRepository;
import com.example.jooq.r2dbc.repository.TagRepository;
import com.example.jooq.r2dbc.testcontainersflyway.db.tables.records.PostsTagsRecord;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final DSLContext dslContext;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostTagRelationRepository postTagRelRepository;

    public Flux<PostSummary> findAll() {
        var post = POSTS;
        var postsTags = POSTS_TAGS;
        var tags = TAGS;
        var postComment = POST_COMMENTS;
        var sql =
                dslContext
                        .select(
                                post.ID,
                                post.TITLE,
                                DSL.field("count(post_comments.id)", SQLDataType.BIGINT),
                                multiset(
                                                select(tags.NAME)
                                                        .from(tags)
                                                        .join(postsTags)
                                                        .on(tags.ID.eq(postsTags.TAG_ID))
                                                        .where(postsTags.POST_ID.eq(post.ID)))
                                        .as("tags"))
                        .from(post.leftJoin(postComment).on(postComment.POST_ID.eq(post.ID)))
                        .groupBy(post.ID)
                        .orderBy(post.CREATED_AT);
        return Flux.from(sql)
                .map(
                        r ->
                                new PostSummary(
                                        r.value1(),
                                        r.value2(),
                                        r.value3(),
                                        r.value4().map(Record1::value1)));
    }

    public Mono<UUID> create(CreatePostCommand createPostCommand) {
        var post = POSTS;
        var postsTags = POSTS_TAGS;
        var sqlInsertPost =
                dslContext
                        .insertInto(post)
                        .columns(post.TITLE, post.CONTENT)
                        .values(createPostCommand.title(), createPostCommand.content())
                        .returningResult(post.ID);
        return Mono.from(sqlInsertPost)
                .flatMap(
                        id -> {
                            Collection<?> tags =
                                    createPostCommand.tagId().stream()
                                            .map(
                                                    tag -> {
                                                        PostsTagsRecord r = postsTags.newRecord();
                                                        r.setPostId(id.value1());
                                                        r.setTagId(tag);
                                                        return r;
                                                    })
                                            .toList();
                            return Mono.from(
                                            dslContext
                                                    .insertInto(postsTags)
                                                    .columns(postsTags.POST_ID, postsTags.TAG_ID)
                                                    .values(tags))
                                    .map(
                                            r -> {
                                                log.debug("inserted tags:: {}", r);
                                                return id;
                                            });
                        })
                .map(Record1::value1);
    }

    public Mono<PaginatedResult> findByKeyword(String keyword, int offset, int limit) {
        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = TAGS;
        var c = POST_COMMENTS;

        Condition where = DSL.trueCondition();
        if (StringUtils.hasText(keyword)) {
            where = where.and(p.TITLE.likeIgnoreCase("%" + keyword + "%"));
        }
        var dataSql =
                dslContext
                        .select(
                                p.ID,
                                p.TITLE,
                                DSL.field("count(comments.id)", SQLDataType.BIGINT),
                                multiset(
                                                select(t.NAME)
                                                        .from(t)
                                                        .join(pt)
                                                        .on(t.ID.eq(pt.TAG_ID))
                                                        .where(pt.POST_ID.eq(p.ID)))
                                        .as("tags"))
                        .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                        .where(where)
                        .groupBy(p.ID)
                        .orderBy(p.CREATED_AT)
                        .limit(offset, limit);

        val countSql =
                dslContext.select(DSL.field("count(*)", SQLDataType.BIGINT)).from(p).where(where);

        return Mono.zip(
                        Flux.from(dataSql)
                                .map(
                                        r ->
                                                new PostSummary(
                                                        r.value1(),
                                                        r.value2(),
                                                        r.value3(),
                                                        r.value4().map(Record1::value1)))
                                .collectList(),
                        Mono.from(countSql).map(Record1::value1))
                .map(it -> new PaginatedResult(it.getT1(), it.getT2()));
    }

    public Mono<Post> findById(String id) {
        return this.postRepository.findById(UUID.fromString(id));
    }

    public Mono<Post> save(Post post) {
        return this.postRepository.save(post);
    }
}

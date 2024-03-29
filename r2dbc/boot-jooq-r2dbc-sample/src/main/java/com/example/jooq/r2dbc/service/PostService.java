package com.example.jooq.r2dbc.service;

import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.testcontainersflyway.db.Tables.TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.model.request.CreatePostComment;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.model.response.PostSummary;
import com.example.jooq.r2dbc.repository.PostRepository;
import com.example.jooq.r2dbc.testcontainersflyway.db.tables.records.PostCommentsRecord;
import com.example.jooq.r2dbc.testcontainersflyway.db.tables.records.PostsTagsRecord;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final DSLContext dslContext;
    private final PostRepository postRepository;

    public Flux<PostSummary> findAll() {
        var sql =
                dslContext
                        .select(
                                POSTS.ID,
                                POSTS.TITLE,
                                DSL.field("count(post_comments.id)", SQLDataType.BIGINT),
                                multiset(
                                                select(TAGS.NAME)
                                                        .from(TAGS)
                                                        .join(POSTS_TAGS)
                                                        .on(TAGS.ID.eq(POSTS_TAGS.TAG_ID))
                                                        .where(POSTS_TAGS.POST_ID.eq(POSTS.ID)))
                                        .as("tags")
                                        .convertFrom(record -> record.map(Record1::value1)))
                        .from(POSTS.leftJoin(POST_COMMENTS).on(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                        .groupBy(POSTS.ID)
                        .orderBy(POSTS.CREATED_AT);
        return Flux.from(sql)
                .map(r -> new PostSummary(r.value1(), r.value2(), r.value3(), r.value4()));
    }

    public Mono<UUID> create(CreatePostCommand createPostCommand) {

        var createPostSQL =
                dslContext
                        .insertInto(POSTS)
                        .columns(POSTS.TITLE, POSTS.CONTENT)
                        .values(createPostCommand.title(), createPostCommand.content())
                        .returningResult(POSTS.ID);

        return Flux.fromIterable(createPostCommand.tagName())
                .flatMap(this::fetchOrInsertTag)
                .collectList()
                .flatMap(
                        tagIdList ->
                                Mono.from(createPostSQL)
                                        .flatMap(
                                                postIdRecord ->
                                                        insertIntoPostTags(
                                                                tagIdList,
                                                                postIdRecord.component1())));
    }

    private Mono<UUID> insertIntoPostTags(List<UUID> tagIdList, UUID postId) {
        List<PostsTagsRecord> tags =
                tagIdList.stream()
                        .map(
                                tagId -> {
                                    PostsTagsRecord r = POSTS_TAGS.newRecord();
                                    r.setPostId(postId);
                                    r.setTagId(tagId);
                                    return r;
                                })
                        .toList();
        return Mono.from(
                        dslContext
                                .insertInto(POSTS_TAGS)
                                .columns(POSTS_TAGS.POST_ID, POSTS_TAGS.TAG_ID)
                                .valuesOfRecords(tags)
                                .returning())
                .map(
                        r -> {
                            log.debug("inserted tags:: {}", r);
                            return r.component1();
                        });
    }

    private Mono<UUID> fetchOrInsertTag(String tagName) {

        // Check if the tag with the given Name exists
        return Mono.from(dslContext.select(TAGS.ID).from(TAGS).where(TAGS.NAME.eq(tagName)))
                .switchIfEmpty(
                        Mono.from(
                                dslContext
                                        .insertInto(TAGS)
                                        .columns(TAGS.NAME)
                                        .values(tagName)
                                        .returningResult(TAGS.ID)))
                .map(Record1::value1);
    }

    public Mono<PaginatedResult<PostResponse>> findByKeyword(String keyword, Pageable pageable) {
        log.debug(
                "findByKeyword with keyword :{} with offset :{} and limit :{}",
                keyword,
                pageable.getOffset(),
                pageable.getPageSize());

        return this.postRepository.findByKeyword(keyword, pageable).map(PaginatedResult::new);
    }

    public Mono<Post> findById(String id) {
        return this.postRepository.findById(UUID.fromString(id));
    }

    public Mono<Post> save(Post post) {
        return this.postRepository.save(post);
    }

    public Mono<UUID> addCommentToPostId(String postId, CreatePostComment createPostComment) {
        return findById(postId)
                .flatMap(
                        post -> {
                            PostCommentsRecord r = POST_COMMENTS.newRecord();
                            r.setContent(createPostComment.content());
                            r.setPostId(post.getId());

                            return Mono.from(
                                    dslContext
                                            .insertInto(POST_COMMENTS)
                                            .set(r)
                                            .returningResult(POST_COMMENTS.ID));
                        })
                .map(Record1::component1);
    }
}

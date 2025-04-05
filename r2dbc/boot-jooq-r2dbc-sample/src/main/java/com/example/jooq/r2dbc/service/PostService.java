package com.example.jooq.r2dbc.service;

import static com.example.jooq.r2dbc.dbgen.Tables.POSTS;
import static com.example.jooq.r2dbc.dbgen.Tables.POSTS_TAGS;
import static com.example.jooq.r2dbc.dbgen.Tables.POST_COMMENTS;
import static com.example.jooq.r2dbc.dbgen.Tables.TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.jooq.r2dbc.dbgen.tables.records.PostCommentsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.PostsTagsRecord;
import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.model.Status;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.model.request.CreatePostComment;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.model.response.PostResponse;
import com.example.jooq.r2dbc.model.response.PostSummary;
import com.example.jooq.r2dbc.repository.PostRepository;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final DSLContext dslContext;
    private final PostRepository postRepository;

    public PostService(DSLContext dslContext, PostRepository postRepository) {
        this.dslContext = dslContext;
        this.postRepository = postRepository;
    }

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
                        .columns(POSTS.TITLE, POSTS.CONTENT, POSTS.STATUS)
                        .values(
                                createPostCommand.title(),
                                createPostCommand.content(),
                                Status.DRAFT.name())
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
        // Check if the keyword has text
        if (!StringUtils.hasText(keyword)) {
            log.debug("findByKeyword called with empty or null keyword");
            return Mono.empty();
        }
        // Sanitize the keyword to avoid injection-like issues
        String sanitizedKeyword = keyword.replaceAll("[^a-zA-Z0-9\\s-]", "_");
        log.debug(
                "findByKeyword [keyword: {}, sanitized: {}, page: {}, size: {}, sort: {}]",
                keyword,
                sanitizedKeyword,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

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

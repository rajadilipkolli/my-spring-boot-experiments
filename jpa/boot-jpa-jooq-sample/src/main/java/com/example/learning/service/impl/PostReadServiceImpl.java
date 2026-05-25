package com.example.learning.service.impl;

import static com.example.learning.db.tables.PostComments.POST_COMMENTS;
import static com.example.learning.db.tables.PostDetails.POST_DETAILS;
import static com.example.learning.db.tables.PostTag.POST_TAG;
import static com.example.learning.db.tables.Posts.POSTS;
import static com.example.learning.db.tables.Tags.TAGS;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.learning.exception.PostNotFoundException;
import com.example.learning.model.response.PostCommentResponse;
import com.example.learning.model.response.PostResponse;
import com.example.learning.model.response.TagResponse;
import com.example.learning.service.PostReadService;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostReadServiceImpl implements PostReadService {

    private final DSLContext dslContext;

    public PostReadServiceImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public PostResponse fetchPostByUserNameAndTitle(String userName, String title) {
        PostResponse response = dslContext
                .select(
                        POSTS.TITLE,
                        POSTS.CONTENT,
                        POSTS.PUBLISHED,
                        POSTS.PUBLISHED_AT,
                        POST_DETAILS.CREATED_BY,
                        POST_DETAILS.CREATED_AT,
                        fetchCommentsSubQuery(),
                        fetchTagsSubQuery())
                .from(POSTS)
                .join(POST_DETAILS)
                .on(POSTS.ID.eq(POST_DETAILS.ID))
                .where(POST_DETAILS.CREATED_BY.eq(userName).and(POSTS.TITLE.eq(title)))
                .fetchOneInto(PostResponse.class);

        if (response == null) {
            throw new PostNotFoundException(
                    String.format("Post with title '%s' not found for user '%s'", title, userName));
        }

        return response;
    }

    @Override
    public boolean existsByTitleIgnoreCase(String title) {
        return dslContext.fetchExists(dslContext.selectOne().from(POSTS).where(POSTS.TITLE.equalIgnoreCase(title)));
    }

    @Override
    public boolean existsByTitleAndDetailsCreatedBy(String title, String createdBy) {
        return dslContext.fetchExists(dslContext
                .selectOne()
                .from(POSTS)
                .join(POST_DETAILS)
                .on(POSTS.ID.eq(POST_DETAILS.ID))
                .where(POSTS.TITLE.eq(title).and(POST_DETAILS.CREATED_BY.eq(createdBy))));
    }

    private Field<List<TagResponse>> fetchTagsSubQuery() {
        return multiset(select(TAGS.TAG_NAME, TAGS.TAG_DESCRIPTION)
                        .from(TAGS)
                        .join(POST_TAG)
                        .on(POSTS.ID.eq(POST_TAG.POST_ID))
                        .where(POST_TAG.TAG_ID.eq(TAGS.ID)))
                .as("tags")
                .convertFrom(r -> r.map(mapping(TagResponse::new)));
    }

    private Field<List<PostCommentResponse>> fetchCommentsSubQuery() {
        return multiset(select(
                                POST_COMMENTS.TITLE,
                                POST_COMMENTS.CONTENT,
                                POST_COMMENTS.PUBLISHED,
                                POST_COMMENTS.PUBLISHED_AT)
                        .from(POST_COMMENTS)
                        .where(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                .as("comments")
                .convertFrom(r -> r.map(mapping(PostCommentResponse::new)));
    }
}

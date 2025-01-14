package com.example.learning.service.impl;

import static com.example.learning.db.tables.PostComments.POST_COMMENTS;
import static com.example.learning.db.tables.PostDetails.POST_DETAILS;
import static com.example.learning.db.tables.PostTag.POST_TAG;
import static com.example.learning.db.tables.Posts.POSTS;
import static com.example.learning.db.tables.Tags.TAGS;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostCommentResponse;
import com.example.learning.model.response.PostResponse;
import com.example.learning.model.response.TagResponse;
import com.example.learning.service.PostService;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("JooqPostService")
@Transactional(readOnly = true)
public class JooqPostServiceImpl implements PostService {

    private final DSLContext dslContext;

    public JooqPostServiceImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public void createPost(PostRequest postRequest, String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PostResponse fetchPostByUserNameAndTitle(String userName, String title) {

        return dslContext
                .select(
                        POSTS.TITLE,
                        POSTS.CONTENT,
                        POSTS.PUBLISHED,
                        POSTS.PUBLISHED_AT,
                        POST_DETAILS.CREATED_BY,
                        POST_DETAILS.CREATED_AT,
                        multiset(select(
                                                POST_COMMENTS.TITLE,
                                                POST_COMMENTS.CONTENT,
                                                POST_COMMENTS.PUBLISHED,
                                                POST_COMMENTS.PUBLISHED_AT)
                                        .from(POST_COMMENTS)
                                        .where(POST_COMMENTS.POST_ID.eq(POSTS.ID)))
                                .as("comments")
                                .convertFrom(r -> r.map(mapping(PostCommentResponse::new))),
                        multiset(select(TAGS.TAG_NAME, TAGS.TAG_DESCRIPTION)
                                        .from(TAGS)
                                        .join(POST_TAG)
                                        .on(POSTS.ID.eq(POST_TAG.POST_ID))
                                        .where(POST_TAG.TAG_ID.eq(TAGS.ID)))
                                .as("tags")
                                .convertFrom(r -> r.map(mapping(TagResponse::new))))
                .from(POSTS)
                .join(POST_DETAILS)
                .on(POSTS.ID.eq(POST_DETAILS.ID))
                .where(POST_DETAILS.CREATED_BY.eq(userName).and(POSTS.TITLE.eq(title)))
                .fetchOneInto(PostResponse.class);
    }
}

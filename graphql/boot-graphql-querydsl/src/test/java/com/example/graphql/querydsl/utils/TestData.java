package com.example.graphql.querydsl.utils;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.entities.PostDetails;
import java.time.LocalDateTime;

public final class TestData {

    public static Post getPost(String title, String content, String review) {
        Post post = new Post().setTitle(title).setContent(content);
        post.addDetails(new PostDetails()
                .setCreatedOn(LocalDateTime.of(2023, 12, 31, 10, 35, 45, 99))
                .setCreatedBy("appUser"));
        if (null != review) {
            post.addComment(
                    new PostComment().setReview(review).setCreatedOn(LocalDateTime.of(2023, 12, 31, 10, 35, 45, 99)));
        }
        return post;
    }
}

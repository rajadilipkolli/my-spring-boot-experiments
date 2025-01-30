package com.example.learning.service;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;

public interface PostService {

    /**
     * Fetches a post by username and title.
     *
     * @param userName the username of the post author
     * @param title the title of the post
     * @return the post response containing post details, comments, and tags
     * @throws com.example.learning.exception.ResourceNotFoundException if the post is not found
     */
    PostResponse fetchPostByUserNameAndTitle(String userName, String title);

    void createPost(PostRequest postRequest, String userName);

    PostResponse updatePostByUserNameAndTitle(PostRequest postRequest, String userName, String title);

    void deletePostByTitleAndUserName(String userName, String title);
}

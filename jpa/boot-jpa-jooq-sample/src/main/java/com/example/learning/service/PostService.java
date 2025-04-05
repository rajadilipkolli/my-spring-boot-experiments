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

    /**
     * Updates a post by username and title.
     *
     * @param postRequest the updated post data
     * @param userName the username of the post author
     * @param title the title of the post
     * @return the updated post response
     * @throws com.example.learning.exception.ResourceNotFoundException if the post is not found
     */
    PostResponse updatePostByUserNameAndTitle(PostRequest postRequest, String userName, String title);

    /**
     * Deletes a post by username and title.
     *
     * @param userName the username of the post author
     * @param title the title of the post
     * @throws com.example.learning.exception.ResourceNotFoundException if the post is not found
     */
    void deletePostByUserNameAndTitle(String userName, String title);
}

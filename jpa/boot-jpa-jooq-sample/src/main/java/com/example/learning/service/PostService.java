package com.example.learning.service;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;

public interface PostService {

    void createPost(PostRequest postRequest, String userName);

    PostResponse fetchPostByUserNameAndTitle(String userName, String title);
}

package com.example.learning.service;

import com.example.learning.model.request.PostRequest;

public interface PostService {

    void createPost(PostRequest postRequest, String userName);
}

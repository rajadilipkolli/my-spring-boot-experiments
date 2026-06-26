package com.example.learning.service;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;

public interface PostWriteService {

    void createPost(PostRequest postRequest, String userName);

    PostResponse updatePostByUserNameAndTitle(PostRequest request, String userName, String title);

    void deletePostByUserNameAndTitle(String userName, String title);
}

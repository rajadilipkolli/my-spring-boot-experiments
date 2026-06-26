package com.example.learning.service;

import com.example.learning.model.response.PostResponse;

public interface PostReadService {

    PostResponse fetchPostByUserNameAndTitle(String userName, String title);

    boolean existsByTitleAndDetailsCreatedBy(String title, String createdBy);
}

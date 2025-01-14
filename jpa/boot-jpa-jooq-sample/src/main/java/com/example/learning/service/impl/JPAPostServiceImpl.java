package com.example.learning.service.impl;

import com.example.learning.entities.Post;
import com.example.learning.mapper.PostMapper;
import com.example.learning.model.request.PostRequest;
import com.example.learning.repository.PostRepository;
import com.example.learning.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("jpaPostService")
public class JPAPostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostRepository postRepository;

    public JPAPostServiceImpl(PostMapper postMapper, PostRepository postRepository) {
        this.postMapper = postMapper;
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public void createPost(PostRequest postRequest, String userName) {
        Post post = this.postMapper.postRequestToEntity(postRequest, userName);
        this.postRepository.save(post);
    }
}

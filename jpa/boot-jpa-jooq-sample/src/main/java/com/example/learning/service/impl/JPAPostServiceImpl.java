package com.example.learning.service.impl;

import com.example.learning.entities.Post;
import com.example.learning.exception.PostAlreadyExistsException;
import com.example.learning.mapper.PostMapper;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import com.example.learning.repository.PostRepository;
import com.example.learning.service.PostService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("jpaPostService")
@Transactional(readOnly = true)
public class JPAPostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(JPAPostServiceImpl.class);

    private final PostMapper postMapper;
    private final PostRepository postRepository;

    public JPAPostServiceImpl(PostMapper postMapper, PostRepository postRepository) {
        this.postMapper = postMapper;
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public void createPost(PostRequest postRequest, String userName) {
        Optional<Post> byTitleIgnoreCase = this.postRepository.findByTitleIgnoreCase(postRequest.title());
        if (byTitleIgnoreCase.isPresent()) {
            log.debug("Post with title '{}' already exists", postRequest.title());
            throw new PostAlreadyExistsException(postRequest.title());
        } else {
            log.debug("Creating post with title '{}' for user '{}'", postRequest.title(), userName);
            Post post = this.postMapper.postRequestToEntity(postRequest, userName);
            this.postRepository.save(post);
        }
    }

    @Override
    public PostResponse fetchPostByUserNameAndTitle(String userName, String title) {
        throw new UnsupportedOperationException();
    }
}

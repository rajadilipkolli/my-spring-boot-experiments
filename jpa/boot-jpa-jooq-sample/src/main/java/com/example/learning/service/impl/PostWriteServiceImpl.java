package com.example.learning.service.impl;

import com.example.learning.entities.Post;
import com.example.learning.exception.PostNotFoundException;
import com.example.learning.mapper.PostMapper;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import com.example.learning.repository.PostRepository;
import com.example.learning.service.PostWriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostWriteServiceImpl implements PostWriteService {

    private static final Logger log = LoggerFactory.getLogger(PostWriteServiceImpl.class);

    private final PostMapper postMapper;
    private final PostRepository postRepository;

    public PostWriteServiceImpl(PostMapper postMapper, PostRepository postRepository) {
        this.postMapper = postMapper;
        this.postRepository = postRepository;
    }

    @Override
    public void createPost(PostRequest postRequest, String userName) {
        Post post = this.postMapper.postRequestToEntity(postRequest, userName);
        this.postRepository.save(post);
    }

    @Override
    public PostResponse updatePostByUserNameAndTitle(PostRequest postRequest, String userName, String title) {
        return this.postRepository
                .findByTitleAndDetails_CreatedBy(title, userName)
                .map(post -> {
                    log.debug("Updating post with title '{}' for user '{}'", title, userName);
                    this.postMapper.updateReferenceValues(postRequest, post);
                    return this.postRepository.save(post);
                })
                .map(this.postMapper::postToPostResponse)
                .orElseThrow(() -> {
                    log.debug("Post with title '{}' for user '{}' not found", title, userName);
                    return new PostNotFoundException(
                            String.format("Post with title '%s' not found for user '%s'", title, userName));
                });
    }

    @Override
    public void deletePostByUserNameAndTitle(String userName, String title) {
        this.postRepository.deleteByTitleAndCreatedBy(title, userName);
    }
}

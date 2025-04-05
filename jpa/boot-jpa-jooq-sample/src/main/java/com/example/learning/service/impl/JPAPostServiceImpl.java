package com.example.learning.service.impl;

import com.example.learning.entities.Post;
import com.example.learning.exception.PostAlreadyExistsException;
import com.example.learning.exception.PostNotFoundException;
import com.example.learning.mapper.PostMapper;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import com.example.learning.repository.PostRepository;
import com.example.learning.service.PostService;
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
        boolean exists = this.postRepository.existsByTitleIgnoreCase(postRequest.title());
        if (exists) {
            log.debug("Post with title '{}' already exists", postRequest.title());
            throw new PostAlreadyExistsException(postRequest.title());
        } else {
            log.debug("Creating post with title '{}' for user '{}'", postRequest.title(), userName);
            Post post = this.postMapper.postRequestToEntity(postRequest, userName);
            this.postRepository.save(post);
        }
    }

    @Override
    @Transactional
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
    @Transactional
    public void deletePostByUserNameAndTitle(String userName, String title) {
        boolean exists = this.postRepository.existsByTitleAndDetails_CreatedBy(title, userName);
        if (!exists) {
            log.debug("Post with title '{}' for user '{}' not found", title, userName);
            throw new PostNotFoundException(
                    String.format("Post with title '%s' not found for user '%s'", title, userName));
        }
        this.postRepository.deleteByTitleAndCreatedBy(title, userName);
    }

    /**
     * This operation is not supported in the JPA implementation.
     * Please use the JOOQ implementation (JooqPostService) instead.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public PostResponse fetchPostByUserNameAndTitle(String userName, String title) {
        throw new UnsupportedOperationException();
    }
}

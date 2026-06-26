package com.example.learning.service;

import com.example.learning.exception.PostAlreadyExistsException;
import com.example.learning.exception.PostNotFoundException;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PostServiceFacade implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceFacade.class);

    private final PostReadService postReadService;
    private final PostWriteService postWriteService;

    public PostServiceFacade(PostReadService postReadService, PostWriteService postWriteService) {
        this.postReadService = postReadService;
        this.postWriteService = postWriteService;
    }

    @Override
    public PostResponse fetchPostByUserNameAndTitle(String userName, String title) {
        return this.postReadService.fetchPostByUserNameAndTitle(userName, title);
    }

    @Override
    public void createPost(PostRequest postRequest, String userName) {
        boolean exists = this.postReadService.existsByTitleAndDetailsCreatedBy(postRequest.title(), userName);
        if (exists) {
            log.debug("Post with title '{}' already exists", postRequest.title());
            throw new PostAlreadyExistsException(postRequest.title());
        }
        log.debug("Creating post with title '{}' for user '{}'", postRequest.title(), userName);
        this.postWriteService.createPost(postRequest, userName);
    }

    @Override
    public PostResponse updatePostByUserNameAndTitle(PostRequest postRequest, String userName, String title) {
        return this.postWriteService.updatePostByUserNameAndTitle(postRequest, userName, title);
    }

    @Override
    public void deletePostByUserNameAndTitle(String userName, String title) {
        boolean exists = this.postReadService.existsByTitleAndDetailsCreatedBy(title, userName);
        if (!exists) {
            log.debug("Post with title '{}' for user '{}' not found", title, userName);
            throw new PostNotFoundException(
                    String.format("Post with title '%s' not found for user '%s'", title, userName));
        }
        this.postWriteService.deletePostByUserNameAndTitle(userName, title);
    }
}

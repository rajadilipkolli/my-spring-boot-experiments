package com.example.graphql.querydsl.services;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.exception.PostCommentNotFoundException;
import com.example.graphql.querydsl.mapper.PostCommentMapper;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.repositories.PostCommentRepository;
import com.example.graphql.querydsl.utils.PageUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostCommentMapper postCommentMapper;

    public PostCommentService(PostCommentRepository postCommentRepository, PostCommentMapper postCommentMapper) {
        this.postCommentRepository = postCommentRepository;
        this.postCommentMapper = postCommentMapper;
    }

    public PagedResult<PostCommentResponse> findAllPostComments(FindQuery findPostCommentsQuery) {

        // create Pageable instance
        Pageable pageable = PageUtil.createPageable(findPostCommentsQuery);

        Page<PostComment> postCommentsPage = postCommentRepository.findAll(pageable);

        List<PostCommentResponse> postCommentResponseList =
                postCommentMapper.toResponseList(postCommentsPage.getContent());

        return new PagedResult<>(postCommentsPage, postCommentResponseList);
    }

    public Optional<PostCommentResponse> findPostCommentById(Long id) {
        return postCommentRepository.findById(id).map(postCommentMapper::toResponse);
    }

    @Transactional
    public PostCommentResponse savePostComment(CreatePostCommentRequest createPostCommentRequest) {
        PostComment postComment = postCommentMapper.toEntity(createPostCommentRequest);
        PostComment savedPostComment = postCommentRepository.save(postComment);
        return postCommentMapper.toResponse(savedPostComment);
    }

    @Transactional
    public PostCommentResponse updatePostComment(Long id, PostCommentRequest postCommentRequest) {
        PostComment postComment =
                postCommentRepository.findById(id).orElseThrow(() -> new PostCommentNotFoundException(id));

        // Update the postComment object with data from postCommentRequest
        postCommentMapper.mapPostCommentWithRequest(postComment, postCommentRequest);

        // Save the updated postComment object
        PostComment updatedPostComment = postCommentRepository.save(postComment);

        return postCommentMapper.toResponse(updatedPostComment);
    }

    @Transactional
    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }
}

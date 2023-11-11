package com.example.graphql.querydsl.services;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.exception.PostCommentNotFoundException;
import com.example.graphql.querydsl.mapper.PostCommentMapper;
import com.example.graphql.querydsl.model.query.FindPostCommentsQuery;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.repositories.PostCommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostCommentMapper postCommentMapper;

    public PagedResult<PostCommentResponse> findAllPostComments(FindPostCommentsQuery findPostCommentsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findPostCommentsQuery);

        Page<PostComment> postCommentsPage = postCommentRepository.findAll(pageable);

        List<PostCommentResponse> postCommentResponseList =
                postCommentMapper.toResponseList(postCommentsPage.getContent());

        return new PagedResult<>(postCommentsPage, postCommentResponseList);
    }

    private Pageable createPageable(FindPostCommentsQuery findPostCommentsQuery) {
        int pageNo = Math.max(findPostCommentsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findPostCommentsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findPostCommentsQuery.sortBy())
                        : Sort.Order.desc(findPostCommentsQuery.sortBy()));
        return PageRequest.of(pageNo, findPostCommentsQuery.pageSize(), sort);
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

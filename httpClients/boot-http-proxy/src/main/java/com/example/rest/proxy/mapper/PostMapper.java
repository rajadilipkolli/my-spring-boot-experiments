package com.example.rest.proxy.mapper;

import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.entities.PostComment;
import com.example.rest.proxy.model.response.PostCommentDto;
import com.example.rest.proxy.model.response.PostResponse;
import com.example.rest.proxy.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public List<PostCommentDto> mapToResponseList(List<PostComment> postCommentList) {
        return postCommentList.stream()
                .map(
                        postComment ->
                                new PostCommentDto(
                                        postComment.getPost().getId(),
                                        postComment.getId(),
                                        postComment.getName(),
                                        postComment.getEmail(),
                                        postComment.getBody()))
                .toList();
    }

    public List<PostComment> mapToEntityList(
            List<PostCommentDto> postCommentDtos, PostRepository postRepository) {
        return postCommentDtos.stream()
                .map(
                        postCommentDto -> {
                            PostComment postComment = new PostComment();
                            postComment.setBody(postCommentDto.body());
                            postComment.setEmail(postCommentDto.email());
                            postComment.setName(postCommentDto.name());
                            postComment.setPost(
                                    postRepository.getReferenceById(postCommentDto.postId()));
                            return postComment;
                        })
                .toList();
    }

    public PostResponse mapToPostResponse(Post post) {

        return new PostResponse(
                post.getId(), post.getUserId(), post.getTitle(), post.getBody(), new ArrayList<>());
    }
}

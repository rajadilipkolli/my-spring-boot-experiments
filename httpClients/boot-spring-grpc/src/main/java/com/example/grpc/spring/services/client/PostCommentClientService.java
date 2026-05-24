package com.example.grpc.spring.services.client;

import com.example.grpc.spring.model.PostCommentDto;
import com.example.grpc.spring.proto.AddCommentRequest;
import com.example.grpc.spring.proto.DeleteCommentRequest;
import com.example.grpc.spring.proto.DeleteCommentResponse;
import com.example.grpc.spring.proto.GetCommentRequest;
import com.example.grpc.spring.proto.ListCommentsRequest;
import com.example.grpc.spring.proto.PostComment;
import com.example.grpc.spring.proto.PostCommentServiceGrpc;
import com.example.grpc.spring.proto.UpdateCommentRequest;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostCommentClientService {

    private final PostCommentServiceGrpc.PostCommentServiceBlockingStub commentServiceStub;

    public PostCommentClientService(
            PostCommentServiceGrpc.PostCommentServiceBlockingStub commentServiceStub) {
        this.commentServiceStub = commentServiceStub;
    }

    public PostCommentDto addComment(Long postId, PostCommentDto dto) {
        try {
            PostComment response =
                    commentServiceStub.addComment(
                            AddCommentRequest.newBuilder()
                                    .setPostId(postId)
                                    .setReview(dto.review())
                                    .build());
            return new PostCommentDto(response.getId(), response.getPostId(), response.getReview());
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }

    public PostCommentDto getComment(Long postId, Long id) {
        try {
            PostComment response =
                    commentServiceStub.getComment(
                            GetCommentRequest.newBuilder().setPostId(postId).setId(id).build());
            return new PostCommentDto(response.getId(), response.getPostId(), response.getReview());
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }

    public PostCommentDto updateComment(Long postId, Long id, PostCommentDto dto) {
        try {
            PostComment response =
                    commentServiceStub.updateComment(
                            UpdateCommentRequest.newBuilder()
                                    .setPostId(postId)
                                    .setId(id)
                                    .setReview(dto.review())
                                    .build());
            return new PostCommentDto(response.getId(), response.getPostId(), response.getReview());
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }

    public boolean deleteComment(Long postId, Long id) {
        try {
            DeleteCommentResponse response =
                    commentServiceStub.deleteComment(
                            DeleteCommentRequest.newBuilder().setPostId(postId).setId(id).build());
            return response.getSuccess();
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }

    public List<PostCommentDto> listComments(Long postId) {
        try {
            return commentServiceStub
                    .listComments(ListCommentsRequest.newBuilder().setPostId(postId).build())
                    .getCommentsList()
                    .stream()
                    .map(c -> new PostCommentDto(c.getId(), c.getPostId(), c.getReview()))
                    .toList();
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }
}

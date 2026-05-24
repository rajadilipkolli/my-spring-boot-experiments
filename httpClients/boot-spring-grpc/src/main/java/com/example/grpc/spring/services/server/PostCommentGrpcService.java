package com.example.grpc.spring.services.server;

import com.example.grpc.spring.entities.PostCommentEntity;
import com.example.grpc.spring.proto.AddCommentRequest;
import com.example.grpc.spring.proto.DeleteCommentRequest;
import com.example.grpc.spring.proto.DeleteCommentResponse;
import com.example.grpc.spring.proto.GetCommentRequest;
import com.example.grpc.spring.proto.ListCommentsRequest;
import com.example.grpc.spring.proto.ListCommentsResponse;
import com.example.grpc.spring.proto.PostComment;
import com.example.grpc.spring.proto.PostCommentServiceGrpc;
import com.example.grpc.spring.proto.UpdateCommentRequest;
import com.example.grpc.spring.repositories.PostCommentRepository;
import com.example.grpc.spring.repositories.PostRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostCommentGrpcService extends PostCommentServiceGrpc.PostCommentServiceImplBase {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;

    public PostCommentGrpcService(
            PostCommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void addComment(
            AddCommentRequest request, StreamObserver<PostComment> responseObserver) {
        postRepository
                .findById(request.getPostId())
                .ifPresentOrElse(
                        post -> {
                            PostCommentEntity comment = new PostCommentEntity();
                            comment.setReview(request.getReview());
                            comment.setPost(post);
                            PostCommentEntity savedComment = commentRepository.save(comment);
                            responseObserver.onNext(mapToProto(savedComment));
                            responseObserver.onCompleted();
                        },
                        () ->
                                responseObserver.onError(
                                        Status.NOT_FOUND
                                                .withDescription("Post not found")
                                                .asRuntimeException()));
    }

    @Override
    public void getComment(
            GetCommentRequest request, StreamObserver<PostComment> responseObserver) {
        commentRepository
                .findById(request.getId())
                .ifPresentOrElse(
                        comment -> {
                            responseObserver.onNext(mapToProto(comment));
                            responseObserver.onCompleted();
                        },
                        () ->
                                responseObserver.onError(
                                        Status.NOT_FOUND
                                                .withDescription("Comment not found")
                                                .asRuntimeException()));
    }

    @Override
    public void updateComment(
            UpdateCommentRequest request, StreamObserver<PostComment> responseObserver) {
        commentRepository
                .findById(request.getId())
                .ifPresentOrElse(
                        comment -> {
                            comment.setReview(request.getReview());
                            PostCommentEntity updated = commentRepository.save(comment);
                            responseObserver.onNext(mapToProto(updated));
                            responseObserver.onCompleted();
                        },
                        () ->
                                responseObserver.onError(
                                        Status.NOT_FOUND
                                                .withDescription("Comment not found")
                                                .asRuntimeException()));
    }

    @Override
    public void deleteComment(
            DeleteCommentRequest request, StreamObserver<DeleteCommentResponse> responseObserver) {
        if (commentRepository.existsById(request.getId())) {
            commentRepository.deleteById(request.getId());
            responseObserver.onNext(DeleteCommentResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Comment not found").asRuntimeException());
        }
    }

    @Override
    public void listComments(
            ListCommentsRequest request, StreamObserver<ListCommentsResponse> responseObserver) {
        if (!postRepository.existsById(request.getPostId())) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Post not found").asRuntimeException());
            return;
        }

        List<PostComment> comments =
                commentRepository.findByPostId(request.getPostId()).stream()
                        .map(this::mapToProto)
                        .toList();

        responseObserver.onNext(ListCommentsResponse.newBuilder().addAllComments(comments).build());
        responseObserver.onCompleted();
    }

    private PostComment mapToProto(PostCommentEntity entity) {
        return PostComment.newBuilder()
                .setId(entity.getId())
                .setPostId(entity.getPost().getId())
                .setReview(entity.getReview())
                .build();
    }
}

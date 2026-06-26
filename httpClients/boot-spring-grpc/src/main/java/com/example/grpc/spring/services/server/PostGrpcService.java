package com.example.grpc.spring.services.server;

import com.example.grpc.spring.entities.PostEntity;
import com.example.grpc.spring.proto.CreatePostRequest;
import com.example.grpc.spring.proto.DeletePostRequest;
import com.example.grpc.spring.proto.DeletePostResponse;
import com.example.grpc.spring.proto.GetPostRequest;
import com.example.grpc.spring.proto.ListPostsRequest;
import com.example.grpc.spring.proto.ListPostsResponse;
import com.example.grpc.spring.proto.Post;
import com.example.grpc.spring.proto.PostServiceGrpc;
import com.example.grpc.spring.proto.UpdatePostRequest;
import com.example.grpc.spring.repositories.PostRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class PostGrpcService extends PostServiceGrpc.PostServiceImplBase {

    private final PostRepository postRepository;

    public PostGrpcService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void createPost(CreatePostRequest request, StreamObserver<Post> responseObserver) {
        PostEntity entity = new PostEntity();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());

        PostEntity saved = postRepository.save(entity);
        responseObserver.onNext(mapToProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void getPost(GetPostRequest request, StreamObserver<Post> responseObserver) {
        postRepository
                .findById(request.getId())
                .ifPresentOrElse(
                        entity -> {
                            responseObserver.onNext(mapToProto(entity));
                            responseObserver.onCompleted();
                        },
                        () ->
                                responseObserver.onError(
                                        Status.NOT_FOUND
                                                .withDescription("Post not found")
                                                .asRuntimeException()));
    }

    @Override
    public void updatePost(UpdatePostRequest request, StreamObserver<Post> responseObserver) {
        postRepository
                .findById(request.getId())
                .ifPresentOrElse(
                        entity -> {
                            entity.setTitle(request.getTitle());
                            entity.setContent(request.getContent());
                            PostEntity updated = postRepository.save(entity);
                            responseObserver.onNext(mapToProto(updated));
                            responseObserver.onCompleted();
                        },
                        () ->
                                responseObserver.onError(
                                        Status.NOT_FOUND
                                                .withDescription("Post not found")
                                                .asRuntimeException()));
    }

    @Override
    public void deletePost(
            DeletePostRequest request, StreamObserver<DeletePostResponse> responseObserver) {
        if (postRepository.existsById(request.getId())) {
            postRepository.deleteById(request.getId());
            responseObserver.onNext(DeletePostResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Post not found").asRuntimeException());
        }
    }

    @Override
    public void listPosts(
            ListPostsRequest request, StreamObserver<ListPostsResponse> responseObserver) {
        List<Post> posts = postRepository.findAll().stream().map(this::mapToProto).toList();

        responseObserver.onNext(ListPostsResponse.newBuilder().addAllPosts(posts).build());
        responseObserver.onCompleted();
    }

    private Post mapToProto(PostEntity entity) {
        return Post.newBuilder()
                .setId(entity.getId())
                .setTitle(entity.getTitle())
                .setContent(entity.getContent())
                .build();
    }
}

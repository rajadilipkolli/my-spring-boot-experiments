package com.example.grpc.spring.services.client;

import com.example.grpc.spring.model.PostDto;
import com.example.grpc.spring.proto.CreatePostRequest;
import com.example.grpc.spring.proto.DeletePostRequest;
import com.example.grpc.spring.proto.DeletePostResponse;
import com.example.grpc.spring.proto.GetPostRequest;
import com.example.grpc.spring.proto.ListPostsRequest;
import com.example.grpc.spring.proto.Post;
import com.example.grpc.spring.proto.PostServiceGrpc;
import com.example.grpc.spring.proto.UpdatePostRequest;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostClientService {

    private final PostServiceGrpc.PostServiceBlockingStub postServiceStub;

    public PostClientService(PostServiceGrpc.PostServiceBlockingStub postServiceStub) {
        this.postServiceStub = postServiceStub;
    }

    public PostDto createPost(PostDto dto) {
        try {
            Post response =
                    postServiceStub.createPost(
                            CreatePostRequest.newBuilder()
                                    .setTitle(dto.title())
                                    .setContent(dto.content())
                                    .build());
            return new PostDto(response.getId(), response.getTitle(), response.getContent());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to create post", e);
        }
    }

    public PostDto getPost(Long id) {
        try {
            Post response = postServiceStub.getPost(GetPostRequest.newBuilder().setId(id).build());
            return new PostDto(response.getId(), response.getTitle(), response.getContent());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Post not found", e);
        }
    }

    public PostDto updatePost(Long id, PostDto dto) {
        try {
            Post response =
                    postServiceStub.updatePost(
                            UpdatePostRequest.newBuilder()
                                    .setId(id)
                                    .setTitle(dto.title())
                                    .setContent(dto.content())
                                    .build());
            return new PostDto(response.getId(), response.getTitle(), response.getContent());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to update post", e);
        }
    }

    public boolean deletePost(Long id) {
        try {
            DeletePostResponse response =
                    postServiceStub.deletePost(DeletePostRequest.newBuilder().setId(id).build());
            return response.getSuccess();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to delete post", e);
        }
    }

    public List<PostDto> listPosts() {
        try {
            return postServiceStub
                    .listPosts(ListPostsRequest.newBuilder().build())
                    .getPostsList()
                    .stream()
                    .map(p -> new PostDto(p.getId(), p.getTitle(), p.getContent()))
                    .toList();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to list posts", e);
        }
    }
}

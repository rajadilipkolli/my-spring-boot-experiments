package com.example.graphql.services;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.mapper.NewPostRequestToPostEntityMapper;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.repositories.AuthorRepository;
import com.example.graphql.repositories.PostRepository;
import com.example.graphql.repositories.TagRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;

    private final ConversionService myConversionService;
    private final NewPostRequestToPostEntityMapper mapNewPostRequestToPostEntityMapper;

    public List<PostResponse> findAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> myConversionService.convert(post, PostResponse.class))
                .toList();
    }

    public List<PostInfo> findAllPostsByAuthorEmail(String emailId) {
        return postRepository.findByAuthorEntity_EmailIgnoreCase(emailId);
    }

    public Optional<PostResponse> findPostById(Long id) {
        return postRepository
                .findById(id)
                .map(post -> myConversionService.convert(post, PostResponse.class));
    }

    @Transactional
    public PostResponse savePost(NewPostRequest newPostRequest) {
        return myConversionService.convert(createPost(newPostRequest), PostResponse.class);
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public Map<Long, List<PostInfo>> getPostByAuthorIdIn(List<Long> authorIds) {
        return this.postRepository.findByAuthorEntity_IdIn(authorIds).stream()
                .collect(Collectors.groupingBy(postInfo -> postInfo.getAuthorEntity().getId()));
    }

    @Transactional
    public PostEntity createPost(NewPostRequest newPostRequest) {
        PostEntity postEntity =
                this.mapNewPostRequestToPostEntityMapper.convert(newPostRequest, tagRepository);
        postEntity.setAuthorEntity(
                this.authorRepository.getReferenceByEmail(newPostRequest.email()));
        return this.postRepository.save(postEntity);
    }

    public Optional<PostResponse> updatePost(Long id, NewPostRequest newPostRequest) {
        return postRepository
                .findById(id)
                .map(
                        postEntity -> {
                            mapNewPostRequestToPostEntityMapper.updatePostEntity(
                                    newPostRequest, postEntity);
                            PostEntity updatedPostEntity = postRepository.save(postEntity);
                            return myConversionService.convert(
                                    updatedPostEntity, PostResponse.class);
                        });
    }
}

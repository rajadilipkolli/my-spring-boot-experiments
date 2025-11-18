package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.mapper.NewPostRequestToPostEntityMapper;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.repositories.AuthorRepository;
import com.example.graphql.repositories.PostRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;

    private final ConversionService appConversionService;
    private final NewPostRequestToPostEntityMapper mapNewPostRequestToPostEntityMapper;

    public PostService(
            PostRepository postRepository,
            AuthorRepository authorRepository,
            TagRepository tagRepository,
            ConversionService appConversionService,
            NewPostRequestToPostEntityMapper mapNewPostRequestToPostEntityMapper) {
        this.postRepository = postRepository;
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.appConversionService = appConversionService;
        this.mapNewPostRequestToPostEntityMapper = mapNewPostRequestToPostEntityMapper;
    }

    public List<PostResponse> findAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> appConversionService.convert(post, PostResponse.class))
                .toList();
    }

    public List<PostInfo> findAllPostsByAuthorEmail(String emailId) {
        return postRepository.findByAuthorEntity_EmailIgnoreCase(emailId);
    }

    public Optional<PostResponse> findPostById(Long id) {
        return postRepository.findById(id).map(post -> appConversionService.convert(post, PostResponse.class));
    }

    public PostResponse savePost(NewPostRequest newPostRequest) {
        return appConversionService.convert(createPost(newPostRequest), PostResponse.class);
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public Map<Long, List<PostInfo>> getPostByAuthorIdIn(List<Long> authorIds) {
        return this.postRepository.findByAuthorEntity_IdIn(authorIds).stream()
                .collect(Collectors.groupingBy(
                        postInfo -> postInfo.getAuthorEntity().getId()));
    }

    @Transactional
    public PostEntity createPost(NewPostRequest newPostRequest) {
        PostEntity postEntity = this.mapNewPostRequestToPostEntityMapper.convert(newPostRequest, tagRepository);
        postEntity.setAuthorEntity(this.authorRepository.getReferenceByEmail(newPostRequest.email()));
        return this.postRepository.save(postEntity);
    }

    @Transactional
    public Optional<PostResponse> updatePost(Long id, NewPostRequest newPostRequest) {
        return postRepository.findById(id).map(postEntity -> {
            mapNewPostRequestToPostEntityMapper.updatePostEntity(newPostRequest, postEntity, tagRepository);
            PostEntity updatedPostEntity = postRepository.save(postEntity);
            return appConversionService.convert(updatedPostEntity, PostResponse.class);
        });
    }

    public boolean existsPostById(Long id) {
        return postRepository.existsById(id);
    }
}

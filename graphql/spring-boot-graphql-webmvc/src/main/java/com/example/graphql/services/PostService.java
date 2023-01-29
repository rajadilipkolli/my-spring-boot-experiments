package com.example.graphql.services;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.mapper.adapter.ConversionServiceAdapter;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.repositories.AuthorRepository;
import com.example.graphql.repositories.PostRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;

    private final ConversionServiceAdapter conversionServiceAdapter;

    public List<PostEntity> findAllPosts() {
        return postRepository.findAll();
    }

    public List<PostInfo> findAllPostsByAuthorEmail(String emailId) {
        return postRepository.findByAuthorEntity_EmailIgnoreCase(emailId);
    }

    public Optional<PostEntity> findPostById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public PostEntity savePost(PostEntity postEntity) {
        return postRepository.save(postEntity);
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
                this.conversionServiceAdapter.mapNewPostRequestToPostEntity(newPostRequest);
        // handle Tags till bug is fixed, Ideally this should be handled in Mapper
        if (null != newPostRequest.tags()) {
            newPostRequest
                    .tags()
                    .forEach(tagsRequest -> postEntity.addTag(getTagEntity(tagsRequest)));
        }
        postEntity.setAuthorEntity(
                this.authorRepository.getReferenceByEmail(newPostRequest.email()));
        return this.postRepository.save(postEntity);
    }

    private TagEntity getTagEntity(TagsRequest tagsRequest) {
        return this.tagRepository
                .findByTagNameIgnoreCase(tagsRequest.tagName())
                .orElseGet(
                        () ->
                                this.tagRepository.save(
                                        new TagEntity(
                                                tagsRequest.tagName(),
                                                tagsRequest.tagDescription())));
    }
}

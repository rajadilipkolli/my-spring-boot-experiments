package com.example.graphql.services;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.model.response.TagResponse;
import com.example.graphql.repositories.PostTagRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final ConversionService appConversionService;

    public TagService(
            TagRepository tagRepository, PostTagRepository postTagRepository, ConversionService appConversionService) {
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.appConversionService = appConversionService;
    }

    public List<TagResponse> findAllTags() {
        return tagRepository.findAll().stream()
                .map(tagEntity -> appConversionService.convert(tagEntity, TagResponse.class))
                .collect(Collectors.toList());
    }

    public Optional<TagResponse> findTagById(Long id) {
        return tagRepository.findById(id).map(tagEntity -> appConversionService.convert(tagEntity, TagResponse.class));
    }

    @Transactional
    public @Nullable TagResponse saveTag(TagEntity tagEntity) {
        TagEntity saved = tagRepository.save(tagEntity);
        return appConversionService.convert(saved, TagResponse.class);
    }

    @Transactional
    public void deleteTagById(Long id) {
        tagRepository.deleteById(id);
    }

    public Map<Long, List<TagEntity>> getTagsByPostIdIn(List<Long> postIds) {
        return postTagRepository.findByPostEntity_IdIn(postIds).stream()
                .collect(Collectors.groupingBy(
                        postTagEntity -> postTagEntity.getPostEntity().getId(),
                        Collectors.mapping(PostTagEntity::getTagEntity, Collectors.toList())));
    }

    @Transactional
    public TagResponse saveTag(String tagName, String tagDescription) {
        return this.tagRepository
                .findByTagNameIgnoreCase(tagName)
                .map(tagEntity -> {
                    tagEntity.setTagDescription(tagDescription);
                    TagEntity saved = tagRepository.save(tagEntity);
                    return appConversionService.convert(saved, TagResponse.class);
                })
                .orElseGet(() -> saveTag(new TagEntity().setTagName(tagName).setTagDescription(tagDescription)));
    }

    public Optional<TagResponse> findTagByName(String tagName) {
        return this.tagRepository
                .findByTagNameIgnoreCase(tagName)
                .map(tagEntity -> appConversionService.convert(tagEntity, TagResponse.class));
    }

    @Transactional
    public TagResponse updateTag(String tagName, String tagDescription) {
        return saveTag(tagName, tagDescription);
    }

    @Transactional
    public void deleteTagByName(String tagName) {
        this.tagRepository.deleteByTagName(tagName);
    }

    public boolean existsTagById(Long id) {
        return this.tagRepository.existsById(id);
    }

    @Transactional
    public @Nullable TagResponse updateTag(Long id, TagsRequest tagsRequest) {
        TagEntity tagEntity = appConversionService.convert(tagsRequest, TagEntity.class);
        tagEntity.setId(id);
        return saveTag(tagEntity);
    }
}

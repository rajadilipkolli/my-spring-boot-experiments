package com.example.graphql.services;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.repositories.PostTagRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    public TagService(TagRepository tagRepository, PostTagRepository postTagRepository) {
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    public List<TagEntity> findAllTags() {
        return tagRepository.findAll();
    }

    public Optional<TagEntity> findTagById(Long id) {
        return tagRepository.findById(id);
    }

    public TagEntity saveTag(TagEntity tagEntity) {
        return tagRepository.save(tagEntity);
    }

    public void deleteTagById(Long id) {
        tagRepository.deleteById(id);
    }

    public Map<Long, List<TagEntity>> getTagsByPostIdIn(List<Long> postIds) {
        return postTagRepository.findByPostEntity_IdIn(postIds).stream()
                .collect(Collectors.groupingBy(
                        postTagEntity -> postTagEntity.getPostEntity().getId(),
                        Collectors.mapping(PostTagEntity::getTagEntity, Collectors.toList())));
    }

    public TagEntity saveTag(String tagName, String tagDescription) {
        return this.tagRepository
                .findByTagNameIgnoreCase(tagName)
                .orElseGet(() -> saveTag(new TagEntity().setTagName(tagName).setTagDescription(tagDescription)));
    }

    public Optional<TagEntity> findTagByName(String tagName) {
        return this.tagRepository.findByTagNameIgnoreCase(tagName);
    }

    public Optional<TagEntity> updateTag(String tagName, String tagDescription) {
        return this.tagRepository.findByTagNameIgnoreCase(tagName).map(tagEntity -> {
            tagEntity.setTagDescription(tagDescription);
            return saveTag(tagEntity);
        });
    }

    public void deleteTagByName(String tagName) {
        TagEntity tagEntity =
                this.tagRepository.findByTagNameIgnoreCase(tagName).orElseThrow();
        this.tagRepository.delete(tagEntity);
    }
}

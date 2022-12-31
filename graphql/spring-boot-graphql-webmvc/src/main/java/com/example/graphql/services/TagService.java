package com.example.graphql.services;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.repositories.PostTagRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

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
                .collect(
                        Collectors.groupingBy(
                                postTagEntity -> postTagEntity.getPostEntity().getId(),
                                Collectors.mapping(
                                        PostTagEntity::getTagEntity, Collectors.toList())));
    }
}

package com.example.graphql.services;

import com.example.graphql.entities.PostTag;
import com.example.graphql.entities.Tag;
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

    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> findTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTagById(Long id) {
        tagRepository.deleteById(id);
    }

    public Map<Long, List<Tag>> getTagsByPostIdIn(List<Long> postIds) {
        return postTagRepository.findByPost_IdIn(postIds).stream()
                .collect(
                        Collectors.groupingBy(
                                postTag -> postTag.getPost().getId(),
                                Collectors.mapping(PostTag::getTag, Collectors.toList())));
    }
}

package com.example.graphql.querydsl.services;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.exception.TagNotFoundException;
import com.example.graphql.querydsl.mapper.TagMapper;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.repositories.TagRepository;
import com.example.graphql.querydsl.utils.PageUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    public PagedResult<TagResponse> findAllTags(FindQuery findTagsQuery) {

        // create Pageable instance
        Pageable pageable = PageUtil.createPageable(findTagsQuery);

        Page<Tag> tagsPage = tagRepository.findAll(pageable);

        List<TagResponse> tagResponseList = tagMapper.toResponseList(tagsPage.getContent());

        return new PagedResult<>(tagsPage, tagResponseList);
    }

    public Optional<TagResponse> findTagById(Long id) {
        return tagRepository.findById(id).map(tagMapper::toResponse);
    }

    @Transactional
    public TagResponse saveTag(TagRequest tagRequest) {
        Tag tag = tagMapper.toEntity(tagRequest);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Transactional
    public TagResponse updateTag(Long id, TagRequest tagRequest) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));

        // Update the tag object with data from tagRequest
        tagMapper.mapTagWithRequest(tag, tagRequest);

        // Save the updated tag object
        Tag updatedTag = tagRepository.save(tag);

        return tagMapper.toResponse(updatedTag);
    }

    @Transactional
    public void deleteTagById(Long id) {
        tagRepository.deleteById(id);
    }
}

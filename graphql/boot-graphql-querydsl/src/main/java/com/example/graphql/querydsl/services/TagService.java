package com.example.graphql.querydsl.services;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.exception.TagNotFoundException;
import com.example.graphql.querydsl.mapper.TagMapper;
import com.example.graphql.querydsl.model.query.FindTagsQuery;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.repositories.TagRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public PagedResult<TagResponse> findAllTags(FindTagsQuery findTagsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findTagsQuery);

        Page<Tag> tagsPage = tagRepository.findAll(pageable);

        List<TagResponse> tagResponseList = tagMapper.toResponseList(tagsPage.getContent());

        return new PagedResult<>(tagsPage, tagResponseList);
    }

    private Pageable createPageable(FindTagsQuery findTagsQuery) {
        int pageNo = Math.max(findTagsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findTagsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findTagsQuery.sortBy())
                        : Sort.Order.desc(findTagsQuery.sortBy()));
        return PageRequest.of(pageNo, findTagsQuery.pageSize(), sort);
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

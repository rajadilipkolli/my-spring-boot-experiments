package com.example.graphql.querydsl.web.controllers;

import com.example.graphql.querydsl.exception.TagNotFoundException;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.services.TagService;
import com.example.graphql.querydsl.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public PagedResult<TagResponse> getAllTags(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        FindQuery findTagsQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return tagService.findAllTags(findTagsQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        return tagService.findTagById(id).map(ResponseEntity::ok).orElseThrow(() -> new TagNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@RequestBody @Validated TagRequest tagRequest) {
        TagResponse response = tagService.saveTag(tagRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/tags/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(@PathVariable Long id, @RequestBody @Valid TagRequest tagRequest) {
        return ResponseEntity.ok(tagService.updateTag(id, tagRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponse> deleteTag(@PathVariable Long id) {
        return tagService
                .findTagById(id)
                .map(tag -> {
                    tagService.deleteTagById(id);
                    return ResponseEntity.ok(tag);
                })
                .orElseThrow(() -> new TagNotFoundException(id));
    }
}

package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.TagNotFoundException;
import com.example.graphql.model.query.FindQuery;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.model.response.PagedResult;
import com.example.graphql.model.response.TagResponse;
import com.example.graphql.services.TagService;
import com.example.graphql.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tags")
@Loggable
@Validated
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    ResponseEntity<PagedResult<TagResponse>> getAllTags(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindQuery findQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(tagService.findAllTags(findQuery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull TagResponse> getTagById(@PathVariable Long id) {
        return tagService.findTagById(id).map(ResponseEntity::ok).orElseThrow(() -> new TagNotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull TagResponse> createTag(@RequestBody @Valid TagsRequest tagsRequest) {
        TagResponse tagResponse = tagService.saveTag(tagsRequest.tagName(), tagsRequest.tagDescription());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tagResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(tagResponse);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull TagResponse> updateTag(
            @PathVariable Long id, @RequestBody @Valid TagsRequest tagsRequest) {
        return tagService
                .findTagById(id)
                .map(tagObj -> ResponseEntity.ok(tagService.updateTag(id, tagsRequest)))
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        if (tagService.existsTagById(id)) {
            tagService.deleteTagById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new TagNotFoundException(id);
        }
    }
}

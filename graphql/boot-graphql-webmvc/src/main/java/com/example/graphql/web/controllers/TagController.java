package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.TagNotFoundException;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.model.response.TagResponse;
import com.example.graphql.services.TagService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tags")
@Loggable
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponse> getAllTags() {
        return tagService.findAllTags();
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull TagResponse> getTagById(@PathVariable Long id) {
        return tagService.findTagById(id).map(ResponseEntity::ok).orElseThrow(() -> new TagNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<@NonNull TagResponse> createTag(@RequestBody @Validated TagsRequest tagsRequest) {
        TagResponse tagResponse = tagService.saveTag(tagsRequest.tagName(), tagsRequest.tagDescription());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tagResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(tagResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull TagResponse> updateTag(
            @PathVariable Long id, @RequestBody @Valid TagsRequest tagsRequest) {
        return tagService
                .findTagById(id)
                .map(tagObj -> ResponseEntity.ok(tagService.updateTag(id, tagsRequest)))
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTag(@PathVariable Long id) {
        if (tagService.existsTagById(id)) {
            tagService.deleteTagById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new TagNotFoundException(id);
        }
    }
}

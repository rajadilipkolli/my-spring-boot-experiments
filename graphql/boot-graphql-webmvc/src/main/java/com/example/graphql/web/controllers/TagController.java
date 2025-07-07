package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.exception.TagNotFoundException;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.services.TagService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@Loggable
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagEntity> getAllTags() {
        return tagService.findAllTags();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagEntity> getTagById(@PathVariable Long id) {
        return tagService.findTagById(id).map(ResponseEntity::ok).orElseThrow(() -> new TagNotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagEntity createTag(@RequestBody @Validated TagsRequest tagsRequest) {
        return tagService.saveTag(tagsRequest.tagName(), tagsRequest.tagDescription());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagEntity> updateTag(@PathVariable Long id, @RequestBody TagEntity tagEntity) {
        return tagService
                .findTagById(id)
                .map(tagObj -> {
                    tagEntity.setId(id);
                    return ResponseEntity.ok(tagService.saveTag(tagEntity));
                })
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagEntity> deleteTag(@PathVariable Long id) {
        return tagService
                .findTagById(id)
                .map(tag -> {
                    tagService.deleteTagById(id);
                    return ResponseEntity.ok(tag);
                })
                .orElseThrow(() -> new TagNotFoundException(id));
    }
}

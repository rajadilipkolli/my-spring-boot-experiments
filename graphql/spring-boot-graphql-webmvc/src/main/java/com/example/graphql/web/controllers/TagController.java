package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.services.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Loggable
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagEntity> getAllTags() {
        return tagService.findAllTags();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagEntity> getTagById(@PathVariable Long id) {
        return tagService
                .findTagById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagEntity createTag(@RequestBody @Validated TagEntity tagEntity) {
        return tagService.saveTag(tagEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagEntity> updateTag(
            @PathVariable Long id, @RequestBody TagEntity tagEntity) {
        return tagService
                .findTagById(id)
                .map(
                        tagObj -> {
                            tagEntity.setId(id);
                            return ResponseEntity.ok(tagService.saveTag(tagEntity));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagEntity> deleteTag(@PathVariable Long id) {
        return tagService
                .findTagById(id)
                .map(
                        tag -> {
                            tagService.deleteTagById(id);
                            return ResponseEntity.ok(tag);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

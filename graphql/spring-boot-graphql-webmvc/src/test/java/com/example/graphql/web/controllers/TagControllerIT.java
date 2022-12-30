package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.repositories.TagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class TagControllerIT extends AbstractIntegrationTest {

    @Autowired private TagRepository tagRepository;

    private List<TagEntity> tagList = null;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        tagList = new ArrayList<>();
        tagList.add(TagEntity.builder().tagName("First Tag").build());
        tagList.add(TagEntity.builder().tagName("Second Tag").build());
        tagList.add(TagEntity.builder().tagName("Third Tag").build());
        tagList = tagRepository.saveAll(tagList);
    }

    @Test
    void shouldFetchAllTags() throws Exception {
        this.mockMvc
                .perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(tagList.size())));
    }

    @Test
    void shouldFindTagById() throws Exception {
        TagEntity tag = tagList.get(0);
        Long tagId = tag.getId();

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tag.getTagName())));
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        TagEntity tag = new TagEntity(null, "New Tag", null);
        this.mockMvc
                .perform(
                        post("/api/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagName", is(tag.getTagName())));
    }

    @Test
    void shouldUpdateTag() throws Exception {
        TagEntity tag = tagList.get(0);
        tag.setTagName("Updated Tag");

        this.mockMvc
                .perform(
                        put("/api/tags/{id}", tag.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tag.getTagName())));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        TagEntity tag = tagList.get(0);

        this.mockMvc
                .perform(delete("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tag.getTagName())));
    }
}

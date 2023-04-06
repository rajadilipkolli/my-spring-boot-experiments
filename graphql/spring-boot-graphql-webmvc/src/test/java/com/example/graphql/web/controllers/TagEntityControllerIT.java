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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

class TagEntityControllerIT extends AbstractIntegrationTest {

    @Autowired private TagRepository tagRepository;

    private List<TagEntity> tagEntityList = null;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        tagEntityList = new ArrayList<>();
        tagEntityList.add(TagEntity.builder().tagName("First Tag").build());
        tagEntityList.add(TagEntity.builder().tagName("Second Tag").build());
        tagEntityList.add(TagEntity.builder().tagName("Third Tag").build());
        tagEntityList = tagRepository.saveAll(tagEntityList);
    }

    @Test
    void shouldFetchAllTags() throws Exception {
        this.mockMvc
                .perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(tagEntityList.size())));
    }

    @Test
    void shouldFindTagById() throws Exception {
        TagEntity tagEntity = tagEntityList.get(0);
        Long tagId = tagEntity.getId();

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        TagEntity tagEntity = new TagEntity(null, "New Tag", null);
        this.mockMvc
                .perform(
                        post("/api/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldUpdateTag() throws Exception {
        TagEntity tagEntity = tagEntityList.get(0);
        tagEntity.setTagName("Updated Tag");

        this.mockMvc
                .perform(
                        put("/api/tags/{id}", tagEntity.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        TagEntity tagEntity = tagEntityList.get(0);

        this.mockMvc
                .perform(delete("/api/tags/{id}", tagEntity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }
}

package com.example.graphql.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.TagsRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TagEntityControllerIT extends AbstractIntegrationTest {

    private List<TagEntity> tagEntityList = null;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        tagEntityList = new ArrayList<>();
        tagEntityList.add(new TagEntity().setTagName("First Tag"));
        tagEntityList.add(new TagEntity().setTagName("Second Tag"));
        tagEntityList.add(new TagEntity().setTagName("Third Tag"));
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
        TagEntity tagEntity = tagEntityList.getFirst();
        Long tagId = tagEntity.getId();

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        TagsRequest tagEntity = new TagsRequest("New Tag", null);
        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagName", is(tagEntity.tagName())))
                .andExpect(jsonPath("$.tagDescription", is(tagEntity.tagDescription())));
    }

    @Test
    void shouldUpdateTag() throws Exception {
        TagEntity tagEntity = tagEntityList.getFirst();
        tagEntity.setTagName("Updated Tag");

        this.mockMvc
                .perform(put("/api/tags/{id}", tagEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        TagEntity tagEntity = tagEntityList.getFirst();

        this.mockMvc.perform(delete("/api/tags/{id}", tagEntity.getId())).andExpect(status().isAccepted());

        // Verify entity was actually deleted
        assertThat(tagRepository.findById(tagEntity.getId())).isEmpty();
    }
}

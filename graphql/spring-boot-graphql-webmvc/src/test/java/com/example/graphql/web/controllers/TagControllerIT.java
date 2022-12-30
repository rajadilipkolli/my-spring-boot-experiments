package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.Tag;
import com.example.graphql.repositories.TagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class TagControllerIT extends AbstractIntegrationTest {

    @Autowired private TagRepository tagRepository;

    private List<Tag> tagList = null;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        tagList = new ArrayList<>();
        tagList.add(new Tag("First Tag"));
        tagList.add(new Tag("Second Tag"));
        tagList.add(new Tag("Third Tag"));
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
        Tag tag = tagList.get(0);
        Long tagId = tag.getId();

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        Tag tag = new Tag(null, "New Tag");
        this.mockMvc
                .perform(
                        post("/api/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void shouldUpdateTag() throws Exception {
        Tag tag = tagList.get(0);
        tag.setName("Updated Tag");

        this.mockMvc
                .perform(
                        put("/api/tags/{id}", tag.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        Tag tag = tagList.get(0);

        this.mockMvc
                .perform(delete("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }
}

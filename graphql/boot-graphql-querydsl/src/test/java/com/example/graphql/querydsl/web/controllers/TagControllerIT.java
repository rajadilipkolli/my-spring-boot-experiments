package com.example.graphql.querydsl.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.repositories.TagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class TagControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    private List<Tag> tagList = null;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAllInBatch();

        tagList = new ArrayList<>();
        tagList.add(new Tag().setName("First Tag"));
        tagList.add(new Tag().setName("Second Tag"));
        tagList.add(new Tag().setName("Third Tag"));
        tagList = tagRepository.saveAll(tagList);
    }

    @Test
    void shouldFetchAllTags() throws Exception {
        this.mockMvc
                .perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(tagList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindTagById() throws Exception {
        Tag tag = tagList.getFirst();
        Long tagId = tag.getId();

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(tag.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        TagRequest tagRequest = new TagRequest("New Tag");
        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(tagRequest.name())));
    }

    @Test
    void shouldReturn400WhenCreateNewTagWithoutName() throws Exception {
        TagRequest tagRequest = new TagRequest(null);

        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/tags")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("TagName cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateTag() throws Exception {
        Long tagId = tagList.getFirst().getId();
        TagRequest tagRequest = new TagRequest("Updated Tag");

        this.mockMvc
                .perform(put("/api/tags/{id}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(tagId), Long.class))
                .andExpect(jsonPath("$.name", is(tagRequest.name())));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        Tag tag = tagList.getFirst();

        this.mockMvc
                .perform(delete("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(tag.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }
}

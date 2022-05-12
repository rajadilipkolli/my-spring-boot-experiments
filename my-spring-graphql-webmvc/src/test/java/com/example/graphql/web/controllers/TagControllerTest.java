package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.Tag;
import com.example.graphql.services.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = TagController.class)
@ActiveProfiles(PROFILE_TEST)
class TagControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private TagService tagService;

    @Autowired private ObjectMapper objectMapper;

    private List<Tag> tagList;

    @BeforeEach
    void setUp() {
        this.tagList = new ArrayList<>();
        this.tagList.add(new Tag(1L, "text 1"));
        this.tagList.add(new Tag(2L, "text 2"));
        this.tagList.add(new Tag(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllTags() throws Exception {
        given(tagService.findAllTags()).willReturn(this.tagList);

        this.mockMvc
                .perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(tagList.size())));
    }

    @Test
    void shouldFindTagById() throws Exception {
        Long tagId = 1L;
        Tag tag = new Tag(tagId, "text 1");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tag));

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(tag.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/tags/{id}", tagId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewTag() throws Exception {
        given(tagService.saveTag(any(Tag.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Tag tag = new Tag(1L, "some text");
        this.mockMvc
                .perform(
                        post("/api/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(tag.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewTagWithoutText() throws Exception {
        Tag tag = new Tag(null, null);

        this.mockMvc
                .perform(
                        post("/api/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateTag() throws Exception {
        Long tagId = 1L;
        Tag tag = new Tag(tagId, "Updated text");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tag));
        given(tagService.saveTag(any(Tag.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/tags/{id}", tag.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(tag.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());
        Tag tag = new Tag(tagId, "Updated text");

        this.mockMvc
                .perform(
                        put("/api/tags/{id}", tagId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTag() throws Exception {
        Long tagId = 1L;
        Tag tag = new Tag(tagId, "Some text");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tag));
        doNothing().when(tagService).deleteTagById(tag.getId());

        this.mockMvc
                .perform(delete("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(tag.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/tags/{id}", tagId)).andExpect(status().isNotFound());
    }
}

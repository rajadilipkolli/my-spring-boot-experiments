package com.example.graphql.querydsl.web.controllers;

import static com.example.graphql.querydsl.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.exception.TagNotFoundException;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.services.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TagController.class)
@ActiveProfiles(PROFILE_TEST)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Tag> tagList;

    @BeforeEach
    void setUp() {
        this.tagList = new ArrayList<>();
        this.tagList.add(new Tag().setId(1L).setName("text 1"));
        this.tagList.add(new Tag().setId(2L).setName("text 2"));
        this.tagList.add(new Tag().setId(3L).setName("text 3"));
    }

    @Test
    void shouldFetchAllTags() throws Exception {

        Page<Tag> page = new PageImpl<>(tagList);
        PagedResult<TagResponse> tagPagedResult = new PagedResult<>(page, getTagResponseList());
        FindQuery findTagsQuery = new FindQuery(0, 10, "id", "asc");
        given(tagService.findAllTags(findTagsQuery)).willReturn(tagPagedResult);

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
        Long tagId = 1L;
        TagResponse tag = new TagResponse(tagId, "text 1");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tag));

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.name())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Tag with Id '%d' not found".formatted(tagId)));
    }

    @Test
    void shouldCreateNewTag() throws Exception {

        TagResponse tag = new TagResponse(1L, "some text");
        TagRequest tagRequest = new TagRequest("some text");
        given(tagService.saveTag(any(TagRequest.class))).willReturn(tag);

        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(tag.name())));
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
        Long tagId = 1L;
        TagResponse tag = new TagResponse(tagId, "Updated text");
        TagRequest tagRequest = new TagRequest("Updated text");
        given(tagService.updateTag(eq(tagId), any(TagRequest.class))).willReturn(tag);

        this.mockMvc
                .perform(put("/api/tags/{id}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(tagId), Long.class))
                .andExpect(jsonPath("$.name", is(tag.name())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTag() throws Exception {
        Long tagId = 1L;
        TagRequest tagRequest = new TagRequest("Updated text");
        given(tagService.updateTag(eq(tagId), any(TagRequest.class))).willThrow(new TagNotFoundException(tagId));

        this.mockMvc
                .perform(put("/api/tags/{id}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Tag with Id '%d' not found".formatted(tagId)));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        Long tagId = 1L;
        TagResponse tag = new TagResponse(tagId, "Some text");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tag));
        doNothing().when(tagService).deleteTagById(tagId);

        this.mockMvc
                .perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.name())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/tags/{id}", tagId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Tag with Id '%d' not found".formatted(tagId)));
    }

    List<TagResponse> getTagResponseList() {
        return tagList.stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();
    }
}

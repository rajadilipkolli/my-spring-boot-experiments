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

import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.services.TagService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = TagController.class)
@ActiveProfiles(PROFILE_TEST)
class TagEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<TagEntity> tagEntityList;

    @BeforeEach
    void setUp() {
        this.tagEntityList = new ArrayList<>();
        this.tagEntityList.add(new TagEntity().setId(1L).setTagName("text 1"));
        this.tagEntityList.add(new TagEntity().setId(2L).setTagName("text 2"));
        this.tagEntityList.add(new TagEntity().setId(3L).setTagName("text 3"));
    }

    @Test
    void shouldFetchAllTags() throws Exception {
        given(tagService.findAllTags()).willReturn(this.tagEntityList);

        this.mockMvc
                .perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(tagEntityList.size())));
    }

    @Test
    void shouldFindTagById() throws Exception {
        Long tagId = 1L;
        TagEntity tagEntity = new TagEntity().setId(1L).setTagName("text 1");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tagEntity));

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Tag: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/tags/1")));
    }

    @Test
    void shouldCreateNewTag() throws Exception {

        TagEntity tagEntity = new TagEntity().setId(1L).setTagName("some text");
        TagsRequest tagsRequest = new TagsRequest("some text", null);
        given(tagService.saveTag("some text", null)).willReturn(tagEntity);

        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldReturn400WhenCreateNewTagWithoutValidData() throws Exception {
        TagsRequest tag = new TagsRequest(null, null);

        this.mockMvc
                .perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("tagName")))
                .andExpect(jsonPath("$.violations[0].message", is("TagName must not be blank")))
                .andExpect(jsonPath("$.violations[0].object", is("tagsRequest")))
                .andReturn();
    }

    @Test
    void shouldUpdateTag() throws Exception {
        Long tagId = 1L;
        TagEntity tagEntity = new TagEntity().setId(1L).setTagName("updated text");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tagEntity));
        given(tagService.saveTag(any(TagEntity.class))).willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/tags/{id}", tagEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());
        TagEntity tagEntity = new TagEntity().setId(tagId).setTagName("Updated text");

        this.mockMvc
                .perform(put("/api/tags/{id}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagEntity)))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Tag: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/tags/1")));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        Long tagId = 1L;
        TagEntity tagEntity = new TagEntity().setId(tagId).setTagName("Some text");
        given(tagService.findTagById(tagId)).willReturn(Optional.of(tagEntity));
        doNothing().when(tagService).deleteTagById(tagEntity.getId());

        this.mockMvc
                .perform(delete("/api/tags/{id}", tagEntity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagName", is(tagEntity.getTagName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTag() throws Exception {
        Long tagId = 1L;
        given(tagService.findTagById(tagId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Tag: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/tags/1")));
    }
}

// package com.example.bootr2dbc.web.controllers;

// import static com.example.bootr2dbc.utils.AppConstants.PROFILE_TEST;
// import static org.hamcrest.CoreMatchers.is;
// import static org.hamcrest.CoreMatchers.notNullValue;
// import static org.hamcrest.Matchers.hasSize;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.Mockito.doNothing;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import com.example.bootr2dbc.entities.ReactiveComments;
// import com.example.bootr2dbc.model.response.PagedResult;
// import com.example.bootr2dbc.services.ReactiveCommentsService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest(controllers = ReactiveCommentsController.class)
// @ActiveProfiles(PROFILE_TEST)
// class ReactiveCommentsControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private ReactiveCommentsService reactiveCommentsService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     private List<ReactiveComments> reactiveCommentsList;

//     @BeforeEach
//     void setUp() {
//         this.reactiveCommentsList = new ArrayList<>();
//         this.reactiveCommentsList.add(new ReactiveComments(1L, "text 1"));
//         this.reactiveCommentsList.add(new ReactiveComments(2L, "text 2"));
//         this.reactiveCommentsList.add(new ReactiveComments(3L, "text 3"));
//     }

//     @Test
//     void shouldFetchAllReactiveCommentss() throws Exception {
//         Page<ReactiveComments> page = new PageImpl<>(reactiveCommentsList);
//         PagedResult<ReactiveComments> reactiveCommentsPagedResult = new PagedResult<>(page);
//         given(reactiveCommentsService.findAllReactiveCommentss(0, 10, "id", "asc"))
//                 .willReturn(reactiveCommentsPagedResult);

//         this.mockMvc
//                 .perform(get("/api/post/comments"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data.size()", is(reactiveCommentsList.size())))
//                 .andExpect(jsonPath("$.totalElements", is(3)))
//                 .andExpect(jsonPath("$.pageNumber", is(1)))
//                 .andExpect(jsonPath("$.totalPages", is(1)))
//                 .andExpect(jsonPath("$.isFirst", is(true)))
//                 .andExpect(jsonPath("$.isLast", is(true)))
//                 .andExpect(jsonPath("$.hasNext", is(false)))
//                 .andExpect(jsonPath("$.hasPrevious", is(false)));
//     }

//     @Test
//     void shouldFindReactiveCommentsById() throws Exception {
//         Long reactiveCommentsId = 1L;
//         ReactiveComments reactiveComments = new ReactiveComments(reactiveCommentsId, "text 1");
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.of(reactiveComments));

//         this.mockMvc
//                 .perform(get("/api/post/comments/{id}", reactiveCommentsId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldReturn404WhenFetchingNonExistingReactiveComments() throws Exception {
//         Long reactiveCommentsId = 1L;
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.empty());

//         this.mockMvc.perform(get("/api/post/comments/{id}", reactiveCommentsId)).andExpect(status().isNotFound());
//     }

//     @Test
//     void shouldCreateNewReactiveComments() throws Exception {
//         given(reactiveCommentsService.saveReactiveComments(any(ReactiveComments.class)))
//                 .willAnswer((invocation) -> invocation.getArgument(0));

//         ReactiveComments reactiveComments = new ReactiveComments(1L, "some text");
//         this.mockMvc
//                 .perform(post("/api/post/comments")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(reactiveComments)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.id", notNullValue()))
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldReturn400WhenCreateNewReactiveCommentsWithoutText() throws Exception {
//         ReactiveComments reactiveComments = new ReactiveComments(null, null);

//         this.mockMvc
//                 .perform(post("/api/post/comments")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(reactiveComments)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(header().string("Content-Type", is("application/problem+json")))
//                 .andExpect(jsonPath("$.type", is("about:blank")))
//                 .andExpect(jsonPath("$.title", is("Constraint Violation")))
//                 .andExpect(jsonPath("$.status", is(400)))
//                 .andExpect(jsonPath("$.detail", is("Invalid request content.")))
//                 .andExpect(jsonPath("$.instance", is("/api/post/comments")))
//                 .andExpect(jsonPath("$.violations", hasSize(1)))
//                 .andExpect(jsonPath("$.violations[0].field", is("text")))
//                 .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
//                 .andReturn();
//     }

//     @Test
//     void shouldUpdateReactiveComments() throws Exception {
//         Long reactiveCommentsId = 1L;
//         ReactiveComments reactiveComments = new ReactiveComments(reactiveCommentsId, "Updated text");
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.of(reactiveComments));
//         given(reactiveCommentsService.saveReactiveComments(any(ReactiveComments.class)))
//                 .willAnswer((invocation) -> invocation.getArgument(0));

//         this.mockMvc
//                 .perform(put("/api/post/comments/{id}", reactiveComments.getId())
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(reactiveComments)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldReturn404WhenUpdatingNonExistingReactiveComments() throws Exception {
//         Long reactiveCommentsId = 1L;
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.empty());
//         ReactiveComments reactiveComments = new ReactiveComments(reactiveCommentsId, "Updated text");

//         this.mockMvc
//                 .perform(put("/api/post/comments/{id}", reactiveCommentsId)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(reactiveComments)))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void shouldDeleteReactiveComments() throws Exception {
//         Long reactiveCommentsId = 1L;
//         ReactiveComments reactiveComments = new ReactiveComments(reactiveCommentsId, "Some text");
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.of(reactiveComments));
//         doNothing().when(reactiveCommentsService).deleteReactiveCommentsById(reactiveComments.getId());

//         this.mockMvc
//                 .perform(delete("/api/post/comments/{id}", reactiveComments.getId()))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldReturn404WhenDeletingNonExistingReactiveComments() throws Exception {
//         Long reactiveCommentsId = 1L;
//         given(reactiveCommentsService.findReactiveCommentsById(reactiveCommentsId))
//                 .willReturn(Optional.empty());

//         this.mockMvc
//                 .perform(delete("/api/post/comments/{id}", reactiveCommentsId))
//                 .andExpect(status().isNotFound());
//     }
// }

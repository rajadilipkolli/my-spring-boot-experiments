// package com.example.bootr2dbc.web.controllers;

// import static org.hamcrest.CoreMatchers.is;
// import static org.hamcrest.CoreMatchers.notNullValue;
// import static org.hamcrest.Matchers.hasSize;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import com.example.bootr2dbc.common.AbstractIntegrationTest;
// import com.example.bootr2dbc.entities.ReactiveComments;
// import com.example.bootr2dbc.repositories.ReactiveCommentsRepository;
// import java.util.ArrayList;
// import java.util.List;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;

// class ReactiveCommentsControllerIT extends AbstractIntegrationTest {

//     @Autowired
//     private ReactiveCommentsRepository reactiveCommentsRepository;

//     private List<ReactiveComments> reactiveCommentsList = null;

//     @BeforeEach
//     void setUp() {
//         reactiveCommentsRepository.deleteAllInBatch();

//         reactiveCommentsList = new ArrayList<>();
//         reactiveCommentsList.add(new ReactiveComments(null, "First ReactiveComments"));
//         reactiveCommentsList.add(new ReactiveComments(null, "Second ReactiveComments"));
//         reactiveCommentsList.add(new ReactiveComments(null, "Third ReactiveComments"));
//         reactiveCommentsList = reactiveCommentsRepository.saveAll(reactiveCommentsList);
//     }

//     @Test
//     void shouldFetchAllReactiveCommentss() throws Exception {
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
//         ReactiveComments reactiveComments = reactiveCommentsList.get(0);
//         Long reactiveCommentsId = reactiveComments.getId();

//         this.mockMvc
//                 .perform(get("/api/post/comments/{id}", reactiveCommentsId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id", is(reactiveComments.getId()), Long.class))
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldCreateNewReactiveComments() throws Exception {
//         ReactiveComments reactiveComments = new ReactiveComments(null, "New ReactiveComments");
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
//         ReactiveComments reactiveComments = reactiveCommentsList.get(0);
//         reactiveComments.setText("Updated ReactiveComments");

//         this.mockMvc
//                 .perform(put("/api/post/comments/{id}", reactiveComments.getId())
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(reactiveComments)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id", is(reactiveComments.getId()), Long.class))
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }

//     @Test
//     void shouldDeleteReactiveComments() throws Exception {
//         ReactiveComments reactiveComments = reactiveCommentsList.get(0);

//         this.mockMvc
//                 .perform(delete("/api/post/comments/{id}", reactiveComments.getId()))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id", is(reactiveComments.getId()), Long.class))
//                 .andExpect(jsonPath("$.text", is(reactiveComments.getText())));
//     }
// }

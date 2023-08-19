package com.example.restdocs.web.controllers;

import static com.example.restdocs.utils.AppConstants.PROFILE_TEST;
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

import com.example.restdocs.entities.User;
import com.example.restdocs.model.response.PagedResult;
import com.example.restdocs.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles(PROFILE_TEST)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        this.userList = new ArrayList<>();
        this.userList.add(new User(1L, "text 1"));
        this.userList.add(new User(2L, "text 2"));
        this.userList.add(new User(3L, "text 3"));
    }

    @Test
    void shouldFetchAllUsers() throws Exception {
        Page<User> page = new PageImpl<>(userList);
        PagedResult<User> userPagedResult = new PagedResult<>(page);
        given(userService.findAllUsers(0, 10, "id", "asc")).willReturn(userPagedResult);

        this.mockMvc
                .perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(userList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindUserById() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "text 1");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));

        this.mockMvc
                .perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(user.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/users/{id}", userId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        given(userService.saveUser(any(User.class))).willAnswer((invocation) -> invocation.getArgument(0));

        User user = new User(1L, "some text");
        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(user.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewUserWithoutText() throws Exception {
        User user = new User(null, null);

        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/users")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Updated text");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));
        given(userService.saveUser(any(User.class))).willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(user.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());
        User user = new User(userId, "Updated text");

        this.mockMvc
                .perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "Some text");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));
        doNothing().when(userService).deleteUserById(user.getId());

        this.mockMvc
                .perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(user.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNotFound());
    }
}

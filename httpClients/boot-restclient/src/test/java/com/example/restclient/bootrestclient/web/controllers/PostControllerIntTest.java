package com.example.restclient.bootrestclient.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void shouldFindPostById() throws Exception {

        this.mockMvc
                .perform(get("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(
                        jsonPath(
                                "$.title",
                                is(
                                        "sunt aut facere repellat provident occaecati excepturi optio reprehenderit")))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(
                        jsonPath(
                                "$.body",
                                is(
                                        "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto")));
    }
}

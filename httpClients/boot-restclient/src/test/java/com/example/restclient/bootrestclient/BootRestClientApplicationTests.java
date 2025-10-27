package com.example.restclient.bootrestclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

@SpringBootTest
@AutoConfigureMockMvc
class BootRestClientApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RestClient.Builder builder;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer =
                MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
    }

    @Test
    void findPostById() throws Exception {
        // Mock the external API response
        String mockApiResponse =
                """
                {
                  "userId": 1,
                  "id": 1,
                  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                  "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto"
                }
                """;
        mockServer
                .expect(times(1), requestTo("https://jsonplaceholder.typicode.com/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(mockApiResponse, MediaType.APPLICATION_JSON));

        // Perform the test
        String result = this.mockMvc
                .perform(get("/api/posts/{postId}", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify the result
        assertThat(result).isEqualToIgnoringWhitespace(mockApiResponse);

        mockServer.reset();
        // Verify that the expected API call was made
        mockServer.verify();
    }

    @Test
    void createPost() throws Exception {
        // Mock the external API response
        String mockApiResponse =
                """
                {
                  "userId": 1,
                  "id": 101,
                  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                  "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto"
                }
                """;
        String mockApiRequest =
                """
                {
                  "userId": 1,
                  "id": 1,
                  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                  "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto"
                }
                """;
        mockServer
                .expect(times(1), requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(mockApiResponse, MediaType.APPLICATION_JSON));

        // Perform the test
        String result = this.mockMvc
                .perform(post("/api/posts").content(mockApiRequest).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify the result
        assertThat(result).isEqualToIgnoringWhitespace(mockApiResponse);

        mockServer.reset();
        // Verify that the expected API call was made
        mockServer.verify();
    }
}

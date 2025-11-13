package com.example.restclient.bootrestclient.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.restclient.bootrestclient.config.ClientLoggerRequestInterceptor;
import com.example.restclient.bootrestclient.config.RestClientConfiguration;
import com.example.restclient.bootrestclient.model.response.PostDto;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.json.JsonMapper;

@RestClientTest(
        components = {
            PostService.class,
            HttpClientService.class,
            RestClientConfiguration.class,
            ClientLoggerRequestInterceptor.class
        })
class PostServiceTest {

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private PostService postService;

    @Test
    void findPostById() {

        PostDto postDto = new PostDto(1000L, 583L, "JunitTitle", "Response from RestClientTest");
        this.mockRestServiceServer
                .expect(requestTo("https://jsonplaceholder.typicode.com/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header("apiKey", "123456"))
                .andRespond(withSuccess(jsonMapper.writeValueAsString(postDto), MediaType.APPLICATION_JSON));

        Optional<PostDto> optionalPostDto = postService.findPostById(1L);

        assertThat(optionalPostDto).isPresent();
        assertThat(optionalPostDto.get().title()).isEqualTo("JunitTitle");

        mockRestServiceServer.verify();
    }
}

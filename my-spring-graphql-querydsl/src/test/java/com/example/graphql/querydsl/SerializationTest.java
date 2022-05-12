package com.example.graphql.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;
import com.example.graphql.querydsl.model.request.PostRequestDTO;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
class SerializationTest {

    @Autowired private JacksonTester<PostRequestDTO> jacksonTester;

    @Test
    void testJson() throws IOException {
        List<PostCommentsDTO> comments =
                List.of(new PostCommentsDTO("review1"), new PostCommentsDTO("review2"));
        List<TagDTO> tags = List.of(new TagDTO("java"));
        PostRequestDTO postRequestDTO =
                new PostRequestDTO("junit", "title", "content", comments, tags);
        JsonContent<PostRequestDTO> json = jacksonTester.write(postRequestDTO);
        assertThat(json.getJson())
                .isEqualTo(
                        "{\"name\":\"junit\",\"title\":\"title\",\"content\":\"content\",\"comments\":[{\"review\":\"review1\"},{\"review\":\"review2\"}],\"tags\":[{\"name\":\"java\"}]}");
    }
}

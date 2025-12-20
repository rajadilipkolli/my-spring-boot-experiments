package com.example.opensearch;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.opensearch.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.Response;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void restClientOpenSearchNodeVersion() throws IOException {
        final Request request = new Request("GET", "/");
        final Response response = restClient.performRequest(request);
        try (InputStream input = response.getEntity().getContent()) {
            JsonNode result = new ObjectMapper().readTree(input);
            assertThat(result.path("version").path("number").asText()).isEqualTo("3.3.1");
        }
    }
}

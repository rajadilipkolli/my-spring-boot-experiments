package com.example.opensearch;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.opensearch.common.AbstractIntegrationTest;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void restClientOpenSearchNodeVersion() throws IOException {
        final Request request = new Request("GET", "/");
        final Response response = restClient.performRequest(request);
        try (InputStream input = response.getEntity().getContent()) {
            JsonNode result = new JsonMapper().readTree(input);
            assertThat(result.path("version").path("number").asString()).isEqualTo("3.4.0");
        }
    }
}

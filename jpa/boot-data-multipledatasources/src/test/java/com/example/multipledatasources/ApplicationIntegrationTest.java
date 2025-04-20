package com.example.multipledatasources;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.multipledatasources.common.ContainersConfiguration;
import com.example.multipledatasources.dto.ResponseDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ContainersConfiguration.class})
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void verifyBootStrap() {

        this.mockMvcTester
                .get()
                .uri("/details/{memberId}", "1")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(ResponseDto.class)
                .satisfies(responseDto -> {
                    assertThat(responseDto.memberId()).isEqualTo("1");
                    assertThat(responseDto.cardNumber()).isEqualTo("4111111111111111");
                    assertThat(responseDto.memberName()).isEqualTo("raja");
                });
    }

    @Test
    void shouldHandleConcurrentRequests() {
        // Test multiple concurrent requests
        List<CompletableFuture<Void>> futures = IntStream.range(10, 20)
                .mapToObj(i -> CompletableFuture.runAsync(() -> this.mockMvcTester
                        .get()
                        .uri("/details/{memberId}", String.valueOf(i))
                        .accept(MediaType.APPLICATION_JSON)
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .bodyJson()
                        .convertTo(ProblemDetail.class)
                        .satisfies(problemDetail ->
                                assertThat(problemDetail.getTitle()).isEqualTo("Not Found"))))
                .toList();

        // Verify all requests complete successfully
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}

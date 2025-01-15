package com.example.multipledatasources;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.multipledatasources.common.ContainersConfiguration;
import com.example.multipledatasources.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

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
                    assertThat(responseDto.cardNumber()).isEqualTo("1234-5678-9012-3456");
                    assertThat(responseDto.memberName()).isEqualTo("raja");
                });
    }
}

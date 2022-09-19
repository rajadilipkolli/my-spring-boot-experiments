package com.example.multipledatasources.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
class ResponseDtoJsonTest {

    @Autowired private JacksonTester<ResponseDto> json;

    @Test
    void testSerialize() throws Exception {

        ResponseDto responseDto = new ResponseDto("1", "1234-5678-9012-3456", "raja");

        JsonContent<ResponseDto> result = this.json.write(responseDto);

        assertThat(result).hasJsonPathStringValue("$.memberId");
        assertThat(result).extractingJsonPathStringValue("$.memberId").isEqualTo("1");
        assertThat(result)
                .extractingJsonPathStringValue("$.cardNumber")
                .isEqualTo("1234-5678-9012-3456");
        assertThat(result).extractingJsonPathStringValue("$.memberName").isEqualTo("raja");
        assertThat(result).doesNotHaveJsonPath("$.enabled");
    }

    @Test
    void testDeserialize() throws Exception {

        String jsonContent =
                """
                {
                  "memberId": "1",
                  "cardNumber": "1234-5678-9012-3456",
                  "memberName": "raja"
                }
                """;

        ResponseDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.memberId()).isEqualTo("1");
        assertThat(result.cardNumber()).isEqualTo("1234-5678-9012-3456");
        assertThat(result.memberName()).isEqualTo("raja");
    }
}

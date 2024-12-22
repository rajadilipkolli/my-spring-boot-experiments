package com.example.plugin.strategyplugin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.example.plugin.strategyplugin.domain.GenericDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StrategyPluginApplicationTests {
  
  @Autowired private MockMvcTester mockMvcTester;

  @Test
  void fetchingPDF() {
    this.mockMvcTester.get().uri("/fetch").param("type", "pdf")
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasContentType(MediaType.APPLICATION_JSON)
            .hasStatusOk()
            .bodyJson().convertTo(GenericDTO.class)
            .satisfies(genericDTO -> assertThat(genericDTO.message()).isEqualTo("Writing pdf Hello "));
  }

  @Test
  void fetchingCSV() {
    this.mockMvcTester.get().uri("/fetch").param("type", "csv")
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasContentType(MediaType.APPLICATION_JSON)
            .hasStatusOk()
            .bodyJson().convertTo(GenericDTO.class)
            .satisfies(genericDTO -> assertThat(genericDTO.message()).isEqualTo("Writing CSV Hello "));
  }

  @Test
  void fetchingExcel() {
    this.mockMvcTester.get().uri("/fetch").param("type", "xls")
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().convertTo(ProblemDetail.class)
            .satisfies(problemDetail -> {
                assertThat(problemDetail.getDetail()).isEqualTo("Plugin not found for type: xls");
                assertThat(problemDetail.getTitle()).isEqualTo("Product Not Found");
                assertThat(problemDetail.getStatus()).isEqualTo(400);
                assertThat(problemDetail.getType().toString()).isEqualTo("https://api.service.com/errors/bad-request");
                assertThat(problemDetail.getInstance()).isEqualTo("/fetch");
            });
  }
}
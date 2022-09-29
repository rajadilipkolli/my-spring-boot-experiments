package com.example.plugin.strategyplugin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StrategyPluginApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void fetchingPDF() throws Exception {
    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/fetch")
                .param("type", "pdf")
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Writing pdf Hello "));
  }

  @Test
  void fetchingCSV() throws Exception {
    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/fetch")
                .param("type", "csv")
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("Writing CSV Hello "));
  }
}

package com.example.multipledatasources.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.service.DetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DetailsController.class)
class DetailsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DetailsService detailsService;

    @Test
    void getDetails() throws Exception {
        given(detailsService.getDetails("1"))
                .willReturn(new ResponseDto("1", "1234-5678-9012-3456", "raja"));
        this.mockMvc
                .perform(get("/details/{memberId}", "1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        content()
                                .json(
                                        """
                {"memberId":"1","cardNumber":"1234-5678-9012-3456","memberName":"raja"}
                """));
    }
}

package com.example.shortener.controllers;

import com.example.shortener.services.UrlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateRedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlValidator validator;

    @Test
    void createRedirectReturnsShortUrlAndSecretKey() throws Exception {
        when(validator.validateAndGetError("https://example.com"))
                .thenReturn(null);

        mockMvc.perform(
                        post("/make_shorter")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "longUrl": "https://example.com"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.secretKey").isNotEmpty());
    }
}
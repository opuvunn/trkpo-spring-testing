package com.example.shortener.controllers;

import com.example.shortener.services.UrlValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateRedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlValidator validator;

    @Test
    void createRedirectAndFollowShortUrl() throws Exception {

        String longUrl = "https://example.com";

        // Единственную внешнюю систему подменяем:
        // тест не должен реально ходить на example.com.
        when(validator.validateAndGetError(longUrl))
                .thenReturn(null);

        // Пользователь просит создать короткую ссылку.
        MvcResult createResult = mockMvc.perform(
                        post("/make_shorter")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "longUrl": "https://example.com"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.secretKey").isNotEmpty())
                .andReturn();

        // Получаем из ответа приложения shortUrl и secretKey.
        JsonNode response = objectMapper.readTree(
                createResult.getResponse().getContentAsString()
        );

        String shortUrl = response.get("shortUrl").asText();
        String secretKey = response.get("secretKey").asText();

        try {
            // Из полного короткого URL берём путь, например "/abc".
            String shortPath = URI.create(shortUrl).getPath();

            // Теперь ведём себя как пользователь:
            // открываем только что полученную короткую ссылку.
            mockMvc.perform(get(shortPath))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", longUrl));

        } finally {
            // Даже если проверка выше упадёт,
            // пытаемся удалить созданную тестом ссылку.
            mockMvc.perform(delete("/admin/" + secretKey));
        }
    }
}
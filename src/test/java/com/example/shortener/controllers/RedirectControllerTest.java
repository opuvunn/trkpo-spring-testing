package com.example.shortener.controllers;

import com.example.shortener.model.Redirection;
import com.example.shortener.model.RedirectionRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedirectionRepo repo;
    //Проверяет, что переход по shortKey возвращает код 302 и исходный адрес в заголовке Location
    @Test
    void redirectReturns302AndLocationHeader() throws Exception {
        String shortKey = UUID.randomUUID().toString();

        Redirection redirection =
                new Redirection("https://example.com", shortKey, "secret");

        repo.save(redirection);

        mockMvc.perform(get("/" + shortKey))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://example.com"
                ));

        repo.delete(redirection);
    }
}
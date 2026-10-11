package com.example.shortener.controllers;

import com.example.shortener.model.Redirection;
import com.example.shortener.model.RedirectionRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedirectionRepo repo;

    @Test
    void getStatsReturnsCreationDateAndUsageCount() throws Exception {
        String shortKey = UUID.randomUUID().toString();
        String secretKey = UUID.randomUUID().toString();

        Redirection redirection =
                new Redirection("https://example.com", shortKey, secretKey);

        redirection.incrementUsageCount();
        redirection.incrementUsageCount();

        repo.save(redirection);

        mockMvc.perform(get("/admin/" + secretKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.usageCount").value(2));

        repo.delete(redirection);
    }

    @Test
    void deleteRedirectionDeletesBySecretKey() throws Exception {
        String shortKey = UUID.randomUUID().toString();
        String secretKey = UUID.randomUUID().toString();

        Redirection redirection =
                new Redirection("https://example.com", shortKey, secretKey);

        repo.save(redirection);

        mockMvc.perform(delete("/admin/" + secretKey))
                .andExpect(status().isOk());

        assertTrue(repo.findBySecretKey(secretKey).isEmpty());
    }
}
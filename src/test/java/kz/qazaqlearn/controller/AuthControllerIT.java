package kz.qazaqlearn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.qazaqlearn.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLoginShouldReturnToken() throws Exception {
        var registerPayload = objectMapper.writeValueAsString(
                new AuthPayload("Test User", "test@example.com", "Password123", "STUDENT")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        var loginPayload = objectMapper.writeValueAsString(
                new LoginPayload("test@example.com", "Password123")
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    private record AuthPayload(String fullName, String email, String password, String role) {}
    private record LoginPayload(String email, String password) {}
}

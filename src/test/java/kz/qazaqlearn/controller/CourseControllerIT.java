package kz.qazaqlearn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.qazaqlearn.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listCoursesWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teacherCanCreateCourse() throws Exception {
        String token = registerAndLoginTeacher();
        var payload = objectMapper.writeValueAsString(new CoursePayload("Қазақ тілі","Description in Kazakh"));

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    private String registerAndLoginTeacher() throws Exception {
        var registerPayload = objectMapper.writeValueAsString(
                new AuthPayload("Teacher","teacher@example.com","Password123","TEACHER")
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        var loginPayload = objectMapper.writeValueAsString(
                new LoginPayload("teacher@example.com", "Password123")
        );
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private record AuthPayload(String fullName, String email, String password, String role) {}
    private record LoginPayload(String email, String password) {}
    private record CoursePayload(String titleKk, String descriptionKk) {}
}

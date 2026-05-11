package kz.qazaqlearn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.qazaqlearn.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void studentCanEnrollInPublishedCourse() throws Exception {
        String teacherToken = registerAndLoginTeacher();
        String courseId = createAndPublishCourse(teacherToken);
        String studentToken = registerAndLoginStudent();

        mockMvc.perform(post("/api/courses/" + courseId + "/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/me/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    void teacherCanViewEnrolledStudents() throws Exception {
        String teacherToken = registerAndLoginTeacher();
        String courseId = createAndPublishCourse(teacherToken);
        String studentToken = registerAndLoginStudent();

        mockMvc.perform(post("/api/courses/" + courseId + "/enroll")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var result = mockMvc.perform(get("/api/courses/" + courseId + "/students")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.get(0).get("email").asText()).isEqualTo("student@example.com");
        assertThat(json.get(0).get("fullName").asText()).isEqualTo("Student");
        assertThat(json.get(0).get("totalLessonsCount").asInt()).isEqualTo(0);
    }

    @Test
    void studentCannotViewCourseStudents() throws Exception {
        String teacherToken = registerAndLoginTeacher();
        String courseId = createAndPublishCourse(teacherToken);
        String studentToken = registerAndLoginStudent();

        mockMvc.perform(get("/api/courses/" + courseId + "/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    private String registerAndLoginTeacher() throws Exception {
        var registerPayload = objectMapper.writeValueAsString(
                new AuthPayload("Teacher","enroll-teacher@example.com","Password123","TEACHER")
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        var loginPayload = objectMapper.writeValueAsString(
                new LoginPayload("enroll-teacher@example.com", "Password123")
        );
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String registerAndLoginStudent() throws Exception {
        var registerPayload = objectMapper.writeValueAsString(
                new AuthPayload("Student","student@example.com","Password123","STUDENT")
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        var loginPayload = objectMapper.writeValueAsString(
                new LoginPayload("student@example.com", "Password123")
        );
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String createAndPublishCourse(String token) throws Exception {
        var payload = objectMapper.writeValueAsString(new CoursePayload("Published course","Описание"));
        var result = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = json.get("id").asText();

        mockMvc.perform(patch("/api/courses/" + id + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        return id;
    }

    private record AuthPayload(String fullName, String email, String password, String role) {}
    private record LoginPayload(String email, String password) {}
    private record CoursePayload(String titleKk, String descriptionKk) {}
}

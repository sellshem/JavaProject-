package kz.qazaqlearn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityAuthorizationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        String token = registerAndLogin("security-student@example.com", "STUDENT");
        var coursePayload = objectMapper.writeValueAsString(new CoursePayload("Unauthorized","Описание"));

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(coursePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCannotEditAnotherTeachersCourse() throws Exception {
        String teacherOneToken = registerAndLogin("teacher-one@example.com", "TEACHER");
        String courseId = createCourse(teacherOneToken, "Course One", "Описание");
        String teacherTwoToken = registerAndLogin("teacher-two@example.com", "TEACHER");

        var updatePayload = objectMapper.writeValueAsString(new CoursePayload("Edited Title","Updated описание"));
        mockMvc.perform(put("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + teacherTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageAllCourses() throws Exception {
        String teacherToken = registerAndLogin("course-teacher@example.com", "TEACHER");
        String courseId = createCourse(teacherToken, "Course Manage","Описание");
        String adminToken = registerAndLogin("admin@example.com", "ADMIN");

        var updatePayload = objectMapper.writeValueAsString(new CoursePayload("Admin Edited","Updated описание"));
        mockMvc.perform(put("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotAccessUnpublishedCourse() throws Exception {
        String teacherToken = registerAndLogin("course-owner@example.com", "TEACHER");
        String courseId = createCourse(teacherToken, "Hidden Course", "Описание");
        String studentToken = registerAndLogin("course-student@example.com", "STUDENT");

        mockMvc.perform(get("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanAccessPublishedCourse() throws Exception {
        String teacherToken = registerAndLogin("published-owner@example.com", "TEACHER");
        String courseId = createCourse(teacherToken, "Published Course", "Описание");
        mockMvc.perform(post("/api/courses/" + courseId + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        String studentToken = registerAndLogin("published-student@example.com", "STUDENT");
        mockMvc.perform(get("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    private String registerAndLogin(String email, String role) throws Exception {
        var registerPayload = objectMapper.writeValueAsString(
                new AuthPayload("User", email, "Password123", role)
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        var loginPayload = objectMapper.writeValueAsString(
                new LoginPayload(email, "Password123")
        );
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String createCourse(String token, String title, String description) throws Exception {
        var payload = objectMapper.writeValueAsString(new CoursePayload(title, description));
        var result = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private record AuthPayload(String fullName, String email, String password, String role) {}
    private record LoginPayload(String email, String password) {}
    private record CoursePayload(String titleKk, String descriptionKk) {}
}

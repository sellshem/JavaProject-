package kz.qazaqlearn.controller;

import kz.qazaqlearn.domain.*;
import kz.qazaqlearn.dto.AssignmentDto;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AssignmentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacher;
    private User student;
    private Course course;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setFullName("Teacher User");
        teacher.setEmail("teacher@example.com");
        teacher.setPassword(passwordEncoder.encode("Password123"));
        teacher.setRole(Role.TEACHER);
        userRepository.save(teacher);

        student = new User();
        student.setId(UUID.randomUUID());
        student.setFullName("Student User");
        student.setEmail("student@example.com");
        student.setPassword(passwordEncoder.encode("Password123"));
        student.setRole(Role.STUDENT);
        userRepository.save(student);

        course = new Course();
        course.setId(UUID.randomUUID());
        course.setTitleKk("Test Course");
        course.setDescriptionKk("Description");
        course.setTeacher(teacher);
        course.setPublished(true);
        courseRepository.save(course);

        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        teacherToken = jwtTokenProvider.generateToken(teacher.getEmail(), teacher.getId(), teacher.getRole().name());
        studentToken = jwtTokenProvider.generateToken(student.getEmail(), student.getId(), student.getRole().name());
    }

    @Test
    void createAssignment_asTeacher_success() throws Exception {
        mockMvc.perform(post("/api/courses/{courseId}/assignments", course.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titleKk\":\"Title\",\"descriptionKk\":\"Desc\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleKk").value("Title"));
    }

    @Test
    void getAssignmentsByCourse_asStudent_success() throws Exception {
        Assignment assignment = new Assignment();
        assignment.setId(UUID.randomUUID());
        assignment.setCourse(course);
        assignment.setTitleKk("Test Assignment");
        assignment.setDescriptionKk("Desc");
        assignmentRepository.save(assignment);

        mockMvc.perform(get("/api/courses/{courseId}/assignments", course.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titleKk").value("Test Assignment"));
    }

    @Test
    void getAssignmentsByCourse_asStudent_notEnrolled_returns403() throws Exception {
        Course otherCourse = new Course();
        otherCourse.setId(UUID.randomUUID());
        otherCourse.setTitleKk("Other Course");
        otherCourse.setDescriptionKk("Desc");
        otherCourse.setTeacher(teacher);
        otherCourse.setPublished(true);
        courseRepository.save(otherCourse);

        mockMvc.perform(get("/api/courses/{courseId}/assignments", otherCourse.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
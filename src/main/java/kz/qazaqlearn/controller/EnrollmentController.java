package kz.qazaqlearn.controller;

import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.CourseResponse;
import kz.qazaqlearn.dto.CourseStudentResponse;
import kz.qazaqlearn.dto.EnrollmentResponse;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    public EnrollmentController(EnrollmentService enrollmentService, UserRepository userRepository) {
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable("courseId") UUID courseId,
                                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(enrollmentService.enroll(courseId, user));
    }

    @GetMapping("/api/me/courses")
    public ResponseEntity<List<CourseResponse>> getMyCourses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(enrollmentService.getMyCourses(user));
    }

    @GetMapping("/api/courses/{courseId}/students")
    public ResponseEntity<List<CourseStudentResponse>> getCourseStudents(@PathVariable("courseId") UUID courseId,
                                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(enrollmentService.getCourseStudents(courseId, user));
    }

    @DeleteMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<Void> withdraw(@PathVariable("courseId") UUID courseId,
                                         @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        enrollmentService.withdraw(courseId, user);
        return ResponseEntity.noContent().build();
    }
}

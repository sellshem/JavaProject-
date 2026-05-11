package kz.qazaqlearn.controller;

import jakarta.validation.Valid;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.CourseCreateRequest;
import kz.qazaqlearn.dto.CourseResponse;
import kz.qazaqlearn.dto.CourseUpdateRequest;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;

    public CourseController(CourseService courseService, UserRepository userRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return ResponseEntity.ok(courseService.createCourse(request, user));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser != null ? getCurrentUser(currentUser) : createTestStudentUser();
        return ResponseEntity.ok(courseService.getCourses(user));
    }

    private User createTestStudentUser() {
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setRole(Role.STUDENT);
        return testUser;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable("id") UUID id,
                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return ResponseEntity.ok(courseService.getCourseById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable("id") UUID id,
                                                       @Valid @RequestBody CourseUpdateRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return ResponseEntity.ok(courseService.updateCourse(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("id") UUID id,
                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        courseService.deleteCourse(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(@PathVariable("id") UUID id,
                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return ResponseEntity.ok(courseService.publishCourse(id, user));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<CourseResponse> unpublishCourse(@PathVariable("id") UUID id,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return ResponseEntity.ok(courseService.unpublishCourse(id, user));
    }

    private User getCurrentUser(CustomUserDetails currentUser) {
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }
}

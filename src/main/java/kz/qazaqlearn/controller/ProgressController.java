package kz.qazaqlearn.controller;

import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.ProgressResponse;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    public ProgressController(ProgressService progressService, UserRepository userRepository) {
        this.progressService = progressService;
        this.userRepository = userRepository;
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<ProgressResponse> completeLesson(@PathVariable("lessonId") UUID lessonId,
                                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(progressService.completeLesson(lessonId, user));
    }

    @GetMapping("/me/progress")
    public ResponseEntity<List<ProgressResponse>> getMyProgress(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(progressService.getMyProgress(user));
    }

    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<List<ProgressResponse>> getCourseProgress(@PathVariable("courseId") UUID courseId,
                                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(progressService.getCourseProgress(courseId, user));
    }

    @GetMapping("/courses/{courseId}/students/{studentId}/progress")
    public ResponseEntity<List<ProgressResponse>> getStudentProgress(@PathVariable("courseId") UUID courseId,
                                                                      @PathVariable("studentId") UUID studentId,
                                                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(progressService.getStudentProgress(courseId, studentId, user));
    }
}

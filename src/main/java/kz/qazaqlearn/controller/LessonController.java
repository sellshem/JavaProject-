package kz.qazaqlearn.controller;

import jakarta.validation.Valid;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.LessonCreateRequest;
import kz.qazaqlearn.dto.LessonResponse;
import kz.qazaqlearn.dto.LessonUpdateRequest;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class LessonController {

    private final LessonService lessonService;
    private final UserRepository userRepository;

    public LessonController(LessonService lessonService, UserRepository userRepository) {
        this.lessonService = lessonService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/courses/{courseId}/lessons")
    public ResponseEntity<LessonResponse> createLesson(@PathVariable("courseId") UUID courseId,
                                                       @Valid @RequestBody LessonCreateRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.createLesson(courseId, request, user));
    }

    @GetMapping("/api/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonResponse>> getLessons(@PathVariable("courseId") UUID courseId,
                                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.getLessons(courseId, user));
    }

    @GetMapping("/api/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable("lessonId") UUID lessonId,
                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.getLessonById(lessonId, user));
    }

    @PutMapping("/api/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable("lessonId") UUID lessonId,
                                                       @Valid @RequestBody LessonUpdateRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request, user));
    }

    @DeleteMapping("/api/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable("lessonId") UUID lessonId,
                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        lessonService.deleteLesson(lessonId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/lessons/{lessonId}/publish")
    public ResponseEntity<LessonResponse> publishLesson(@PathVariable("lessonId") UUID lessonId,
                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.publishLesson(lessonId, user));
    }

    @PatchMapping("/api/lessons/{lessonId}/unpublish")
    public ResponseEntity<LessonResponse> unpublishLesson(@PathVariable("lessonId") UUID lessonId,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(lessonService.unpublishLesson(lessonId, user));
    }
}

package kz.qazaqlearn.controller;

import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.AssignmentDto;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    public AssignmentController(AssignmentService assignmentService, UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<AssignmentDto.AssignmentResponse> createAssignment(@PathVariable("courseId") UUID courseId,
                                                                              @Valid @RequestBody AssignmentDto.AssignmentRequest request,
                                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        request = new AssignmentDto.AssignmentRequest(courseId, request.lessonId(), request.titleKk(), request.descriptionKk(), request.deadline());
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(assignmentService.createAssignment(request, user));
    }

    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<List<AssignmentDto.AssignmentResponse>> getAssignmentsByCourse(@PathVariable("courseId") UUID courseId,
                                                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId, user));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentDto.AssignmentResponse> getAssignment(@PathVariable("assignmentId") UUID assignmentId,
                                                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(assignmentService.getAssignment(assignmentId, user));
    }

    @PutMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentDto.AssignmentResponse> updateAssignment(@PathVariable("assignmentId") UUID assignmentId,
                                                                              @Valid @RequestBody AssignmentDto.AssignmentRequest request,
                                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, request, user));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable("assignmentId") UUID assignmentId,
                                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        assignmentService.deleteAssignment(assignmentId, user);
        return ResponseEntity.noContent().build();
    }
}
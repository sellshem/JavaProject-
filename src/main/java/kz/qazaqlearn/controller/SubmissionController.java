package kz.qazaqlearn.controller;

import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.SubmissionDto;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.CustomUserDetails;
import kz.qazaqlearn.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    public SubmissionController(SubmissionService submissionService, UserRepository userRepository) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionDto.SubmissionResponse> submitAssignment(@PathVariable("assignmentId") UUID assignmentId,
                                                                              @Valid @RequestBody SubmissionDto.SubmissionRequest request,
                                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(submissionService.submitAssignment(assignmentId, request, user));
    }

    @GetMapping("/me/submissions")
    public ResponseEntity<List<SubmissionDto.SubmissionResponse>> getMySubmissions(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(submissionService.getMySubmissions(user));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionDto.SubmissionResponse>> getSubmissionsByAssignment(@PathVariable("assignmentId") UUID assignmentId,
                                                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId, user));
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<SubmissionDto.SubmissionResponse> gradeSubmission(@PathVariable("submissionId") UUID submissionId,
                                                                             @Valid @RequestBody SubmissionDto.GradeRequest request,
                                                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        return ResponseEntity.ok(submissionService.gradeSubmission(submissionId, request, user));
    }
}
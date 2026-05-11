package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Assignment;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.Submission;
import kz.qazaqlearn.domain.SubmissionStatus;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.domain.events.AssignmentSubmittedEvent;
import kz.qazaqlearn.dto.SubmissionDto;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.SubmissionMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.SubmissionRepository;
import kz.qazaqlearn.service.AuditLogService;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionMapper submissionMapper;
    private final AuditLogService auditLogService;
    private final KafkaEventPublisher kafkaEventPublisher;

    public SubmissionService(SubmissionRepository submissionRepository,
                             AssignmentRepository assignmentRepository,
                             EnrollmentRepository enrollmentRepository,
                             SubmissionMapper submissionMapper,
                             AuditLogService auditLogService,
                             KafkaEventPublisher kafkaEventPublisher) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionMapper = submissionMapper;
        this.auditLogService = auditLogService;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @PreAuthorize("hasRole('STUDENT')")
    public SubmissionDto.SubmissionResponse submitAssignment(UUID assignmentId, SubmissionDto.SubmissionRequest request, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        // Check if enrolled
        boolean enrolled = enrollmentRepository.existsByCourseAndStudent(assignment.getCourse(), currentUser);
        if (!enrolled) {
            throw new AccessDeniedException("Student must be enrolled in the course");
        }
        // Check if already submitted
        Submission existing = submissionRepository.findByAssignmentAndStudent(assignment, currentUser).orElse(null);
        if (existing != null && existing.getStatus() != SubmissionStatus.RETURNED) {
            throw new BadRequestException("Submission already exists and is not returned");
        }
        if (existing != null) {
            // Update existing submission
            existing.setAnswerText(request.answerText());
            existing.setStatus(SubmissionStatus.SUBMITTED);
            existing.setGrade(null);
            existing.setFeedbackKk(null);
            existing.setSubmittedAt(LocalDateTime.now());
            existing.setGradedAt(null);
            Submission saved = submissionRepository.save(existing);
            auditLogService.logAction(currentUser, "SUBMISSION_RESUBMIT", "Submission", saved.getId(), null);
            return submissionMapper.toDto(saved);
         } else {
             // Create new submission
             Submission submission = submissionMapper.toEntity(request);
             submission.setAssignment(assignment);
             submission.setStudent(currentUser);
             submission.setStatus(SubmissionStatus.SUBMITTED);
             Submission saved = submissionRepository.save(submission);
             auditLogService.logAction(currentUser, "SUBMISSION_CREATE", "Submission", saved.getId(), null);
             
             // Publish Kafka event
             AssignmentSubmittedEvent event = new AssignmentSubmittedEvent(
                 saved.getId(),
                 assignmentId,
                 assignment.getCourse().getId(),
                 currentUser.getId(),
                 LocalDateTime.now()
             );
             kafkaEventPublisher.publishAssignmentSubmittedEvent(event);
             
             return submissionMapper.toDto(saved);
         }
    }

    @PreAuthorize("hasRole('STUDENT')")
    public List<SubmissionDto.SubmissionResponse> getMySubmissions(User currentUser) {
        return submissionRepository.findByStudent(currentUser).stream()
                .map(submissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<SubmissionDto.SubmissionResponse> getSubmissionsByAssignment(UUID assignmentId, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        if (currentUser.getRole() == Role.TEACHER && !assignment.getCourse().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view submissions for own courses");
        }
        return submissionRepository.findByAssignment(assignment).stream()
                .map(submissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public SubmissionDto.SubmissionResponse gradeSubmission(UUID submissionId, SubmissionDto.GradeRequest request, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        Assignment assignment = submission.getAssignment();
        if (currentUser.getRole() == Role.TEACHER && !assignment.getCourse().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only grade submissions for own courses");
        }
        submission.setGrade(request.grade());
        submission.setFeedbackKk(request.feedbackKk());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission);
        auditLogService.logAction(currentUser, "SUBMISSION_GRADE", "Submission", saved.getId(), null);
        return submissionMapper.toDto(saved);
    }
}
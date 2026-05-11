package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.*;
import kz.qazaqlearn.dto.SubmissionDto;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.SubmissionMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.SubmissionRepository;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private SubmissionService submissionService;

    private User teacher;
    private User student;
    private Course course;
    private Assignment assignment;
    private Submission submission;
    private SubmissionDto.SubmissionRequest request;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);

        student = new User();
        student.setId(UUID.randomUUID());
        student.setRole(Role.STUDENT);

        course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);

        assignment = new Assignment();
        assignment.setId(UUID.randomUUID());
        assignment.setCourse(course);
        assignment.setTitleKk("Test Assignment");

        submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setAnswerText("Answer");
        submission.setStatus(SubmissionStatus.SUBMITTED);

        student.setFullName("Test Student");
        student.setEmail("student@example.com");

        request = new SubmissionDto.SubmissionRequest("Answer");
    }

    @Test
    void submitAssignment_asStudent_enrolled_success() {
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByCourseAndStudent(course, student)).thenReturn(true);
        when(submissionRepository.findByAssignmentAndStudent(assignment, student)).thenReturn(Optional.empty());
        when(submissionMapper.toEntity(request)).thenReturn(submission);
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        when(submissionMapper.toDto(submission)).thenReturn(new SubmissionDto.SubmissionResponse(
                submission.getId(),
                assignment.getId(),
                assignment.getTitleKk(),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                submission.getAnswerText(),
                null,
                null,
                LocalDateTime.now(),
                null,
                SubmissionStatus.SUBMITTED));

        SubmissionDto.SubmissionResponse response = submissionService.submitAssignment(assignment.getId(), request, student);

        assertThat(response.id()).isEqualTo(submission.getId());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void submitAssignment_asStudent_notEnrolled_throwsAccessDenied() {
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByCourseAndStudent(course, student)).thenReturn(false);

        assertThatThrownBy(() -> submissionService.submitAssignment(assignment.getId(), request, student))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void submitAssignment_alreadySubmitted_throwsBadRequest() {
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByCourseAndStudent(course, student)).thenReturn(true);
        when(submissionRepository.findByAssignmentAndStudent(assignment, student)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.submitAssignment(assignment.getId(), request, student))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getMySubmissions_asStudent_success() {
        when(submissionRepository.findByStudent(student)).thenReturn(List.of(submission));
        when(submissionMapper.toDto(submission)).thenReturn(new SubmissionDto.SubmissionResponse(
                submission.getId(),
                assignment.getId(),
                assignment.getTitleKk(),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                submission.getAnswerText(),
                null,
                null,
                LocalDateTime.now(),
                null,
                SubmissionStatus.SUBMITTED));

        List<SubmissionDto.SubmissionResponse> responses = submissionService.getMySubmissions(student);

        assertThat(responses).hasSize(1);
    }

    @Test
    void gradeSubmission_asTeacher_success() {
        submission.setStatus(SubmissionStatus.SUBMITTED);
        SubmissionDto.GradeRequest gradeRequest = new SubmissionDto.GradeRequest(95, "Good job");

        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        when(submissionMapper.toDto(submission)).thenReturn(new SubmissionDto.SubmissionResponse(
                submission.getId(),
                assignment.getId(),
                assignment.getTitleKk(),
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                submission.getAnswerText(),
                95,
                "Good job",
                LocalDateTime.now(),
                LocalDateTime.now(),
                SubmissionStatus.GRADED));

        SubmissionDto.SubmissionResponse response = submissionService.gradeSubmission(submission.getId(), gradeRequest, teacher);

        assertThat(response.grade()).isEqualTo(95);
        assertThat(response.status()).isEqualTo(SubmissionStatus.GRADED);
    }
}
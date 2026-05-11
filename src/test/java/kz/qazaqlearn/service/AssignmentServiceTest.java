package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.*;
import kz.qazaqlearn.dto.AssignmentDto;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.AssignmentMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.LessonRepository;
import kz.qazaqlearn.repository.UserRepository;
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
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignmentMapper assignmentMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AssignmentService assignmentService;

    private User teacher;
    private User student;
    private Course course;
    private Lesson lesson;
    private Assignment assignment;
    private AssignmentDto.AssignmentRequest request;

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

        lesson = new Lesson();
        lesson.setId(UUID.randomUUID());
        lesson.setCourse(course);

        assignment = new Assignment();
        assignment.setId(UUID.randomUUID());
        assignment.setCourse(course);
        assignment.setLesson(lesson);
        assignment.setTitleKk("Test Assignment");
        assignment.setDescriptionKk("Description");
        assignment.setDeadline(LocalDateTime.now().plusDays(1));

        request = new AssignmentDto.AssignmentRequest(course.getId(), lesson.getId(), "Title", "Desc", LocalDateTime.now());
    }

    @Test
    void createAssignment_asTeacher_success() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(assignmentMapper.toEntity(request)).thenReturn(assignment);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assignmentMapper.toDto(assignment)).thenReturn(new AssignmentDto.AssignmentResponse(
                assignment.getId(), course.getId(), lesson.getId(), "Title", "Desc", assignment.getDeadline(), LocalDateTime.now(), LocalDateTime.now()));

        AssignmentDto.AssignmentResponse response = assignmentService.createAssignment(request, teacher);

        assertThat(response.id()).isEqualTo(assignment.getId());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void createAssignment_asTeacher_wrongCourse_throwsAccessDenied() {
        User otherTeacher = new User();
        otherTeacher.setId(UUID.randomUUID());
        otherTeacher.setRole(Role.TEACHER);
        course.setTeacher(otherTeacher);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> assignmentService.createAssignment(request, teacher))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getAssignmentsByCourse_asStudent_enrolled_success() {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        when(enrollmentRepository.existsByCourseAndStudent(course, student)).thenReturn(true);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(assignmentMapper.toDto(assignment)).thenReturn(new AssignmentDto.AssignmentResponse(
                assignment.getId(), course.getId(), lesson.getId(), "Title", "Desc", assignment.getDeadline(), LocalDateTime.now(), LocalDateTime.now()));

        List<AssignmentDto.AssignmentResponse> responses = assignmentService.getAssignmentsByCourse(course.getId(), student);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getAssignmentsByCourse_asStudent_notEnrolled_throwsAccessDenied() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> assignmentService.getAssignmentsByCourse(course.getId(), student))
                .isInstanceOf(AccessDeniedException.class);
    }
}
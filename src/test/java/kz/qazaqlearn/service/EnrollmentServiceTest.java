package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Assignment;
import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Enrollment;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Progress;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.Submission;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.CourseStudentResponse;
import kz.qazaqlearn.dto.EnrollmentResponse;
import kz.qazaqlearn.mapper.CourseMapper;
import kz.qazaqlearn.mapper.EnrollmentMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.LessonRepository;
import kz.qazaqlearn.repository.ProgressRepository;
import kz.qazaqlearn.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private EnrollmentMapper enrollmentMapper = Mappers.getMapper(EnrollmentMapper.class);
    private CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @InjectMocks
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(courseRepository, enrollmentRepository, lessonRepository,
                assignmentRepository, progressRepository, submissionRepository,
                enrollmentMapper, courseMapper, auditLogService, kafkaEventPublisher);
    }

    @Test
    void enrollShouldCreateEnrollmentForStudent() {
        UUID courseId = UUID.randomUUID();
        Course course = new Course();
        course.setId(courseId);
        course.setPublished(true);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseAndStudent(any(Course.class), any(User.class))).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User student = new User();
        student.setId(UUID.randomUUID());
        student.setRole(Role.STUDENT);

        EnrollmentResponse response = enrollmentService.enroll(courseId, student);

        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.studentId()).isEqualTo(student.getId());
    }

    @Test
    void getCourseStudentsShouldReturnStudentProgressSummary() {
        UUID courseId = UUID.randomUUID();
        Course course = new Course();
        course.setId(courseId);
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        course.setTeacher(teacher);

        User student = new User();
        student.setId(UUID.randomUUID());
        student.setFullName("Test Student");
        student.setEmail("student@example.com");

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(java.time.LocalDateTime.now());

        Lesson lesson1 = new Lesson();
        lesson1.setId(UUID.randomUUID());
        lesson1.setCourse(course);
        Lesson lesson2 = new Lesson();
        lesson2.setId(UUID.randomUUID());
        lesson2.setCourse(course);

        Assignment assignment1 = new Assignment();
        assignment1.setId(UUID.randomUUID());
        assignment1.setCourse(course);
        Assignment assignment2 = new Assignment();
        assignment2.setId(UUID.randomUUID());
        assignment2.setCourse(course);

        Progress progress = new Progress();
        progress.setCourse(course);
        progress.setStudent(student);
        progress.setCompleted(true);

        Submission submission = new Submission();
        submission.setAssignment(assignment1);
        submission.setStudent(student);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(List.of(enrollment));
        when(lessonRepository.findByCourseOrderByLessonOrderAsc(course)).thenReturn(List.of(lesson1, lesson2));
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment1, assignment2));
        when(progressRepository.findByCourseAndStudent(course, student)).thenReturn(List.of(progress));
        when(submissionRepository.findByStudent(student)).thenReturn(List.of(submission));

        var result = enrollmentService.getCourseStudents(courseId, teacher);

        assertThat(result).hasSize(1);
        var studentSummary = result.get(0);
        assertThat(studentSummary.studentId()).isEqualTo(student.getId());
        assertThat(studentSummary.fullName()).isEqualTo("Test Student");
        assertThat(studentSummary.email()).isEqualTo("student@example.com");
        assertThat(studentSummary.completedLessonsCount()).isEqualTo(1);
        assertThat(studentSummary.totalLessonsCount()).isEqualTo(2);
        assertThat(studentSummary.submittedAssignmentsCount()).isEqualTo(1);
        assertThat(studentSummary.totalAssignmentsCount()).isEqualTo(2);
        assertThat(studentSummary.progressPercent()).isEqualTo(50);
    }
}

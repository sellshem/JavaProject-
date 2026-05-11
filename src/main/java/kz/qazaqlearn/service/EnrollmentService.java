package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Enrollment;
import kz.qazaqlearn.domain.EnrollmentStatus;
import kz.qazaqlearn.domain.Progress;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.domain.events.CourseEnrollmentEvent;
import kz.qazaqlearn.dto.CourseResponse;
import kz.qazaqlearn.dto.CourseStudentResponse;
import kz.qazaqlearn.dto.EnrollmentResponse;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.CourseMapper;
import kz.qazaqlearn.mapper.EnrollmentMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.LessonRepository;
import kz.qazaqlearn.repository.ProgressRepository;
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
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final ProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final AuditLogService auditLogService;
    private final KafkaEventPublisher kafkaEventPublisher;

    public EnrollmentService(CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             LessonRepository lessonRepository,
                             AssignmentRepository assignmentRepository,
                             ProgressRepository progressRepository,
                             SubmissionRepository submissionRepository,
                             EnrollmentMapper enrollmentMapper,
                             CourseMapper courseMapper,
                             AuditLogService auditLogService,
                             KafkaEventPublisher kafkaEventPublisher) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.assignmentRepository = assignmentRepository;
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.courseMapper = courseMapper;
        this.auditLogService = auditLogService;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

     @PreAuthorize("hasRole('STUDENT')")
     public EnrollmentResponse enroll(UUID courseId, User currentUser) {
         if (currentUser.getRole() != Role.STUDENT) {
             throw new AccessDeniedException("Only students can enroll in courses");
         }
         Course course = courseRepository.findById(courseId)
                 .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
         if (!course.isPublished()) {
             throw new BadRequestException("Cannot enroll in unpublished course");
         }
         if (enrollmentRepository.existsByCourseAndStudent(course, currentUser)) {
             throw new BadRequestException("Student is already enrolled in this course");
         }
         Enrollment enrollment = new Enrollment();
         enrollment.setCourse(course);
         enrollment.setStudent(currentUser);
         enrollment.setStatus(EnrollmentStatus.ACTIVE);
         Enrollment saved = enrollmentRepository.save(enrollment);
         auditLogService.logAction(currentUser, "ENROLLMENT_CREATE", "Enrollment", saved.getId(), null);
         
         // Publish Kafka event
         CourseEnrollmentEvent event = new CourseEnrollmentEvent(
             saved.getId(),
             courseId,
             currentUser.getId(),
             LocalDateTime.now()
         );
         kafkaEventPublisher.publishCourseEnrollmentEvent(event);
         
         return enrollmentMapper.toDto(saved);
     }

    @PreAuthorize("hasRole('STUDENT')")
    public List<CourseResponse> getMyCourses(User currentUser) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only students can view own course enrollments");
        }
        return enrollmentRepository.findByStudent(currentUser).stream()
                .map(Enrollment::getCourse)
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<CourseStudentResponse> getCourseStudents(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view students for own courses");
        }
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot view enrolled students");
        }

        int totalLessonsCount = lessonRepository.findByCourseOrderByLessonOrderAsc(course).size();
        int totalAssignmentsCount = assignmentRepository.findByCourse(course).size();

        return enrollmentRepository.findByCourse(course).stream()
                .map(enrollment -> {
                    User student = enrollment.getStudent();
                    int completedLessonsCount = (int) progressRepository.findByCourseAndStudent(course, student).stream()
                            .filter(Progress::isCompleted)
                            .count();
                    int submittedAssignmentsCount = (int) submissionRepository.findByStudent(student).stream()
                            .filter(submission -> submission.getAssignment().getCourse().getId().equals(course.getId()))
                            .count();
                    int progressPercent = totalLessonsCount == 0 ? 0 : (int) Math.round(completedLessonsCount * 100.0 / totalLessonsCount);
                    return new CourseStudentResponse(
                            student.getId(),
                            student.getFullName(),
                            student.getEmail(),
                            enrollment.getEnrolledAt(),
                            completedLessonsCount,
                            totalLessonsCount,
                            submittedAssignmentsCount,
                            totalAssignmentsCount,
                            progressPercent
                    );
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('STUDENT')")
    public void withdraw(UUID courseId, User currentUser) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only students can withdraw enrollments");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Enrollment enrollment = enrollmentRepository.findByCourseAndStudent(course, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        auditLogService.logAction(currentUser, "ENROLLMENT_DELETE", "Enrollment", enrollment.getId(), null);
        enrollmentRepository.delete(enrollment);
    }
}

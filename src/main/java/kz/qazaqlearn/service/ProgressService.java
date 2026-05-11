package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Enrollment;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Progress;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.ProgressResponse;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.ProgressMapper;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.LessonRepository;
import kz.qazaqlearn.repository.ProgressRepository;
import kz.qazaqlearn.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ProgressMapper progressMapper;
    private final AuditLogService auditLogService;

    public ProgressService(LessonRepository lessonRepository,
                           CourseRepository courseRepository,
                           EnrollmentRepository enrollmentRepository,
                           ProgressRepository progressRepository,
                           UserRepository userRepository,
                           ProgressMapper progressMapper,
                           AuditLogService auditLogService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.progressMapper = progressMapper;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    public ProgressResponse completeLesson(UUID lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        Course course = lesson.getCourse();
        if (!enrollmentRepository.existsByCourseAndStudent(course, currentUser)) {
            throw new AccessDeniedException("Student must be enrolled to complete lessons");
        }
        Progress progress = progressRepository.findByCourseAndLessonAndStudent(course, lesson, currentUser)
                .orElseGet(() -> {
                    Progress entity = new Progress();
                    entity.setCourse(course);
                    entity.setLesson(lesson);
                    entity.setStudent(currentUser);
                    entity.setCompleted(false);
                    return entity;
                });
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        Progress saved = progressRepository.save(progress);
        auditLogService.logAction(currentUser, "PROGRESS_COMPLETE", "Progress", saved.getId(), null);
        return progressMapper.toDto(saved);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public List<ProgressResponse> getMyProgress(User currentUser) {
        return progressRepository.findByStudent(currentUser).stream()
                .map(progressMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<ProgressResponse> getCourseProgress(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view progress for own courses");
        }
        return progressRepository.findByCourse(course).stream()
                .map(progressMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<ProgressResponse> getStudentProgress(UUID courseId, UUID studentId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view student progress for own courses");
        }
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return progressRepository.findByCourseAndStudent(course, student).stream()
                .map(progressMapper::toDto)
                .collect(Collectors.toList());
    }
}

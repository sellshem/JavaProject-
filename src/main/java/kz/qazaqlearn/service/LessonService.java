package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.LessonCreateRequest;
import kz.qazaqlearn.dto.LessonResponse;
import kz.qazaqlearn.dto.LessonUpdateRequest;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.LessonMapper;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.LessonRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final AuditLogService auditLogService;

    public LessonService(CourseRepository courseRepository,
                         LessonRepository lessonRepository,
                         LessonMapper lessonMapper,
                         AuditLogService auditLogService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.lessonMapper = lessonMapper;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public LessonResponse createLesson(UUID courseId, LessonCreateRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot create lessons");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only create lessons for own courses");
        }
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitleKk(request.titleKk());
        lesson.setContentKk(request.contentKk());
        lesson.setLessonOrder(request.lessonOrder());
        Lesson saved = lessonRepository.save(lesson);
        auditLogService.logAction(currentUser, "LESSON_CREATE", "Lesson", saved.getId(), null);
        return lessonMapper.toDto(saved);
    }

    @PreAuthorize("isAuthenticated()")
    public List<LessonResponse> getLessons(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.STUDENT && !course.isPublished()) {
            throw new AccessDeniedException("Students can only view lessons for published courses");
        }
        return lessonRepository.findByCourseOrderByLessonOrderAsc(course).stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public LessonResponse getLessonById(UUID lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        if (currentUser.getRole() == Role.STUDENT && !lesson.getCourse().isPublished()) {
            throw new AccessDeniedException("Students can only view lessons for published courses");
        }
        return lessonMapper.toDto(lesson);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public LessonResponse updateLesson(UUID lessonId, LessonUpdateRequest request, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        Course course = lesson.getCourse();
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot update lessons");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only update lessons for own courses");
        }
        lesson.setTitleKk(request.titleKk());
        lesson.setContentKk(request.contentKk());
        lesson.setLessonOrder(request.lessonOrder());
        Lesson saved = lessonRepository.save(lesson);
        auditLogService.logAction(currentUser, "LESSON_UPDATE", "Lesson", saved.getId(), null);
        return lessonMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public void deleteLesson(UUID lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        Course course = lesson.getCourse();
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot delete lessons");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only delete lessons for own courses");
        }
        auditLogService.logAction(currentUser, "LESSON_DELETE", "Lesson", lesson.getId(), null);
        lessonRepository.delete(lesson);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public LessonResponse publishLesson(UUID lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        Course course = lesson.getCourse();
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot publish lessons");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only publish lessons for own courses");
        }
        lesson.setPublished(true);
        Lesson saved = lessonRepository.save(lesson);
        auditLogService.logAction(currentUser, "LESSON_PUBLISH", "Lesson", saved.getId(), null);
        return lessonMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public LessonResponse unpublishLesson(UUID lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        Course course = lesson.getCourse();
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot unpublish lessons");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only unpublish lessons for own courses");
        }
        lesson.setPublished(false);
        Lesson saved = lessonRepository.save(lesson);
        auditLogService.logAction(currentUser, "LESSON_UNPUBLISH", "Lesson", saved.getId(), null);
        return lessonMapper.toDto(saved);
    }
}

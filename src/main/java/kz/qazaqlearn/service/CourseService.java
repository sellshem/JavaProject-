package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.CourseCreateRequest;
import kz.qazaqlearn.dto.CourseResponse;
import kz.qazaqlearn.dto.CourseUpdateRequest;
import kz.qazaqlearn.domain.events.CourseCreatedEvent;
import kz.qazaqlearn.domain.events.CoursePublishedEvent;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.CourseMapper;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.service.AuditLogService;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final AuditLogService auditLogService;
    private final KafkaEventPublisher kafkaEventPublisher;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper, AuditLogService auditLogService, KafkaEventPublisher kafkaEventPublisher) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.auditLogService = auditLogService;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @CacheEvict(value = "publishedCourses", allEntries = true)
    public CourseResponse createCourse(CourseCreateRequest request, User teacher) {
        if (teacher.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot create courses");
        }
        Course course = new Course();
        course.setTitleKk(request.titleKk());
        course.setDescriptionKk(request.descriptionKk());
        course.setTeacher(teacher);
        course = courseRepository.save(course);
        auditLogService.logAction(teacher, "COURSE_CREATE", "Course", course.getId(), null);
        
        CourseResponse response = courseMapper.toDto(course);
        
        // Publish Kafka event
        CourseCreatedEvent event = new CourseCreatedEvent(
            course.getId(),
            teacher.getId(),
            course.getTitleKk(),
            LocalDateTime.now()
        );
        kafkaEventPublisher.publishCourseCreatedEvent(event);
        
        return response;
    }

    public List<CourseResponse> getCourses(User currentUser) {
        // Defensive: currentUser may be null in some call paths; treat as STUDENT (public view)
        if (currentUser == null || currentUser.getRole() == Role.STUDENT) {
            logger.info("Loading published courses for public/student");
            return getPublishedCoursesCached();
        }

        logger.info("Loading all courses from database for teacher/admin");
        return courseRepository.findAll().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    // Cache only the public list of published courses. Safe: no SpEL on possibly-null parameters.
    @Cacheable(value = "publishedCourses", key = "'publishedCourses'")
    public List<CourseResponse> getPublishedCoursesCached() {
        logger.info("Loading published courses from database (cached)");
        return courseRepository.findByPublishedTrue().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public CourseResponse getCourseById(UUID id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Check access permission based on role
        boolean hasAccess = false;
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin can view any course
            hasAccess = true;
        } else if (currentUser.getRole() == Role.TEACHER) {
            // Teacher can view own courses (published or draft)
            if (course.getTeacher() != null && course.getTeacher().getId() != null) {
                hasAccess = course.getTeacher().getId().equals(currentUser.getId());
            }
        } else if (currentUser.getRole() == Role.STUDENT) {
            // Student can only view published courses
            hasAccess = course.isPublished();
        }
        
        if (!hasAccess) {
            throw new AccessDeniedException("You do not have permission to view this course");
        }
        
        return courseMapper.toDto(course);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @CacheEvict(value = "publishedCourses", allEntries = true)
    public CourseResponse updateCourse(UUID id, CourseUpdateRequest request, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only update own courses");
        }
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot update courses");
        }
        course.setTitleKk(request.titleKk());
        course.setDescriptionKk(request.descriptionKk());
        Course saved = courseRepository.save(course);
        auditLogService.logAction(currentUser, "COURSE_UPDATE", "Course", saved.getId(), null);
        return courseMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @CacheEvict(value = "publishedCourses", allEntries = true)
    public void deleteCourse(UUID id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only delete own courses");
        }
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot delete courses");
        }
        auditLogService.logAction(currentUser, "COURSE_DELETE", "Course", course.getId(), null);
        courseRepository.delete(course);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @CacheEvict(value = "publishedCourses", allEntries = true)
    public CourseResponse publishCourse(UUID id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        ensureCourseWriteAccess(course, currentUser);
        course.setPublished(true);
        Course saved = courseRepository.save(course);
        try {
            auditLogService.logAction(currentUser, "COURSE_PUBLISH", "Course", saved.getId(), null);
        } catch (Exception e) {
            // Audit logging should not break the main request
        }
        
        // Publish Kafka event
        CoursePublishedEvent event = new CoursePublishedEvent(
            saved.getId(),
            currentUser.getId(),
            true,
            LocalDateTime.now()
        );
        kafkaEventPublisher.publishCoursePublishedEvent(event);
        
        return courseMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @CacheEvict(value = "publishedCourses", allEntries = true)
    public CourseResponse unpublishCourse(UUID id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        ensureCourseWriteAccess(course, currentUser);
        course.setPublished(false);
        Course saved = courseRepository.save(course);
        try {
            auditLogService.logAction(currentUser, "COURSE_UNPUBLISH", "Course", saved.getId(), null);
        } catch (Exception e) {
            // Audit logging should not break the main request
        }
        
        // Publish Kafka event
        CoursePublishedEvent event = new CoursePublishedEvent(
            saved.getId(),
            currentUser.getId(),
            false,
            LocalDateTime.now()
        );
        kafkaEventPublisher.publishCoursePublishedEvent(event);
        
        return courseMapper.toDto(saved);
    }

    private void ensureCourseWriteAccess(Course course, User currentUser) {
        if (currentUser.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("Student cannot manage courses");
        }
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only manage own courses");
        }
    }
}

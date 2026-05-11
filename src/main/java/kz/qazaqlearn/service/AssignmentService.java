package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Assignment;
import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.AssignmentDto;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.AssignmentMapper;
import kz.qazaqlearn.repository.AssignmentRepository;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.EnrollmentRepository;
import kz.qazaqlearn.repository.LessonRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentMapper assignmentMapper;
    private final AuditLogService auditLogService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             LessonRepository lessonRepository,
                             AssignmentMapper assignmentMapper,
                             AuditLogService auditLogService) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.assignmentMapper = assignmentMapper;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public AssignmentDto.AssignmentResponse createAssignment(AssignmentDto.AssignmentRequest request, User currentUser) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only create assignments for own courses");
        }
        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setCourse(course);
        if (request.lessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.lessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
            if (!lesson.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("Lesson does not belong to the course");
            }
            assignment.setLesson(lesson);
        }
        Assignment saved = assignmentRepository.save(assignment);
        auditLogService.logAction(currentUser, "ASSIGNMENT_CREATE", "Assignment", saved.getId(), null);
        return assignmentMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public List<AssignmentDto.AssignmentResponse> getAssignmentsByCourse(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (currentUser.getRole() == Role.STUDENT) {
            // Check if enrolled
            boolean enrolled = enrollmentRepository.existsByCourseAndStudent(course, currentUser);
            if (!enrolled) {
                throw new AccessDeniedException("Student must be enrolled in the course");
            }
        } else if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view assignments for own courses");
        }
        return assignmentRepository.findByCourse(course).stream()
                .map(assignmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public AssignmentDto.AssignmentResponse getAssignment(UUID assignmentId, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        Course course = assignment.getCourse();
        if (currentUser.getRole() == Role.STUDENT) {
            boolean enrolled = enrollmentRepository.existsByCourseAndStudent(course, currentUser);
            if (!enrolled) {
                throw new AccessDeniedException("Student must be enrolled in the course");
            }
        } else if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only view assignments for own courses");
        }
        return assignmentMapper.toDto(assignment);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public AssignmentDto.AssignmentResponse updateAssignment(UUID assignmentId, AssignmentDto.AssignmentRequest request, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        Course course = assignment.getCourse();
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only update assignments for own courses");
        }
        assignment.setTitleKk(request.titleKk());
        assignment.setDescriptionKk(request.descriptionKk());
        assignment.setDeadline(request.deadline());
        if (request.lessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.lessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
            if (!lesson.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("Lesson does not belong to the course");
            }
            assignment.setLesson(lesson);
        } else {
            assignment.setLesson(null);
        }
        Assignment saved = assignmentRepository.save(assignment);
        auditLogService.logAction(currentUser, "ASSIGNMENT_UPDATE", "Assignment", saved.getId(), null);
        return assignmentMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public void deleteAssignment(UUID assignmentId, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        Course course = assignment.getCourse();
        if (currentUser.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Teacher can only delete assignments for own courses");
        }
        auditLogService.logAction(currentUser, "ASSIGNMENT_DELETE", "Assignment", assignment.getId(), null);
        assignmentRepository.delete(assignment);
    }
}
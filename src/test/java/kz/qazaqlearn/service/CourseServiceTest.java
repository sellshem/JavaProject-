package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.CourseCreateRequest;
import kz.qazaqlearn.dto.CourseResponse;
import kz.qazaqlearn.exception.ResourceNotFoundException;
import kz.qazaqlearn.mapper.CourseMapper;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.service.AuditLogService;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, courseMapper, auditLogService, kafkaEventPublisher);
    }

    @Test
    void createCourseShouldSaveAndReturnCourse() {
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        CourseCreateRequest request = new CourseCreateRequest("Title","Description");
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.createCourse(request, teacher);

        assertThat(response.titleKk()).isEqualTo("Title");
        assertThat(response.descriptionKk()).isEqualTo("Description");
    }

    @Test
    void getCoursesShouldReturnOnlyPublishedForStudent() {
        User student = new User();
        student.setRole(Role.STUDENT);
        Course published = new Course();
        published.setId(UUID.randomUUID());
        published.setPublished(true);
        Course hidden = new Course();
        hidden.setId(UUID.randomUUID());
        hidden.setPublished(false);
        when(courseRepository.findByPublishedTrue()).thenReturn(List.of(published));

        List<CourseResponse> responses = courseService.getCourses(student);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(published.getId());
    }

    @Test
    void publishCourseShouldPublishAndReturnCourse() {
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);
        course.setPublished(false);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.publishCourse(course.getId(), teacher);

        assertThat(response.id()).isEqualTo(course.getId());
        assertThat(course.isPublished()).isTrue();
    }

    @Test
    void getCourseByIdShouldReturnCourseForAdmin() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
        
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);
        course.setTitleKk("Test Course");
        course.setPublished(false);
        
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(course.getId(), admin);

        assertThat(response.id()).isEqualTo(course.getId());
        assertThat(response.published()).isFalse();
    }

    @Test
    void getCourseByIdShouldReturnCourseForTeacherOwner() {
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);
        course.setTitleKk("Test Course");
        course.setPublished(false);
        
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(course.getId(), teacher);

        assertThat(response.id()).isEqualTo(course.getId());
    }

    @Test
    void getCourseByIdShouldReturnCourseForStudentIfPublished() {
        User student = new User();
        student.setId(UUID.randomUUID());
        student.setRole(Role.STUDENT);
        
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);
        course.setTitleKk("Test Course");
        course.setPublished(true);
        
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(course.getId(), student);

        assertThat(response.id()).isEqualTo(course.getId());
        assertThat(response.published()).isTrue();
    }

    @Test
    void getCourseByIdShouldThrowAccessDeniedForStudentIfNotPublished() {
        User student = new User();
        student.setId(UUID.randomUUID());
        student.setRole(Role.STUDENT);
        
        User teacher = new User();
        teacher.setId(UUID.randomUUID());
        teacher.setRole(Role.TEACHER);
        
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher);
        course.setPublished(false);
        
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.getCourseById(course.getId(), student))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getCourseByIdShouldThrowAccessDeniedForTeacherNotOwner() {
        User teacher1 = new User();
        teacher1.setId(UUID.randomUUID());
        teacher1.setRole(Role.TEACHER);
        
        User teacher2 = new User();
        teacher2.setId(UUID.randomUUID());
        teacher2.setRole(Role.TEACHER);
        
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTeacher(teacher1);
        course.setPublished(false);
        
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.getCourseById(course.getId(), teacher2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getCourseByIdShouldThrowNotFoundForNonExistentCourse() {
        UUID courseId = UUID.randomUUID();
        User student = new User();
        student.setRole(Role.STUDENT);
        
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(courseId, student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

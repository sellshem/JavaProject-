package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.LessonCreateRequest;
import kz.qazaqlearn.mapper.LessonMapper;
import kz.qazaqlearn.repository.CourseRepository;
import kz.qazaqlearn.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private AuditLogService auditLogService;

    private LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);

    @InjectMocks
    private LessonService lessonService;

    @BeforeEach
    void setUp() {
        lessonService = new LessonService(courseRepository, lessonRepository, lessonMapper, auditLogService);
    }

    @Test
    void createLessonAsTeacherShouldReturnLessonResponse() {
        UUID courseId = UUID.randomUUID();
        Course course = new Course();
        course.setId(courseId);
        course.setTeacher(new User(UUID.randomUUID(), null, null, null, Role.TEACHER, null, null));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonCreateRequest request = new LessonCreateRequest("Lesson","Content",1);
        User teacher = new User();
        teacher.setId(course.getTeacher().getId());
        teacher.setRole(Role.TEACHER);

        var response = lessonService.createLesson(courseId, request, teacher);

        assertThat(response.titleKk()).isEqualTo("Lesson");
        assertThat(response.lessonOrder()).isEqualTo(1);
    }
}

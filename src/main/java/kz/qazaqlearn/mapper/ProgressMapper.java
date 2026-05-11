package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Progress;
import kz.qazaqlearn.dto.ProgressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "lesson.id", target = "lessonId")
    ProgressResponse toDto(Progress progress);
}

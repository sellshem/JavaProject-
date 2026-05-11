package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.dto.LessonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(source = "course.id", target = "courseId")
    LessonResponse toDto(Lesson lesson);
}

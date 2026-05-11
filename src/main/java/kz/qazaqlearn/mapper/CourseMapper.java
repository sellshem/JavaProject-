package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.dto.CourseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher.fullName", target = "teacherName")
    CourseResponse toDto(Course course);
}

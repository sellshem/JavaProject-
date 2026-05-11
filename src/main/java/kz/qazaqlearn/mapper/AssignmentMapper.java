package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Assignment;
import kz.qazaqlearn.dto.AssignmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentDto.AssignmentResponse toDto(Assignment assignment);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assignment toEntity(AssignmentDto.AssignmentRequest request);
}
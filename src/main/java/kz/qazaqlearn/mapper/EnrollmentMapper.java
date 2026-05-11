package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Enrollment;
import kz.qazaqlearn.dto.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    EnrollmentResponse toDto(Enrollment enrollment);
}

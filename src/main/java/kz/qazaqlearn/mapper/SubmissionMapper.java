package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.Submission;
import kz.qazaqlearn.dto.SubmissionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "assignmentTitle", expression = "java(submission.getAssignment().getTitleKk())")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentFullName", expression = "java(submission.getStudent().getFullName())")
    @Mapping(target = "studentEmail", expression = "java(submission.getStudent().getEmail())")
    SubmissionDto.SubmissionResponse toDto(Submission submission);

    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "feedbackKk", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "gradedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Submission toEntity(SubmissionDto.SubmissionRequest request);
}
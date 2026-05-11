package kz.qazaqlearn.dto;

import kz.qazaqlearn.domain.SubmissionStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class SubmissionDto {

    public record SubmissionRequest(
            @NotBlank String answerText
    ) {}

    public record SubmissionResponse(
            UUID id,
            UUID assignmentId,
            String assignmentTitle,
            UUID studentId,
            String studentFullName,
            String studentEmail,
            String answerText,
            Integer grade,
            String feedbackKk,
            LocalDateTime submittedAt,
            LocalDateTime gradedAt,
            SubmissionStatus status
    ) {}

    public record GradeRequest(
            @NotNull Integer grade,
            String feedbackKk
    ) {}
}
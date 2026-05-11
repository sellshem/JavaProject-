package kz.qazaqlearn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssignmentDto {

    public record AssignmentRequest(
            @NotNull UUID courseId,
            UUID lessonId,
            @NotBlank String titleKk,
            @NotBlank String descriptionKk,
            LocalDateTime deadline
    ) {}

    public record AssignmentResponse(
            UUID id,
            UUID courseId,
            UUID lessonId,
            String titleKk,
            String descriptionKk,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
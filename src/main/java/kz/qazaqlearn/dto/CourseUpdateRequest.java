package kz.qazaqlearn.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseUpdateRequest(
        @NotBlank String titleKk,
        @NotBlank String descriptionKk
) {
}

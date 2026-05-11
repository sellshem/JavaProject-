package kz.qazaqlearn.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseCreateRequest(
        @NotBlank String titleKk,
        @NotBlank String descriptionKk
) {
}

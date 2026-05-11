package kz.qazaqlearn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonCreateRequest(
        @NotBlank String titleKk,
        @NotBlank String contentKk,
        @NotNull @Min(1) Integer lessonOrder
) {
}

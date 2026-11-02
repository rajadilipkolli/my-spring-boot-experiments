package com.example.mongoes.model.request;

import com.example.mongoes.document.Grades;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public record GradesRequest(
        @NotBlank(message = "Grade cannot be blank")
                @Pattern(regexp = "^[A-F]$", message = "Grade must be between A and F")
                String grade,
        @NotNull(message = "Date cannot be null") LocalDateTime date,
        @NotNull(message = "Score cannot be null")
                @Min(value = 0, message = "Score must be positive")
                @Max(value = 100, message = "Score cannot exceed 100")
                Integer score) {

    public Grades toGrade() {
        return new Grades(grade, date, score);
    }
}

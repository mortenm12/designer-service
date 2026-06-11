package dk.tinker.designer.api.dto;

import dk.tinker.model.Questionnaire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSurveyRequest(
        @NotBlank @Size(max = 500) String title,
        @NotNull Questionnaire structure
) { }

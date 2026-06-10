package dk.tinker.designer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSurveyRequest(
        @NotBlank @Size(max = 500) String title,
        List<String> supportedLocales
) { }

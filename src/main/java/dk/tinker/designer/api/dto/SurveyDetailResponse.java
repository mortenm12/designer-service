package dk.tinker.designer.api.dto;

import dk.tinker.designer.domain.SurveyStatus;
import dk.tinker.model.Questionnaire;

import java.time.Instant;
import java.util.UUID;

public record SurveyDetailResponse(
        UUID id,
        String title,
        SurveyStatus status,
        Instant createdAt,
        Instant updatedAt,
        Questionnaire structure
) { }

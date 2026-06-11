package dk.tinker.designer.api.dto;

import dk.tinker.designer.domain.SurveyStatus;

import java.time.Instant;
import java.util.UUID;

public record SurveyListItemResponse(
        UUID id,
        String title,
        SurveyStatus status,
        Instant createdAt,
        Instant updatedAt
) { }

package dk.tinker.designer.api.dto;

import dk.tinker.designer.domain.SurveyStatus;
import jakarta.validation.constraints.NotNull;

public record StatusTransitionRequest(@NotNull SurveyStatus status) { }

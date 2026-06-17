package dk.tinker.designer.api.dto;

import dk.tinker.permissionlib.model.PermissionLevel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantSurveyPermissionRequest(
        @NotNull UUID userId,
        @NotNull PermissionLevel level
) {}

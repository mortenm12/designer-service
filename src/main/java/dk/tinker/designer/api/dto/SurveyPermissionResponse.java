package dk.tinker.designer.api.dto;

import dk.tinker.permissionlib.model.PermissionLevel;

import java.time.Instant;
import java.util.UUID;

public record SurveyPermissionResponse(
        UUID id,
        UUID userId,
        PermissionLevel level,
        UUID grantedBy,
        Instant createdAt
) {}

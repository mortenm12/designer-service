package dk.tinker.designer.client.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TokenValidationResult(
        boolean valid,
        String keyHash,
        UUID userId,
        UUID orgId,
        List<String> scopes,
        Instant expiresAt
) { }

package dk.tinker.designer.client;

import dk.tinker.designer.api.dto.SurveyPermissionResponse;
import dk.tinker.permissionlib.config.PermissionLibProperties;
import dk.tinker.permissionlib.model.PermissionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthServicePermissionClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServicePermissionClient.class);
    private static final String RESOURCE_TYPE = "survey";

    private final RestClient restClient;

    public AuthServicePermissionClient(PermissionLibProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getAuthServiceUrl())
                .defaultHeader("Authorization", "ApiKey " + properties.getServiceApiKey())
                .build();
    }

    public SurveyPermissionResponse grant(UUID surveyId, UUID userId, PermissionLevel level, UUID grantedBy) {
        Map<String, Object> body = Map.of(
                "userId", userId,
                "resourceType", RESOURCE_TYPE,
                "resourceId", surveyId.toString(),
                "level", level.name(),
                "grantedBy", grantedBy
        );
        var raw = restClient.post()
                .uri("/api/v1/resource-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        return mapResponse(raw);
    }

    public void revoke(UUID permissionId) {
        restClient.delete()
                .uri("/api/v1/resource-permissions/{id}", permissionId)
                .retrieve()
                .toBodilessEntity();
    }

    public List<SurveyPermissionResponse> list(UUID surveyId) {
        try {
            var raw = restClient.get()
                    .uri("/api/v1/resource-permissions?resourceType={t}&resourceId={r}",
                            RESOURCE_TYPE, surveyId.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            if (raw == null) return List.of();
            return raw.stream().map(this::mapResponse).toList();
        } catch (RestClientException e) {
            log.warn("Failed to list permissions for survey {}: {}", surveyId, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private SurveyPermissionResponse mapResponse(Map<String, Object> raw) {
        return new SurveyPermissionResponse(
                UUID.fromString((String) raw.get("id")),
                UUID.fromString((String) raw.get("userId")),
                PermissionLevel.valueOf((String) raw.get("level")),
                UUID.fromString((String) raw.get("grantedBy")),
                Instant.parse((String) raw.get("createdAt"))
        );
    }
}

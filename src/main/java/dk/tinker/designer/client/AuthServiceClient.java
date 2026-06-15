package dk.tinker.designer.client;

import dk.tinker.designer.client.dto.TokenValidationResult;
import dk.tinker.designer.client.dto.ValidateApiKeyRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private static final String VALIDATE_PATH = "/internal/api-keys/validate";
    private static final String SECRET_HEADER = "X-Internal-Secret";

    private final RestClient restClient;
    private final String internalSecret;

    public AuthServiceClient(
            @Value("${auth.service.url}") String authServiceUrl,
            @Value("${auth.service.internal-secret}") String internalSecret) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
        this.internalSecret = internalSecret;
    }

    public TokenValidationResult validateApiKey(String rawKey) {
        return restClient.post()
                .uri(VALIDATE_PATH)
                .header(SECRET_HEADER, internalSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ValidateApiKeyRequest(rawKey))
                .retrieve()
                .body(TokenValidationResult.class);
    }
}

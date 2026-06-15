package dk.tinker.designer.service;

import dk.tinker.designer.client.AuthServiceClient;
import dk.tinker.designer.client.dto.TokenValidationResult;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class TokenValidationService {

    private final AuthServiceClient authServiceClient;

    public TokenValidationService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Cacheable(value = "apiKeyValidation", key = "#keyHash")
    public TokenValidationResult validateByHash(String rawKey, String keyHash) {
        return authServiceClient.validateApiKey(rawKey);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}

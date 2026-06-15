package dk.tinker.designer.security;

import dk.tinker.designer.client.dto.TokenValidationResult;
import dk.tinker.designer.service.TokenValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_PREFIX = "ApiKey ";

    private final TokenValidationService tokenValidationService;

    public ApiKeyAuthFilter(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(API_KEY_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        String rawKey = header.substring(API_KEY_PREFIX.length()).trim();
        String keyHash = TokenValidationService.sha256Hex(rawKey);
        TokenValidationResult result;
        try {
            result = tokenValidationService.validateByHash(rawKey, keyHash);
        } catch (RuntimeException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to validate token");
            return;
        }
        if (result == null || !result.valid()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired API key");
            return;
        }
        List<GrantedAuthority> authorities = result.scopes().stream()
                .map(scope -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + scope.trim()))
                .toList();
        String principalId = result.userId() != null ? result.userId().toString() : keyHash;
        SecurityContextHolder.getContext().setAuthentication(
                new ApiKeyAuthenticationToken(principalId, authorities)
        );
        chain.doFilter(request, response);
    }
}

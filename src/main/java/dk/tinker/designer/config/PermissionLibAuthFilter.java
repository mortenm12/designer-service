package dk.tinker.designer.config;

import dk.tinker.permissionlib.model.TokenType;
import dk.tinker.permissionlib.model.ValidationResult;
import dk.tinker.permissionlib.service.TokenValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PermissionLibAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_KEY_PREFIX = "ApiKey ";

    private final TokenValidationService tokenValidationService;

    public PermissionLibAuthFilter(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            if (header.startsWith(BEARER_PREFIX)) {
                authenticate(header.substring(BEARER_PREFIX.length()).trim(), TokenType.JWT);
            } else if (header.startsWith(API_KEY_PREFIX)) {
                authenticate(header.substring(API_KEY_PREFIX.length()).trim(), TokenType.API_KEY);
            }
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token, TokenType tokenType) {
        ValidationResult result = tokenValidationService.validate(token, tokenType);
        if (result.valid()) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            result.roles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
            result.scopes().forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
            var auth = new UsernamePasswordAuthenticationToken(result.subject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}

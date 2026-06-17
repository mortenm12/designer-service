package dk.tinker.designer.api;

import dk.tinker.designer.api.dto.GrantSurveyPermissionRequest;
import dk.tinker.designer.api.dto.SurveyPermissionResponse;
import dk.tinker.designer.service.SurveyPermissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/surveys/{surveyId}/permissions")
public class SurveyPermissionController {

    private final SurveyPermissionService permissionService;

    public SurveyPermissionController(SurveyPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public SurveyPermissionResponse grant(
            Authentication auth,
            @PathVariable UUID surveyId,
            @Valid @RequestBody GrantSurveyPermissionRequest request) {
        return permissionService.grantPermission(auth, surveyId, request);
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> revoke(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID permissionId) {
        permissionService.revokePermission(auth, surveyId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<SurveyPermissionResponse> list(
            Authentication auth,
            @PathVariable UUID surveyId) {
        return permissionService.listPermissions(auth, surveyId);
    }
}

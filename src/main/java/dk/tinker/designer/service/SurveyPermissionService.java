package dk.tinker.designer.service;

import dk.tinker.designer.api.dto.GrantSurveyPermissionRequest;
import dk.tinker.designer.api.dto.SurveyPermissionResponse;
import dk.tinker.designer.client.AuthServicePermissionClient;
import dk.tinker.designer.domain.SurveyDefinition;
import dk.tinker.designer.exception.ResourceNotFoundException;
import dk.tinker.designer.repository.SurveyDefinitionRepository;
import dk.tinker.permissionlib.model.PermissionLevel;
import dk.tinker.permissionlib.service.ResourcePermissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SurveyPermissionService {

    private final SurveyDefinitionRepository repository;
    private final ResourcePermissionService resourcePermissionService;
    private final AuthServicePermissionClient permissionClient;

    public SurveyPermissionService(SurveyDefinitionRepository repository,
            ResourcePermissionService resourcePermissionService,
            AuthServicePermissionClient permissionClient) {
        this.repository = repository;
        this.resourcePermissionService = resourcePermissionService;
        this.permissionClient = permissionClient;
    }

    public SurveyPermissionResponse grantPermission(Authentication auth, UUID surveyId,
            GrantSurveyPermissionRequest request) {
        SurveyDefinition survey = findSurveyForAdmin(auth, surveyId);
        boolean isOwner = survey.getOwnerId().equals(auth.getName());
        if (!isOwner && request.level() == PermissionLevel.ADMIN) {
            throw new IllegalArgumentException("Only the owner can grant ADMIN permission");
        }
        UUID grantedBy = UUID.fromString(auth.getName());
        return permissionClient.grant(surveyId, request.userId(), request.level(), grantedBy);
    }

    public void revokePermission(Authentication auth, UUID surveyId, UUID permissionId) {
        findSurveyForAdmin(auth, surveyId);
        permissionClient.revoke(permissionId);
    }

    public List<SurveyPermissionResponse> listPermissions(Authentication auth, UUID surveyId) {
        findSurveyForAdmin(auth, surveyId);
        return permissionClient.list(surveyId);
    }

    private SurveyDefinition findSurveyForAdmin(Authentication auth, UUID surveyId) {
        String subject = auth.getName();
        SurveyDefinition survey = repository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));
        if (survey.getOwnerId().equals(subject)) {
            return survey;
        }
        if (resourcePermissionService.hasPermission(subject, "survey", surveyId.toString(), PermissionLevel.ADMIN)) {
            return survey;
        }
        throw new ResourceNotFoundException("Survey", surveyId);
    }
}

package dk.tinker.designer.service;

import dk.tinker.designer.api.dto.CreateSurveyRequest;
import dk.tinker.designer.api.dto.StatusTransitionRequest;
import dk.tinker.designer.api.dto.SurveyDetailResponse;
import dk.tinker.designer.domain.SurveyDefinition;
import dk.tinker.designer.domain.SurveyStatus;
import dk.tinker.designer.exception.ResourceNotFoundException;
import dk.tinker.designer.repository.SurveyDefinitionRepository;
import dk.tinker.util.QuistionnaireSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyDesignServiceTest {

    @Mock
    private SurveyDefinitionRepository repository;

    @Mock
    private Authentication auth;

    private SurveyDesignService service;

    @BeforeEach
    void setUp() {
        service = new SurveyDesignService(repository);
        when(auth.getName()).thenReturn("user-123");
    }

    @Test
    void createSurvey_persistsAndReturnsDetail() {
        CreateSurveyRequest request = new CreateSurveyRequest("My Survey", List.of("en"));
        SurveyDefinition saved = new SurveyDefinition(
                "user-123", "My Survey", QuistionnaireSerializer.serialize(new dk.tinker.model.Questionnaire()));
        when(repository.save(any())).thenReturn(saved);

        SurveyDetailResponse response = service.createSurvey(auth, request);

        assertNotNull(response);
        assertEquals("My Survey", response.title());
        assertEquals(SurveyStatus.DRAFT, response.status());
        assertNotNull(response.structure());
    }

    @Test
    void transitionStatus_draftToPublished_succeeds() {
        SurveyDefinition definition = new SurveyDefinition(
                "user-123", "Survey", QuistionnaireSerializer.serialize(new dk.tinker.model.Questionnaire()));
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, "user-123")).thenReturn(Optional.of(definition));
        when(repository.save(any())).thenReturn(definition);

        service.transitionStatus(auth, id, new StatusTransitionRequest(SurveyStatus.PUBLISHED));

        assertEquals(SurveyStatus.PUBLISHED, definition.getStatus());
    }

    @Test
    void transitionStatus_draftToArchived_throws() {
        SurveyDefinition definition = new SurveyDefinition(
                "user-123", "Survey", QuistionnaireSerializer.serialize(new dk.tinker.model.Questionnaire()));
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, "user-123")).thenReturn(Optional.of(definition));

        assertThrows(IllegalStateException.class, () ->
                service.transitionStatus(auth, id, new StatusTransitionRequest(SurveyStatus.ARCHIVED)));
    }

    @Test
    void getSurvey_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, "user-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSurvey(auth, id));
    }

    @Test
    void deleteSurvey_nonDraft_throws() {
        SurveyDefinition definition = new SurveyDefinition(
                "user-123", "Survey", QuistionnaireSerializer.serialize(new dk.tinker.model.Questionnaire()));
        definition.transitionStatus(SurveyStatus.PUBLISHED);
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, "user-123")).thenReturn(Optional.of(definition));

        assertThrows(IllegalStateException.class, () -> service.deleteSurvey(auth, id));
    }

    @Test
    void addPage_appendsPageToQuestionnaire() {
        dk.tinker.model.Questionnaire questionnaire = new dk.tinker.model.Questionnaire();
        SurveyDefinition definition = new SurveyDefinition(
                "user-123", "Survey", QuistionnaireSerializer.serialize(questionnaire));
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, "user-123")).thenReturn(Optional.of(definition));
        when(repository.save(any())).thenReturn(definition);

        SurveyDetailResponse response = service.addPage(auth, id);

        assertEquals(1, response.structure().getPages().size());
        verify(repository).save(definition);
    }
}

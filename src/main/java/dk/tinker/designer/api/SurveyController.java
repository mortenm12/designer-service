package dk.tinker.designer.api;

import dk.tinker.designer.api.dto.CreateSurveyRequest;
import dk.tinker.designer.api.dto.StatusTransitionRequest;
import dk.tinker.designer.api.dto.SurveyDetailResponse;
import dk.tinker.designer.api.dto.SurveyListItemResponse;
import dk.tinker.designer.api.dto.UpdateSurveyRequest;
import dk.tinker.designer.domain.SurveyStatus;
import dk.tinker.designer.service.SurveyDesignService;
import dk.tinker.model.Page;
import dk.tinker.model.PageElement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final SurveyDesignService service;

    public SurveyController(SurveyDesignService service) {
        this.service = service;
    }

    @GetMapping
    public org.springframework.data.domain.Page<SurveyListItemResponse> list(
            Authentication auth,
            @RequestParam(required = false) SurveyStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listSurveys(auth, status, pageable);
    }

    @PostMapping
    public ResponseEntity<SurveyDetailResponse> create(
            Authentication auth,
            @Valid @RequestBody CreateSurveyRequest request) {
        SurveyDetailResponse response = service.createSurvey(auth, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public SurveyDetailResponse get(Authentication auth, @PathVariable UUID id) {
        return service.getSurvey(auth, id);
    }

    @PutMapping("/{id}")
    public SurveyDetailResponse update(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSurveyRequest request) {
        return service.updateSurvey(auth, id, request);
    }

    @PatchMapping("/{id}/status")
    public SurveyDetailResponse transitionStatus(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody StatusTransitionRequest request) {
        return service.transitionStatus(auth, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        service.deleteSurvey(auth, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{surveyId}/pages")
    public SurveyDetailResponse addPage(Authentication auth, @PathVariable UUID surveyId) {
        return service.addPage(auth, surveyId);
    }

    @PutMapping("/{surveyId}/pages/{pageId}")
    public SurveyDetailResponse replacePage(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID pageId,
            @RequestBody Page page) {
        return service.replacePage(auth, surveyId, pageId, page);
    }

    @DeleteMapping("/{surveyId}/pages/{pageId}")
    public SurveyDetailResponse removePage(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID pageId) {
        return service.removePage(auth, surveyId, pageId);
    }

    @PostMapping("/{surveyId}/pages/{pageId}/elements")
    public SurveyDetailResponse addElement(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID pageId,
            @RequestBody PageElement element) {
        return service.addElement(auth, surveyId, pageId, element);
    }

    @PutMapping("/{surveyId}/pages/{pageId}/elements/{elementId}")
    public SurveyDetailResponse replaceElement(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID pageId,
            @PathVariable UUID elementId,
            @RequestBody PageElement element) {
        return service.replaceElement(auth, surveyId, pageId, elementId, element);
    }

    @DeleteMapping("/{surveyId}/pages/{pageId}/elements/{elementId}")
    public SurveyDetailResponse removeElement(
            Authentication auth,
            @PathVariable UUID surveyId,
            @PathVariable UUID pageId,
            @PathVariable UUID elementId) {
        return service.removeElement(auth, surveyId, pageId, elementId);
    }
}

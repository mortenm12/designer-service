package dk.tinker.designer.service;

import dk.tinker.designer.api.dto.CreateSurveyRequest;
import dk.tinker.designer.api.dto.StatusTransitionRequest;
import dk.tinker.designer.api.dto.SurveyDetailResponse;
import dk.tinker.designer.api.dto.SurveyListItemResponse;
import dk.tinker.designer.api.dto.UpdateSurveyRequest;
import dk.tinker.designer.domain.SurveyDefinition;
import dk.tinker.designer.domain.SurveyStatus;
import dk.tinker.designer.exception.ResourceNotFoundException;
import dk.tinker.designer.repository.SurveyDefinitionRepository;
import dk.tinker.model.Page;
import dk.tinker.model.PageElement;
import dk.tinker.model.Questionnaire;
import dk.tinker.util.QuistionnaireSerializer;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class SurveyDesignService {

    private final SurveyDefinitionRepository repository;

    public SurveyDesignService(SurveyDefinitionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SurveyListItemResponse> listSurveys(
            Authentication auth, SurveyStatus status, Pageable pageable) {
        String ownerId = auth.getName();
        org.springframework.data.domain.Page<SurveyDefinition> results = (status != null)
                ? repository.findByOwnerIdAndStatus(ownerId, status, pageable)
                : repository.findByOwnerId(ownerId, pageable);
        return results.map(this::toListItem);
    }

    public SurveyDetailResponse createSurvey(Authentication auth, CreateSurveyRequest request) {
        Questionnaire questionnaire = new Questionnaire();
        if (request.supportedLocales() != null) {
            request.supportedLocales().stream()
                    .map(Locale::forLanguageTag)
                    .forEach(questionnaire.getSupportedLocales()::add);
        }
        Document structure = toDocument(questionnaire);
        SurveyDefinition definition = new SurveyDefinition(auth.getName(), request.title(), structure);
        return toDetailResponse(repository.save(definition));
    }

    @Transactional(readOnly = true)
    public SurveyDetailResponse getSurvey(Authentication auth, UUID id) {
        return toDetailResponse(findOwned(auth, id));
    }

    public SurveyDetailResponse updateSurvey(Authentication auth, UUID id, UpdateSurveyRequest request) {
        SurveyDefinition definition = findOwned(auth, id);
        definition.update(request.title(), toDocument(request.structure()));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse transitionStatus(
            Authentication auth, UUID id, StatusTransitionRequest request) {
        SurveyDefinition definition = findOwned(auth, id);
        definition.transitionStatus(request.status());
        return toDetailResponse(repository.save(definition));
    }

    public void deleteSurvey(Authentication auth, UUID id) {
        SurveyDefinition definition = findOwned(auth, id);
        if (definition.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT surveys can be deleted");
        }
        repository.delete(definition);
    }

    public SurveyDetailResponse addPage(Authentication auth, UUID surveyId) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        questionnaire.addPage(new Page());
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse replacePage(Authentication auth, UUID surveyId, UUID pageId, Page page) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        List<Page> pages = questionnaire.getPages();
        int index = findPageIndex(pages, pageId);
        page.setId(pageId);
        pages.set(index, page);
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse removePage(Authentication auth, UUID surveyId, UUID pageId) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        List<Page> pages = questionnaire.getPages();
        int index = findPageIndex(pages, pageId);
        pages.remove(index);
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse addElement(
            Authentication auth, UUID surveyId, UUID pageId, PageElement element) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        Page page = findPage(questionnaire.getPages(), pageId);
        page.addPageElement(element);
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse replaceElement(
            Authentication auth, UUID surveyId, UUID pageId, UUID elementId, PageElement element) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        Page page = findPage(questionnaire.getPages(), pageId);
        List<PageElement> elements = page.getPageElements();
        int index = findElementIndex(elements, elementId);
        element.setId(elementId);
        elements.set(index, element);
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    public SurveyDetailResponse removeElement(
            Authentication auth, UUID surveyId, UUID pageId, UUID elementId) {
        SurveyDefinition definition = findOwned(auth, surveyId);
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        Page page = findPage(questionnaire.getPages(), pageId);
        List<PageElement> elements = page.getPageElements();
        int index = findElementIndex(elements, elementId);
        elements.remove(index);
        definition.updateStructure(toDocument(questionnaire));
        return toDetailResponse(repository.save(definition));
    }

    private SurveyDefinition findOwned(Authentication auth, UUID id) {
        return repository.findByIdAndOwnerId(id, auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Survey", id));
    }

    private Document toDocument(Questionnaire questionnaire) {
        return Document.parse(QuistionnaireSerializer.serialize(questionnaire));
    }

    private Questionnaire fromDocument(Document document) {
        return QuistionnaireSerializer.deserialize(document.toJson());
    }

    private int findPageIndex(List<Page> pages, UUID pageId) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId().equals(pageId)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("Page", pageId);
    }

    private Page findPage(List<Page> pages, UUID pageId) {
        return pages.stream()
                .filter(p -> p.getId().equals(pageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Page", pageId));
    }

    private int findElementIndex(List<PageElement> elements, UUID elementId) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getId().equals(elementId)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("Element", elementId);
    }

    private SurveyListItemResponse toListItem(SurveyDefinition definition) {
        return new SurveyListItemResponse(
                definition.getId(),
                definition.getTitle(),
                definition.getStatus(),
                definition.getCreatedAt(),
                definition.getUpdatedAt()
        );
    }

    private SurveyDetailResponse toDetailResponse(SurveyDefinition definition) {
        Questionnaire questionnaire = fromDocument(definition.getStructure());
        return new SurveyDetailResponse(
                definition.getId(),
                definition.getTitle(),
                definition.getStatus(),
                definition.getCreatedAt(),
                definition.getUpdatedAt(),
                questionnaire
        );
    }
}

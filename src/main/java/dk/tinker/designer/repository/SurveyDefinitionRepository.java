package dk.tinker.designer.repository;

import dk.tinker.designer.domain.SurveyDefinition;
import dk.tinker.designer.domain.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface SurveyDefinitionRepository extends MongoRepository<SurveyDefinition, UUID> {

    Page<SurveyDefinition> findByOwnerId(String ownerId, Pageable pageable);

    Page<SurveyDefinition> findByOwnerIdAndStatus(String ownerId, SurveyStatus status, Pageable pageable);

    Optional<SurveyDefinition> findByIdAndOwnerId(UUID id, String ownerId);
}

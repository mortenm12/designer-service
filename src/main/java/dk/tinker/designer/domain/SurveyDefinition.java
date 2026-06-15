package dk.tinker.designer.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.bson.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@org.springframework.data.mongodb.core.mapping.Document(collection = "survey_definitions")
public class SurveyDefinition {

    @Id
    private UUID id;

    @Indexed
    @Field("owner_id")
    private String ownerId;

    @NotBlank
    @Size(max = 500)
    @Field("title")
    private String title;

    @Indexed
    @Field("status")
    private SurveyStatus status;

    @Field("structure")
    private Document structure;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    protected SurveyDefinition() { }

    public SurveyDefinition(String ownerId, String title, Document structure) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.title = title;
        this.structure = structure;
        this.status = SurveyStatus.DRAFT;
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public Document getStructure() {
        return structure;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String title, Document structure) {
        this.title = title;
        this.structure = structure;
    }

    public void updateStructure(Document structure) {
        this.structure = structure;
    }

    public void transitionStatus(SurveyStatus newStatus) {
        if (status == SurveyStatus.DRAFT && newStatus == SurveyStatus.PUBLISHED) {
            this.status = newStatus;
        } else if (status == SurveyStatus.PUBLISHED && newStatus == SurveyStatus.ARCHIVED) {
            this.status = newStatus;
        } else {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus
            );
        }
    }
}

package dk.tinker.designer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "survey_definitions")
public class SurveyDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SurveyStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String structure;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected SurveyDefinition() { }

    public SurveyDefinition(String ownerId, String title, String structure) {
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

    public String getStructure() {
        return structure;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String title, String structure) {
        this.title = title;
        this.structure = structure;
    }

    public void updateStructure(String structure) {
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

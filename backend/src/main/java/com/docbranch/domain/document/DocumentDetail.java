package com.docbranch.domain.document;

import com.docbranch.domain.project.Project;
import com.docbranch.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "document_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_detail_id", nullable = false)
    private UUID documentDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_version_id")
    private DocumentVersion rootVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_version_id")
    private DocumentVersion finalVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    public static DocumentDetail create(
            Project project,
            String name,
            String description,
            User createdBy,
            OffsetDateTime now
    ) {
        DocumentDetail documentDetail = new DocumentDetail();
        documentDetail.project = project;
        documentDetail.name = name;
        documentDetail.description = description;
        documentDetail.status = DocumentStatus.DRAFT;
        documentDetail.createdBy = createdBy;
        documentDetail.createdAt = now;
        documentDetail.updatedAt = now;
        return documentDetail;
    }

    public void updateBasicInfo(String name, String description, OffsetDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.updatedAt = updatedAt;
    }
}

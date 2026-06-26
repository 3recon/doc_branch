package com.docbranch.domain.document;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "document_versions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_version_id", nullable = false)
    private UUID documentVersionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_detail_id", nullable = false)
    private DocumentDetail documentDetail;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_type", nullable = false, length = 30)
    private DocumentVersionType versionType = DocumentVersionType.REVISION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "change_note", columnDefinition = "TEXT")
    private String changeNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    public static DocumentVersion create(
            DocumentDetail documentDetail,
            Integer versionNumber,
            String title,
            String content,
            DocumentVersionType versionType,
            User createdBy,
            OffsetDateTime now
    ) {
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.documentDetail = documentDetail;
        documentVersion.versionNumber = versionNumber;
        documentVersion.title = title;
        documentVersion.content = content;
        documentVersion.versionType = versionType;
        documentVersion.status = DocumentStatus.DRAFT;
        documentVersion.uploadedBy = createdBy;
        documentVersion.lastModifiedBy = createdBy;
        documentVersion.createdAt = now;
        documentVersion.updatedAt = now;
        return documentVersion;
    }
}

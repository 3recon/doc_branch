package com.docbranch.domain.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "document_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_file_id", nullable = false)
    private UUID documentFileId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_key", nullable = false, columnDefinition = "TEXT")
    private String storageKey;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 30)
    private UploadStatus uploadStatus = UploadStatus.UPLOADING;

    @Column(name = "upload_error_code", length = 50)
    private String uploadErrorCode;

    @Column(name = "upload_error_message", columnDefinition = "TEXT")
    private String uploadErrorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

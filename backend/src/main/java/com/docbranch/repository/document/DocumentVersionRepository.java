package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    @Query("""
            select coalesce(max(documentVersion.versionNumber), 0)
            from DocumentVersion documentVersion
            where documentVersion.documentDetail.documentDetailId = :documentDetailId
              and documentVersion.deletedAt is null
            """)
    Integer findMaxVersionNumber(@Param("documentDetailId") UUID documentDetailId);

    List<DocumentVersion> findByDocumentDetailDocumentDetailIdAndDeletedAtIsNullOrderByVersionNumberAsc(
            UUID documentDetailId
    );

    Optional<DocumentVersion> findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
            UUID documentDetailId,
            UUID documentVersionId
    );
}

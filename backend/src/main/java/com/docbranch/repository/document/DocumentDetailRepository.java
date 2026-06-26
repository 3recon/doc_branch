package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentDetailRepository extends JpaRepository<DocumentDetail, UUID> {

    List<DocumentDetail> findByProjectProjectIdAndDeletedAtIsNullOrderByUpdatedAtDescNameAsc(UUID projectId);

    Optional<DocumentDetail> findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
            UUID projectId,
            UUID documentDetailId
    );
}

package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
}

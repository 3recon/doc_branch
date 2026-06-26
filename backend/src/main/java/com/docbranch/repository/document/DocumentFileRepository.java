package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, UUID> {
}

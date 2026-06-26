package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentDetailPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentDetailPermissionRepository extends JpaRepository<DocumentDetailPermission, UUID> {
}

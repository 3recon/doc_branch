package com.docbranch.repository.document;

import com.docbranch.domain.document.DocumentVersionRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentVersionRelationRepository extends JpaRepository<DocumentVersionRelation, UUID> {
}

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "document_version_relations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentVersionRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "relation_id", nullable = false)
    private UUID relationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_version_id", nullable = false)
    private DocumentVersion parentVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_version_id", nullable = false)
    private DocumentVersion childVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private RelationType relationType = RelationType.REVISION;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

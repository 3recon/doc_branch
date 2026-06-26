package com.docbranch.domain.trash;

import com.docbranch.domain.common.TargetType;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "trash_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrashItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "trash_item_id", nullable = false)
    private UUID trashItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deleted_by", nullable = false)
    private User deletedBy;

    @Column(name = "deleted_at", nullable = false)
    private OffsetDateTime deletedAt;

    @Column(name = "restored_at")
    private OffsetDateTime restoredAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}

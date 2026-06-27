package com.docbranch.repository.project;

import com.docbranch.domain.project.ProjectInvitation;
import com.docbranch.domain.project.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {

    List<ProjectInvitation> findByProjectProjectIdOrderByExpiresAtAsc(UUID projectId);

    Optional<ProjectInvitation> findByInvitationIdAndProjectProjectId(UUID invitationId, UUID projectId);

    Optional<ProjectInvitation> findByInvitationIdAndProjectProjectIdAndStatus(
            UUID invitationId,
            UUID projectId,
            InvitationStatus status
    );

    List<ProjectInvitation> findByProjectProjectIdAndStatusAndExpiresAtBefore(
            UUID projectId,
            InvitationStatus status,
            OffsetDateTime expiresAt
    );
}

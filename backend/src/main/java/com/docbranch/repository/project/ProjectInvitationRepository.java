package com.docbranch.repository.project;

import com.docbranch.domain.project.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {
}

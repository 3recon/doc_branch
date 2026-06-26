package com.docbranch.repository.project;

import com.docbranch.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByDeletedAtIsNullOrderByUpdatedAtDesc();

    Optional<Project> findByProjectIdAndDeletedAtIsNull(UUID projectId);
}

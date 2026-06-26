package com.docbranch.repository.project;

import com.docbranch.domain.project.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    List<ProjectMember> findByProjectProjectIdAndRemovedAtIsNullOrderByJoinedAtAsc(UUID projectId);

    Optional<ProjectMember> findByProjectMemberIdAndProjectProjectIdAndRemovedAtIsNull(
            UUID projectMemberId,
            UUID projectId
    );
}

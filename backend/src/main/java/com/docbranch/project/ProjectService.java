package com.docbranch.project;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.user.User;
import com.docbranch.repository.project.ProjectMemberRepository;
import com.docbranch.repository.project.ProjectRepository;
import com.docbranch.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
        User owner = userRepository.findById(request.ownerUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now();
        Project project = projectRepository.save(Project.create(
                request.name(),
                request.description(),
                owner,
                now
        ));
        projectMemberRepository.save(ProjectMember.createProjectAdmin(project, owner, now));

        return toDetailResponse(project);
    }

    public List<ProjectSummaryResponse> getProjects() {
        return projectRepository.findByDeletedAtIsNullOrderByUpdatedAtDesc()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public ProjectDetailResponse getProject(UUID projectId) {
        Project project = projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return toDetailResponse(project);
    }

    private ProjectSummaryResponse toSummaryResponse(Project project) {
        return new ProjectSummaryResponse(
                project.getProjectId(),
                project.getName(),
                project.getStatus().name(),
                project.getOwner().getName(),
                project.getUpdatedAt()
        );
    }

    private ProjectDetailResponse toDetailResponse(Project project) {
        return new ProjectDetailResponse(
                project.getProjectId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),
                project.getOwner().getName(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}

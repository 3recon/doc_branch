package com.docbranch.project;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.project.InvitationStatus;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectInvitation;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.user.User;
import com.docbranch.repository.project.ProjectInvitationRepository;
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
    private final ProjectInvitationRepository projectInvitationRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectInvitationRepository projectInvitationRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectInvitationRepository = projectInvitationRepository;
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

    @Transactional
    public ProjectDetailResponse updateProject(UUID projectId, ProjectUpdateRequest request) {
        Project project = projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        validateProjectAdmin(projectId, request.requesterUserId());
        project.updateBasicInfo(request.name(), request.description(), OffsetDateTime.now());

        return toDetailResponse(project);
    }

    @Transactional
    public void deleteProject(UUID projectId, ProjectDeleteRequest request) {
        Project project = projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        User deletedBy = userRepository.findById(request.deletedByUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateProjectAdmin(projectId, request.deletedByUserId());
        project.delete(deletedBy, OffsetDateTime.now());
    }

    public List<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        validateProjectExists(projectId);
        return projectMemberRepository.findByProjectProjectIdAndRemovedAtIsNullOrderByJoinedAtAsc(projectId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(
            UUID projectId,
            UUID projectMemberId,
            ProjectMemberRoleUpdateRequest request
    ) {
        validateProjectExists(projectId);
        validateProjectAdmin(projectId, request.requesterUserId());
        ProjectMember projectMember = findActiveProjectMember(projectId, projectMemberId);
        projectMember.changeRole(ProjectRole.valueOf(request.role()));

        return toMemberResponse(projectMember);
    }

    @Transactional
    public void removeProjectMember(UUID projectId, UUID projectMemberId, UUID requesterUserId) {
        validateProjectExists(projectId);
        validateProjectAdmin(projectId, requesterUserId);
        ProjectMember projectMember = findActiveProjectMember(projectId, projectMemberId);
        projectMember.remove(OffsetDateTime.now());
    }

    @Transactional
    public ProjectInvitationResponse createProjectInvitation(
            UUID projectId,
            ProjectInvitationCreateRequest request
    ) {
        Project project = findActiveProject(projectId);
        validateProjectAdmin(projectId, request.requesterUserId());
        ProjectInvitation invitation = projectInvitationRepository.save(ProjectInvitation.create(
                project,
                request.invitedEmail(),
                ProjectRole.valueOf(request.role()),
                request.expiresAt()
        ));

        return toInvitationResponse(invitation);
    }

    public List<ProjectInvitationResponse> getProjectInvitations(UUID projectId) {
        validateProjectExists(projectId);
        return projectInvitationRepository.findByProjectProjectIdOrderByExpiresAtAsc(projectId)
                .stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    @Transactional
    public ProjectInvitationResponse acceptProjectInvitation(
            UUID projectId,
            UUID invitationId,
            ProjectInvitationAcceptRequest request
    ) {
        validateProjectExists(projectId);
        ProjectInvitation invitation = findPendingInvitation(projectId, invitationId);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        invitation.accept();
        projectMemberRepository.save(ProjectMember.create(
                invitation.getProject(),
                user,
                invitation.getRole(),
                OffsetDateTime.now()
        ));

        return toInvitationResponse(invitation);
    }

    @Transactional
    public ProjectInvitationExpireResponse expireProjectInvitations(UUID projectId) {
        validateProjectExists(projectId);
        List<ProjectInvitation> invitations = projectInvitationRepository
                .findByProjectProjectIdAndStatusAndExpiresAtBefore(
                        projectId,
                        InvitationStatus.PENDING,
                        OffsetDateTime.now()
                );
        invitations.forEach(ProjectInvitation::expire);

        return new ProjectInvitationExpireResponse(invitations.size());
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

    private void validateProjectExists(UUID projectId) {
        findActiveProject(projectId);
    }

    private Project findActiveProject(UUID projectId) {
        return projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private void validateProjectAdmin(UUID projectId, UUID requesterUserId) {
        ProjectMember requester = projectMemberRepository
                .findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));
        if (requester.getRole() != ProjectRole.PROJECT_ADMIN) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private ProjectMember findActiveProjectMember(UUID projectId, UUID projectMemberId) {
        return projectMemberRepository
                .findByProjectMemberIdAndProjectProjectIdAndRemovedAtIsNull(projectMemberId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private ProjectInvitation findPendingInvitation(UUID projectId, UUID invitationId) {
        return projectInvitationRepository
                .findByInvitationIdAndProjectProjectIdAndStatus(invitationId, projectId, InvitationStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_INVITATION_NOT_FOUND));
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

    private ProjectMemberResponse toMemberResponse(ProjectMember projectMember) {
        User user = projectMember.getUser();
        return new ProjectMemberResponse(
                projectMember.getProjectMemberId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                projectMember.getRole().name(),
                projectMember.getJoinedAt()
        );
    }

    private ProjectInvitationResponse toInvitationResponse(ProjectInvitation invitation) {
        return new ProjectInvitationResponse(
                invitation.getInvitationId(),
                invitation.getProject().getProjectId(),
                invitation.getInvitedEmail(),
                invitation.getRole().name(),
                invitation.getStatus().name(),
                invitation.getExpiresAt()
        );
    }
}

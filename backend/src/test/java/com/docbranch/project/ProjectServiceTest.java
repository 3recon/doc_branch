package com.docbranch.project;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectInvitation;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.project.ProjectStatus;
import com.docbranch.domain.project.InvitationStatus;
import com.docbranch.domain.user.User;
import com.docbranch.repository.project.ProjectInvitationRepository;
import com.docbranch.repository.project.ProjectMemberRepository;
import com.docbranch.repository.project.ProjectRepository;
import com.docbranch.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class ProjectServiceTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final ProjectInvitationRepository projectInvitationRepository = mock(ProjectInvitationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProjectService projectService = new ProjectService(
            projectRepository,
            projectMemberRepository,
            projectInvitationRepository,
            userRepository
    );

    @Test
    void createProjectInvitationSavesPendingInvitation() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID invitationId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-07-03T09:00:00+09:00");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        ProjectInvitationCreateRequest request = new ProjectInvitationCreateRequest(
                "member@example.com",
                "PARTICIPANT",
                expiresAt
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> {
            ProjectInvitation invitation = invocation.getArgument(0);
            ReflectionTestUtils.setField(invitation, "invitationId", invitationId);
            return invitation;
        });

        ProjectInvitationResponse response = projectService.createProjectInvitation(projectId, request);

        ArgumentCaptor<ProjectInvitation> invitationCaptor = ArgumentCaptor.forClass(ProjectInvitation.class);
        verify(projectInvitationRepository).save(invitationCaptor.capture());
        ProjectInvitation savedInvitation = invitationCaptor.getValue();
        assertThat(savedInvitation.getProject()).isSameAs(project);
        assertThat(savedInvitation.getInvitedEmail()).isEqualTo("member@example.com");
        assertThat(savedInvitation.getRole()).isEqualTo(ProjectRole.PARTICIPANT);
        assertThat(savedInvitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(savedInvitation.getExpiresAt()).isEqualTo(expiresAt);

        assertThat(response.invitationId()).isEqualTo(invitationId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.invitedEmail()).isEqualTo("member@example.com");
        assertThat(response.role()).isEqualTo("PARTICIPANT");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void createProjectInvitationThrowsBusinessExceptionWhenProjectDoesNotExist() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        ProjectInvitationCreateRequest request = new ProjectInvitationCreateRequest(
                "member@example.com",
                "PARTICIPANT",
                OffsetDateTime.parse("2026-07-03T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProjectInvitation(projectId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void getProjectInvitationsReturnsProjectInvitations() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID invitationId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-07-03T09:00:00+09:00");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        ProjectInvitation invitation = projectInvitation(
                invitationId,
                project,
                "member@example.com",
                ProjectRole.PARTICIPANT,
                InvitationStatus.PENDING,
                expiresAt
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectInvitationRepository.findByProjectProjectIdOrderByExpiresAtAsc(projectId))
                .thenReturn(List.of(invitation));

        List<ProjectInvitationResponse> invitations = projectService.getProjectInvitations(projectId);

        assertThat(invitations).hasSize(1);
        assertThat(invitations.getFirst().invitationId()).isEqualTo(invitationId);
        assertThat(invitations.getFirst().projectId()).isEqualTo(projectId);
        assertThat(invitations.getFirst().invitedEmail()).isEqualTo("member@example.com");
        assertThat(invitations.getFirst().role()).isEqualTo("PARTICIPANT");
        assertThat(invitations.getFirst().status()).isEqualTo("PENDING");
        assertThat(invitations.getFirst().expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void getProjectMembersReturnsActiveMembers() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        User user = user(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Owner",
                "owner@example.com"
        );
        ProjectMember member = projectMember(
                UUID.fromString("11111111-2222-3333-4444-555555555555"),
                project,
                user,
                ProjectRole.PROJECT_ADMIN,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectProjectIdAndRemovedAtIsNullOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(member));

        List<ProjectMemberResponse> members = projectService.getProjectMembers(projectId);

        assertThat(members).hasSize(1);
        assertThat(members.getFirst().projectMemberId()).isEqualTo(member.getProjectMemberId());
        assertThat(members.getFirst().userId()).isEqualTo(user.getUserId());
        assertThat(members.getFirst().name()).isEqualTo("Owner");
        assertThat(members.getFirst().email()).isEqualTo("owner@example.com");
        assertThat(members.getFirst().role()).isEqualTo("PROJECT_ADMIN");
        assertThat(members.getFirst().joinedAt()).isEqualTo(member.getJoinedAt());
    }

    @Test
    void updateProjectMemberRoleChangesRole() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        User user = user(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Owner",
                "owner@example.com"
        );
        ProjectMember member = projectMember(
                memberId,
                project,
                user,
                ProjectRole.PARTICIPANT,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectMemberIdAndProjectProjectIdAndRemovedAtIsNull(memberId, projectId))
                .thenReturn(Optional.of(member));

        ProjectMemberResponse response = projectService.updateProjectMemberRole(
                projectId,
                memberId,
                new ProjectMemberRoleUpdateRequest("READ_ONLY")
        );

        assertThat(member.getRole()).isEqualTo(ProjectRole.READ_ONLY);
        assertThat(response.projectMemberId()).isEqualTo(memberId);
        assertThat(response.role()).isEqualTo("READ_ONLY");
    }

    @Test
    void removeProjectMemberSetsRemovedAt() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        User user = user(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Owner",
                "owner@example.com"
        );
        ProjectMember member = projectMember(
                memberId,
                project,
                user,
                ProjectRole.PARTICIPANT,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectMemberIdAndProjectProjectIdAndRemovedAtIsNull(memberId, projectId))
                .thenReturn(Optional.of(member));

        projectService.removeProjectMember(projectId, memberId);

        assertThat(member.getRemovedAt()).isNotNull();
    }

    @Test
    void updateProjectMemberRoleThrowsBusinessExceptionWhenMemberDoesNotExist() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("22222222-2222-3333-4444-555555555555");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectMemberIdAndProjectProjectIdAndRemovedAtIsNull(memberId, projectId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProjectMemberRole(
                projectId,
                memberId,
                new ProjectMemberRoleUpdateRequest("READ_ONLY")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
    }

    @Test
    void deleteProjectSoftDeletesProjectWithDeletedBy() {
        UUID projectId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID deletedByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        User deletedBy = user(deletedByUserId, "Owner");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(deletedByUserId)).thenReturn(Optional.of(deletedBy));

        projectService.deleteProject(projectId, new ProjectDeleteRequest(deletedByUserId));

        assertThat(project.getDeletedAt()).isNotNull();
        assertThat(project.getDeletedBy()).isSameAs(deletedBy);
    }

    @Test
    void deleteProjectThrowsBusinessExceptionWhenProjectDoesNotExist() {
        UUID projectId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        UUID deletedByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(
                projectId,
                new ProjectDeleteRequest(deletedByUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void deleteProjectThrowsBusinessExceptionWhenDeletedByDoesNotExist() {
        UUID projectId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        UUID deletedByUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Project project = project(
                projectId,
                "Project",
                "Description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-25T10:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(deletedByUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(
                projectId,
                new ProjectDeleteRequest(deletedByUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void updateProjectChangesProjectBasicInfo() {
        UUID projectId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-25T09:00:00+09:00");
        OffsetDateTime previousUpdatedAt = OffsetDateTime.parse("2026-06-25T10:00:00+09:00");
        Project project = project(
                projectId,
                "Original Project",
                "Original description",
                ProjectStatus.IN_PROGRESS,
                "Owner",
                createdAt,
                previousUpdatedAt
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));

        ProjectDetailResponse response = projectService.updateProject(
                projectId,
                new ProjectUpdateRequest("Updated Project", "Updated description")
        );

        assertThat(project.getName()).isEqualTo("Updated Project");
        assertThat(project.getDescription()).isEqualTo("Updated description");
        assertThat(project.getUpdatedAt()).isAfter(previousUpdatedAt);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Updated Project");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.ownerName()).isEqualTo("Owner");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(project.getUpdatedAt());
    }

    @Test
    void updateProjectThrowsBusinessExceptionWhenProjectDoesNotExist() {
        UUID projectId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(
                projectId,
                new ProjectUpdateRequest("Updated Project", "Updated description")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void createProjectSavesProjectAndProjectAdminMember() {
        UUID ownerUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User owner = user(ownerUserId, "홍길동");
        ProjectCreateRequest request = new ProjectCreateRequest(ownerUserId, "문서 관리 프로젝트", "문서 버전 관리");
        when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            ReflectionTestUtils.setField(project, "projectId", projectId);
            return project;
        });

        ProjectDetailResponse response = projectService.createProject(request);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getName()).isEqualTo("문서 관리 프로젝트");
        assertThat(savedProject.getDescription()).isEqualTo("문서 버전 관리");
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(savedProject.getOwner()).isSameAs(owner);
        assertThat(savedProject.getCreatedAt()).isNotNull();
        assertThat(savedProject.getUpdatedAt()).isEqualTo(savedProject.getCreatedAt());

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());
        ProjectMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getProject()).isSameAs(savedProject);
        assertThat(savedMember.getUser()).isSameAs(owner);
        assertThat(savedMember.getRole()).isEqualTo(ProjectRole.PROJECT_ADMIN);
        assertThat(savedMember.getJoinedAt()).isNotNull();

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("문서 관리 프로젝트");
        assertThat(response.description()).isEqualTo("문서 버전 관리");
        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.ownerName()).isEqualTo("홍길동");
        assertThat(response.createdAt()).isEqualTo(savedProject.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(savedProject.getUpdatedAt());
    }

    @Test
    void createProjectThrowsBusinessExceptionWhenOwnerDoesNotExist() {
        UUID ownerUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        ProjectCreateRequest request = new ProjectCreateRequest(ownerUserId, "문서 관리 프로젝트", null);
        when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getProjectsReturnsProjectsFromRepository() {
        Project project = project(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "문서 관리 프로젝트",
                "문서 버전 관리",
                ProjectStatus.IN_PROGRESS,
                "홍길동",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByDeletedAtIsNullOrderByUpdatedAtDesc()).thenReturn(List.of(project));

        List<ProjectSummaryResponse> projects = projectService.getProjects();

        assertThat(projects).hasSize(1);
        assertThat(projects.getFirst().projectId()).isEqualTo(project.getProjectId());
        assertThat(projects.getFirst().name()).isEqualTo("문서 관리 프로젝트");
        assertThat(projects.getFirst().status()).isEqualTo("IN_PROGRESS");
        assertThat(projects.getFirst().ownerName()).isEqualTo("홍길동");
        assertThat(projects.getFirst().updatedAt()).isEqualTo(project.getUpdatedAt());
    }

    @Test
    void getProjectReturnsProjectFromRepository() {
        UUID projectId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Project project = project(
                projectId,
                "문서 브랜치",
                "문서 버전 관리를 위한 프로젝트",
                ProjectStatus.IN_PROGRESS,
                "홍길동",
                OffsetDateTime.parse("2026-06-25T09:00:00+09:00"),
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));

        ProjectDetailResponse response = projectService.getProject(projectId);

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("문서 브랜치");
        assertThat(response.description()).isEqualTo("문서 버전 관리를 위한 프로젝트");
        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.ownerName()).isEqualTo("홍길동");
        assertThat(response.createdAt()).isEqualTo(project.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(project.getUpdatedAt());
    }

    @Test
    void getProjectThrowsBusinessExceptionWhenProjectDoesNotExist() {
        UUID projectId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    private Project project(
            UUID projectId,
            String name,
            String description,
            ProjectStatus status,
            String ownerName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        User owner = BeanUtils.instantiateClass(User.class);
        ReflectionTestUtils.setField(owner, "name", ownerName);

        Project project = BeanUtils.instantiateClass(Project.class);
        ReflectionTestUtils.setField(project, "projectId", projectId);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "description", description);
        ReflectionTestUtils.setField(project, "status", status);
        ReflectionTestUtils.setField(project, "owner", owner);
        ReflectionTestUtils.setField(project, "createdAt", createdAt);
        ReflectionTestUtils.setField(project, "updatedAt", updatedAt);
        return project;
    }

    private User user(UUID userId, String name) {
        User user = BeanUtils.instantiateClass(User.class);
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }

    private User user(UUID userId, String name, String email) {
        User user = user(userId, name);
        ReflectionTestUtils.setField(user, "email", email);
        return user;
    }

    private ProjectMember projectMember(
            UUID projectMemberId,
            Project project,
            User user,
            ProjectRole role,
            OffsetDateTime joinedAt
    ) {
        ProjectMember projectMember = BeanUtils.instantiateClass(ProjectMember.class);
        ReflectionTestUtils.setField(projectMember, "projectMemberId", projectMemberId);
        ReflectionTestUtils.setField(projectMember, "project", project);
        ReflectionTestUtils.setField(projectMember, "user", user);
        ReflectionTestUtils.setField(projectMember, "role", role);
        ReflectionTestUtils.setField(projectMember, "joinedAt", joinedAt);
        return projectMember;
    }

    private ProjectInvitation projectInvitation(
            UUID invitationId,
            Project project,
            String invitedEmail,
            ProjectRole role,
            InvitationStatus status,
            OffsetDateTime expiresAt
    ) {
        ProjectInvitation invitation = BeanUtils.instantiateClass(ProjectInvitation.class);
        ReflectionTestUtils.setField(invitation, "invitationId", invitationId);
        ReflectionTestUtils.setField(invitation, "project", project);
        ReflectionTestUtils.setField(invitation, "invitedEmail", invitedEmail);
        ReflectionTestUtils.setField(invitation, "role", role);
        ReflectionTestUtils.setField(invitation, "status", status);
        ReflectionTestUtils.setField(invitation, "expiresAt", expiresAt);
        return invitation;
    }
}

package com.docbranch.project;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.project.ProjectStatus;
import com.docbranch.domain.user.User;
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
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProjectService projectService = new ProjectService(
            projectRepository,
            projectMemberRepository,
            userRepository
    );

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
}

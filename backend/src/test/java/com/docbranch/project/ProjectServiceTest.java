package com.docbranch.project;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectStatus;
import com.docbranch.domain.user.User;
import com.docbranch.repository.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectService projectService = new ProjectService(projectRepository);

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
}

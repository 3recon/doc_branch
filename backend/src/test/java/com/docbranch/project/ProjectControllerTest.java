package com.docbranch.project;

import com.docbranch.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createProjectInvitationReturnsCreatedInvitation() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID invitationId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-07-03T09:00:00+09:00");
        when(projectService.createProjectInvitation(
                eq(projectId),
                any(ProjectInvitationCreateRequest.class)
        )).thenReturn(
                new ProjectInvitationResponse(
                        invitationId,
                        projectId,
                        "member@example.com",
                        "PARTICIPANT",
                        "PENDING",
                        expiresAt
                )
        );

        mockMvc.perform(post("/api/projects/{projectId}/invitations", projectId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "invitedEmail": "member@example.com",
                                  "role": "PARTICIPANT",
                                  "expiresAt": "2026-07-03T09:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationId").value(invitationId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.invitedEmail").value("member@example.com"))
                .andExpect(jsonPath("$.role").value("PARTICIPANT"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.expiresAt").value("2026-07-03T09:00:00+09:00"));
    }

    @Test
    void getProjectInvitationsReturnsInvitations() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID invitationId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-07-03T09:00:00+09:00");
        when(projectService.getProjectInvitations(projectId)).thenReturn(List.of(
                new ProjectInvitationResponse(
                        invitationId,
                        projectId,
                        "member@example.com",
                        "PARTICIPANT",
                        "PENDING",
                        expiresAt
                )
        ));

        mockMvc.perform(get("/api/projects/{projectId}/invitations", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invitationId").value(invitationId.toString()))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$[0].invitedEmail").value("member@example.com"))
                .andExpect(jsonPath("$[0].role").value("PARTICIPANT"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].expiresAt").value("2026-07-03T09:00:00+09:00"));
    }

    @Test
    void getProjectMembersReturnsMembers() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime joinedAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(projectService.getProjectMembers(projectId)).thenReturn(List.of(
                new ProjectMemberResponse(memberId, userId, "Owner", "owner@example.com", "PROJECT_ADMIN", joinedAt)
        ));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectMemberId").value(memberId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].name").value("Owner"))
                .andExpect(jsonPath("$[0].email").value("owner@example.com"))
                .andExpect(jsonPath("$[0].role").value("PROJECT_ADMIN"))
                .andExpect(jsonPath("$[0].joinedAt").value("2026-06-26T09:00:00+09:00"));
    }

    @Test
    void updateProjectMemberRoleReturnsUpdatedMember() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime joinedAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(projectService.updateProjectMemberRole(
                projectId,
                memberId,
                new ProjectMemberRoleUpdateRequest("READ_ONLY")
        )).thenReturn(
                new ProjectMemberResponse(memberId, userId, "Owner", "owner@example.com", "READ_ONLY", joinedAt)
        );

        mockMvc.perform(patch("/api/projects/{projectId}/members/{projectMemberId}", projectId, memberId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "READ_ONLY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(memberId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("READ_ONLY"));
    }

    @Test
    void removeProjectMemberReturnsNoContent() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID memberId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        mockMvc.perform(delete("/api/projects/{projectId}/members/{projectMemberId}", projectId, memberId))
                .andExpect(status().isNoContent());

        verify(projectService).removeProjectMember(projectId, memberId);
    }

    @Test
    void deleteProjectReturnsNoContent() throws Exception {
        UUID projectId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID deletedByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "deletedByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(projectId, new ProjectDeleteRequest(deletedByUserId));
    }

    @Test
    void updateProjectReturnsUpdatedProject() throws Exception {
        UUID projectId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-25T09:00:00+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-26T10:00:00+09:00");
        when(projectService.updateProject(
                projectId,
                new ProjectUpdateRequest("Updated Project", "Updated description")
        )).thenReturn(
                new ProjectDetailResponse(
                        projectId,
                        "Updated Project",
                        "Updated description",
                        "IN_PROGRESS",
                        "Owner",
                        createdAt,
                        updatedAt
                )
        );

        mockMvc.perform(patch("/api/projects/{projectId}", projectId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Project",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Project"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.ownerName").value("Owner"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-25T09:00:00+09:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T10:00:00+09:00"));
    }

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        UUID ownerUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(projectService.createProject(
                new ProjectCreateRequest(ownerUserId, "문서 관리 프로젝트", "문서 버전 관리")
        )).thenReturn(
                new ProjectDetailResponse(
                        projectId,
                        "문서 관리 프로젝트",
                        "문서 버전 관리",
                        "IN_PROGRESS",
                        "홍길동",
                        createdAt,
                        createdAt
                )
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                  "name": "문서 관리 프로젝트",
                                  "description": "문서 버전 관리"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("문서 관리 프로젝트"))
                .andExpect(jsonPath("$.description").value("문서 버전 관리"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.ownerName").value("홍길동"));
    }

    @Test
    void getProjectsReturnsProjectSummaries() throws Exception {
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(projectService.getProjects()).thenReturn(List.of(
                new ProjectSummaryResponse(projectId, "문서 관리 프로젝트", "IN_PROGRESS", "홍길동", updatedAt)
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$[0].name").value("문서 관리 프로젝트"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].ownerName").value("홍길동"))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-06-26T09:00:00+09:00"));
    }

    @Test
    void getProjectReturnsProjectDetail() throws Exception {
        UUID projectId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-25T09:00:00+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(projectService.getProject(projectId)).thenReturn(
                new ProjectDetailResponse(
                        projectId,
                        "문서 브랜치",
                        "문서 버전 관리를 위한 프로젝트",
                        "IN_PROGRESS",
                        "홍길동",
                        createdAt,
                        updatedAt
                )
        );

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("문서 브랜치"))
                .andExpect(jsonPath("$.description").value("문서 버전 관리를 위한 프로젝트"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.ownerName").value("홍길동"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-25T09:00:00+09:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T09:00:00+09:00"));
    }
}

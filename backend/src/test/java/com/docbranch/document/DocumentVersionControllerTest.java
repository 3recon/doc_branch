package com.docbranch.document;

import com.docbranch.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentVersionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DocumentVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentVersionService documentVersionService;

    @Test
    void createDocumentVersionReturnsCreatedVersion() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentVersionService.createDocumentVersion(
                eq(projectId),
                eq(documentDetailId),
                any(DocumentVersionCreateRequest.class)
        )).thenReturn(
                new DocumentVersionResponse(
                        documentVersionId,
                        documentDetailId,
                        1,
                        "First draft",
                        "Initial content",
                        "INITIAL",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                )
        );

        mockMvc.perform(post(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions",
                        projectId,
                        documentDetailId
                )
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First draft",
                                  "content": "Initial content",
                                  "createdByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentVersionId").value(documentVersionId.toString()))
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.title").value("First draft"))
                .andExpect(jsonPath("$.content").value("Initial content"))
                .andExpect(jsonPath("$.versionType").value("INITIAL"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.createdByUserId").value(createdByUserId.toString()))
                .andExpect(jsonPath("$.createdByName").value("Owner"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-26T09:00:00+09:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T09:00:00+09:00"));
    }

    @Test
    void getDocumentVersionsReturnsVersionsInVersionNumberOrder() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentVersionService.getDocumentVersions(projectId, documentDetailId)).thenReturn(List.of(
                new DocumentVersionResponse(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        documentDetailId,
                        1,
                        "First draft",
                        "Initial content",
                        "INITIAL",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                ),
                new DocumentVersionResponse(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        documentDetailId,
                        2,
                        "Second draft",
                        "Updated content",
                        "REVISION",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt.plusHours(1),
                        createdAt.plusHours(1)
                )
        ));

        mockMvc.perform(get(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions",
                        projectId,
                        documentDetailId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$[0].versionNumber").value(1))
                .andExpect(jsonPath("$[0].title").value("First draft"))
                .andExpect(jsonPath("$[0].content").value("Initial content"))
                .andExpect(jsonPath("$[0].versionType").value("INITIAL"))
                .andExpect(jsonPath("$[1].documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$[1].versionNumber").value(2))
                .andExpect(jsonPath("$[1].title").value("Second draft"))
                .andExpect(jsonPath("$[1].content").value("Updated content"))
                .andExpect(jsonPath("$[1].versionType").value("REVISION"));
    }

    @Test
    void getDocumentVersionReturnsVersion() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentVersionService.getDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId
        )).thenReturn(
                new DocumentVersionResponse(
                        documentVersionId,
                        documentDetailId,
                        1,
                        "First draft",
                        "Initial content",
                        "INITIAL",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                )
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}",
                        projectId,
                        documentDetailId,
                        documentVersionId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentVersionId").value(documentVersionId.toString()))
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.title").value("First draft"))
                .andExpect(jsonPath("$.content").value("Initial content"))
                .andExpect(jsonPath("$.versionType").value("INITIAL"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.createdByUserId").value(createdByUserId.toString()))
                .andExpect(jsonPath("$.createdByName").value("Owner"));
    }

    @Test
    void updateDocumentVersionReturnsUpdatedVersion() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-26T10:00:00+09:00");
        when(documentVersionService.updateDocumentVersion(
                eq(projectId),
                eq(documentDetailId),
                eq(documentVersionId),
                any(DocumentVersionUpdateRequest.class)
        )).thenReturn(
                new DocumentVersionResponse(
                        documentVersionId,
                        documentDetailId,
                        1,
                        "Updated draft",
                        "Updated content",
                        "INITIAL",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        updatedAt
                )
        );

        mockMvc.perform(patch(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}",
                        projectId,
                        documentDetailId,
                        documentVersionId
                )
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated draft",
                                  "content": "Updated content"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentVersionId").value(documentVersionId.toString()))
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.title").value("Updated draft"))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.versionType").value("INITIAL"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T10:00:00+09:00"));
    }

    @Test
    void deleteDocumentVersionReturnsNoContent() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        mockMvc.perform(delete(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}",
                        projectId,
                        documentDetailId,
                        documentVersionId
                ))
                .andExpect(status().isNoContent());

        verify(documentVersionService).deleteDocumentVersion(projectId, documentDetailId, documentVersionId);
    }

    @Test
    void updateFinalDocumentVersionReturnsFinalVersion() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-26T10:00:00+09:00");
        when(documentVersionService.updateFinalDocumentVersion(
                eq(projectId),
                eq(documentDetailId),
                eq(documentVersionId),
                any(DocumentVersionFinalUpdateRequest.class)
        )).thenReturn(
                new DocumentVersionResponse(
                        documentVersionId,
                        documentDetailId,
                        2,
                        "Second draft",
                        "Updated content",
                        "REVISION",
                        "DRAFT",
                        requesterUserId,
                        "Admin",
                        createdAt,
                        updatedAt
                )
        );

        mockMvc.perform(patch(
                        "/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}/final",
                        projectId,
                        documentDetailId,
                        documentVersionId
                )
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentVersionId").value(documentVersionId.toString()))
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.title").value("Second draft"))
                .andExpect(jsonPath("$.versionType").value("REVISION"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T10:00:00+09:00"));
    }
}

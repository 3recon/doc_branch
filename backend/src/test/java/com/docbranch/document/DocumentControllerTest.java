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
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void createDocumentReturnsCreatedDocument() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentService.createDocument(
                eq(projectId),
                any(DocumentCreateRequest.class)
        )).thenReturn(
                new DocumentResponse(
                        documentDetailId,
                        projectId,
                        "Guide",
                        "Project guide",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                )
        );

        mockMvc.perform(post("/api/projects/{projectId}/documents", projectId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Guide",
                                  "description": "Project guide",
                                  "createdByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Guide"))
                .andExpect(jsonPath("$.description").value("Project guide"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.createdByUserId").value(createdByUserId.toString()))
                .andExpect(jsonPath("$.createdByName").value("Owner"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-26T09:00:00+09:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T09:00:00+09:00"));
    }

    @Test
    void getDocumentsReturnsProjectDocuments() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentService.getDocuments(projectId)).thenReturn(List.of(
                new DocumentResponse(
                        documentDetailId,
                        projectId,
                        "Guide",
                        "Project guide",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                )
        ));

        mockMvc.perform(get("/api/projects/{projectId}/documents", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$[0].name").value("Guide"))
                .andExpect(jsonPath("$[0].status").value("DRAFT"))
                .andExpect(jsonPath("$[0].createdByUserId").value(createdByUserId.toString()));
    }

    @Test
    void getDocumentReturnsProjectDocument() throws Exception {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        when(documentService.getDocument(projectId, documentDetailId)).thenReturn(
                new DocumentResponse(
                        documentDetailId,
                        projectId,
                        "Guide",
                        "Project guide",
                        "DRAFT",
                        createdByUserId,
                        "Owner",
                        createdAt,
                        createdAt
                )
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/documents/{documentDetailId}",
                        projectId,
                        documentDetailId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentDetailId").value(documentDetailId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Guide"))
                .andExpect(jsonPath("$.description").value("Project guide"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.createdByUserId").value(createdByUserId.toString()))
                .andExpect(jsonPath("$.createdByName").value("Owner"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-26T09:00:00+09:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-26T09:00:00+09:00"));
    }
}

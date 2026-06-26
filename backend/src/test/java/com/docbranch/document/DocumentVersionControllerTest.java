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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
}

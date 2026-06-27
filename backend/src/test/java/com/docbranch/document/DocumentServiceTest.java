package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.document.DocumentStatus;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.project.ProjectStatus;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
import com.docbranch.repository.project.ProjectMemberRepository;
import com.docbranch.repository.project.ProjectRepository;
import com.docbranch.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentServiceTest {

    private final DocumentDetailRepository documentDetailRepository = mock(DocumentDetailRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DocumentService documentService = new DocumentService(
            documentDetailRepository,
            projectRepository,
            projectMemberRepository,
            userRepository
    );

    @Test
    void createDocumentSavesDraftDocumentWithCreatedBy() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        User createdBy = user(createdByUserId, "Owner");
        DocumentCreateRequest request = new DocumentCreateRequest("Guide", "Project guide", createdByUserId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.of(createdBy));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        createdBy,
                        ProjectRole.PARTICIPANT
                )));
        when(documentDetailRepository.save(any(DocumentDetail.class))).thenAnswer(invocation -> {
            DocumentDetail documentDetail = invocation.getArgument(0);
            ReflectionTestUtils.setField(documentDetail, "documentDetailId", documentDetailId);
            return documentDetail;
        });

        DocumentResponse response = documentService.createDocument(projectId, request);

        ArgumentCaptor<DocumentDetail> documentCaptor = ArgumentCaptor.forClass(DocumentDetail.class);
        verify(documentDetailRepository).save(documentCaptor.capture());
        DocumentDetail savedDocument = documentCaptor.getValue();
        assertThat(savedDocument.getProject()).isSameAs(project);
        assertThat(savedDocument.getName()).isEqualTo("Guide");
        assertThat(savedDocument.getDescription()).isEqualTo("Project guide");
        assertThat(savedDocument.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(savedDocument.getCreatedBy()).isSameAs(createdBy);
        assertThat(savedDocument.getCreatedAt()).isNotNull();
        assertThat(savedDocument.getUpdatedAt()).isEqualTo(savedDocument.getCreatedAt());

        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Guide");
        assertThat(response.description()).isEqualTo("Project guide");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.createdByUserId()).isEqualTo(createdByUserId);
        assertThat(response.createdByName()).isEqualTo("Owner");
    }

    @Test
    void createDocumentThrowsBusinessExceptionWhenProjectDoesNotExist() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        DocumentCreateRequest request = new DocumentCreateRequest("Guide", "Project guide", createdByUserId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.createDocument(projectId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void createDocumentThrowsBusinessExceptionWhenCreatedByDoesNotExist() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        DocumentCreateRequest request = new DocumentCreateRequest("Guide", "Project guide", createdByUserId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project(projectId)));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.createDocument(projectId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void createDocumentThrowsBusinessExceptionWhenCreatedByIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        User createdBy = user(createdByUserId, "Reader");
        DocumentCreateRequest request = new DocumentCreateRequest("Guide", "Project guide", createdByUserId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.of(createdBy));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        createdBy,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentService.createDocument(projectId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void getDocumentsReturnsProjectDocuments() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID requesterUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Project project = project(projectId);
        User createdBy = user(createdByUserId, "Owner");
        DocumentDetail documentDetail = documentDetail(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                project,
                "Guide",
                "Project guide",
                createdBy,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        project,
                        user(requesterUserId, "Reader"),
                        ProjectRole.READ_ONLY
                )));
        when(documentDetailRepository.findByProjectProjectIdAndDeletedAtIsNullOrderByUpdatedAtDescNameAsc(projectId))
                .thenReturn(List.of(documentDetail));

        List<DocumentResponse> documents = documentService.getDocuments(
                projectId,
                new DocumentReadRequest(requesterUserId)
        );

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().documentDetailId()).isEqualTo(documentDetail.getDocumentDetailId());
        assertThat(documents.getFirst().projectId()).isEqualTo(projectId);
        assertThat(documents.getFirst().name()).isEqualTo("Guide");
        assertThat(documents.getFirst().status()).isEqualTo("DRAFT");
        assertThat(documents.getFirst().createdByUserId()).isEqualTo(createdByUserId);
        assertThat(documents.getFirst().createdByName()).isEqualTo("Owner");
    }

    @Test
    void getDocumentReturnsProjectDocument() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID requesterUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Project project = project(projectId);
        User createdBy = user(createdByUserId, "Owner");
        DocumentDetail documentDetail = documentDetail(
                documentDetailId,
                project,
                "Guide",
                "Project guide",
                createdBy,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        project,
                        user(requesterUserId, "Reader"),
                        ProjectRole.READ_ONLY
                )));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));

        DocumentResponse response = documentService.getDocument(
                projectId,
                documentDetailId,
                new DocumentReadRequest(requesterUserId)
        );

        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Guide");
        assertThat(response.description()).isEqualTo("Project guide");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.createdByUserId()).isEqualTo(createdByUserId);
        assertThat(response.createdByName()).isEqualTo("Owner");
        assertThat(response.createdAt()).isEqualTo(OffsetDateTime.parse("2026-06-26T09:00:00+09:00"));
        assertThat(response.updatedAt()).isEqualTo(OffsetDateTime.parse("2026-06-26T09:00:00+09:00"));
    }

    @Test
    void getDocumentThrowsBusinessExceptionWhenDocumentDoesNotExistInProject() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Project project = project(projectId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        project,
                        user(requesterUserId, "Reader"),
                        ProjectRole.READ_ONLY
                )));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(
                projectId,
                documentDetailId,
                new DocumentReadRequest(requesterUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND);
    }

    @Test
    void getDocumentThrowsBusinessExceptionWhenRequesterIsNotProjectMember() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Project project = project(projectId);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(
                projectId,
                documentDetailId,
                new DocumentReadRequest(requesterUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void updateDocumentUpdatesProjectDocumentBasicInfo() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        Project project = project(projectId);
        User createdBy = user(createdByUserId, "Owner");
        DocumentDetail documentDetail = documentDetail(
                documentDetailId,
                project,
                "Guide",
                "Project guide",
                createdBy,
                createdAt
        );
        DocumentUpdateRequest request = new DocumentUpdateRequest(
                createdByUserId,
                "Updated Guide",
                "Updated project guide"
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        createdBy,
                        ProjectRole.PARTICIPANT
                )));

        DocumentResponse response = documentService.updateDocument(projectId, documentDetailId, request);

        assertThat(documentDetail.getName()).isEqualTo("Updated Guide");
        assertThat(documentDetail.getDescription()).isEqualTo("Updated project guide");
        assertThat(documentDetail.getUpdatedAt()).isAfter(createdAt);
        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Updated Guide");
        assertThat(response.description()).isEqualTo("Updated project guide");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.createdByUserId()).isEqualTo(createdByUserId);
        assertThat(response.createdByName()).isEqualTo("Owner");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(documentDetail.getUpdatedAt());
    }

    @Test
    void updateDocumentThrowsBusinessExceptionWhenRequesterIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        User requester = user(requesterUserId, "Reader");
        DocumentDetail documentDetail = documentDetail(
                documentDetailId,
                project,
                "Guide",
                "Project guide",
                requester,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        requester,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentService.updateDocument(
                projectId,
                documentDetailId,
                new DocumentUpdateRequest(requesterUserId, "Updated Guide", "Updated project guide")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void deleteDocumentSoftDeletesProjectDocument() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        Project project = project(projectId);
        User createdBy = user(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Owner");
        DocumentDetail documentDetail = documentDetail(
                documentDetailId,
                project,
                "Guide",
                "Project guide",
                createdBy,
                createdAt
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(
                projectId,
                createdBy.getUserId()
        ))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        createdBy,
                        ProjectRole.PROJECT_ADMIN
                )));

        documentService.deleteDocument(
                projectId,
                documentDetailId,
                new DocumentDeleteRequest(createdBy.getUserId())
        );

        assertThat(documentDetail.getDeletedAt()).isNotNull();
        assertThat(documentDetail.getDeletedAt()).isAfter(createdAt);
    }

    @Test
    void deleteDocumentThrowsBusinessExceptionWhenRequesterIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        User requester = user(requesterUserId, "Reader");
        DocumentDetail documentDetail = documentDetail(
                documentDetailId,
                project,
                "Guide",
                "Project guide",
                requester,
                OffsetDateTime.parse("2026-06-26T09:00:00+09:00")
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        project,
                        requester,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentService.deleteDocument(
                projectId,
                documentDetailId,
                new DocumentDeleteRequest(requesterUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    private Project project(UUID projectId) {
        Project project = BeanUtils.instantiateClass(Project.class);
        ReflectionTestUtils.setField(project, "projectId", projectId);
        ReflectionTestUtils.setField(project, "name", "Project");
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        return project;
    }

    private User user(UUID userId, String name) {
        User user = BeanUtils.instantiateClass(User.class);
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }

    private DocumentDetail documentDetail(
            UUID documentDetailId,
            Project project,
            String name,
            String description,
            User createdBy,
            OffsetDateTime now
    ) {
        DocumentDetail documentDetail = BeanUtils.instantiateClass(DocumentDetail.class);
        ReflectionTestUtils.setField(documentDetail, "documentDetailId", documentDetailId);
        ReflectionTestUtils.setField(documentDetail, "project", project);
        ReflectionTestUtils.setField(documentDetail, "name", name);
        ReflectionTestUtils.setField(documentDetail, "description", description);
        ReflectionTestUtils.setField(documentDetail, "status", DocumentStatus.DRAFT);
        ReflectionTestUtils.setField(documentDetail, "createdBy", createdBy);
        ReflectionTestUtils.setField(documentDetail, "createdAt", now);
        ReflectionTestUtils.setField(documentDetail, "updatedAt", now);
        return documentDetail;
    }

    private ProjectMember projectMember(
            UUID projectMemberId,
            Project project,
            User user,
            ProjectRole role
    ) {
        ProjectMember projectMember = BeanUtils.instantiateClass(ProjectMember.class);
        ReflectionTestUtils.setField(projectMember, "projectMemberId", projectMemberId);
        ReflectionTestUtils.setField(projectMember, "project", project);
        ReflectionTestUtils.setField(projectMember, "user", user);
        ReflectionTestUtils.setField(projectMember, "role", role);
        ReflectionTestUtils.setField(projectMember, "joinedAt", OffsetDateTime.parse("2026-06-26T09:00:00+09:00"));
        return projectMember;
    }
}

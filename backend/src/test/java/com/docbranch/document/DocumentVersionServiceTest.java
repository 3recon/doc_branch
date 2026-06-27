package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.document.DocumentStatus;
import com.docbranch.domain.document.DocumentVersion;
import com.docbranch.domain.document.DocumentVersionType;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.project.ProjectStatus;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
import com.docbranch.repository.document.DocumentVersionRepository;
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

class DocumentVersionServiceTest {

    private final DocumentVersionRepository documentVersionRepository = mock(DocumentVersionRepository.class);
    private final DocumentDetailRepository documentDetailRepository = mock(DocumentDetailRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DocumentVersionService documentVersionService = new DocumentVersionService(
            documentVersionRepository,
            documentDetailRepository,
            projectRepository,
            projectMemberRepository,
            userRepository
    );

    @Test
    void createDocumentVersionSavesNextVersionAndSetsRootAndFinalVersion() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersionCreateRequest request = new DocumentVersionCreateRequest(
                "First draft",
                "Initial content",
                createdByUserId
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.of(createdBy));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        createdBy,
                        ProjectRole.PARTICIPANT
                )));
        when(documentVersionRepository.findMaxVersionNumber(documentDetailId)).thenReturn(0);
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> {
            DocumentVersion documentVersion = invocation.getArgument(0);
            ReflectionTestUtils.setField(documentVersion, "documentVersionId", documentVersionId);
            return documentVersion;
        });

        DocumentVersionResponse response = documentVersionService.createDocumentVersion(
                projectId,
                documentDetailId,
                request
        );

        ArgumentCaptor<DocumentVersion> versionCaptor = ArgumentCaptor.forClass(DocumentVersion.class);
        verify(documentVersionRepository).save(versionCaptor.capture());
        DocumentVersion savedVersion = versionCaptor.getValue();
        assertThat(savedVersion.getDocumentDetail()).isSameAs(documentDetail);
        assertThat(savedVersion.getVersionNumber()).isEqualTo(1);
        assertThat(savedVersion.getTitle()).isEqualTo("First draft");
        assertThat(savedVersion.getContent()).isEqualTo("Initial content");
        assertThat(savedVersion.getVersionType()).isEqualTo(DocumentVersionType.INITIAL);
        assertThat(savedVersion.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(savedVersion.getUploadedBy()).isSameAs(createdBy);
        assertThat(savedVersion.getLastModifiedBy()).isSameAs(createdBy);
        assertThat(savedVersion.getCreatedAt()).isNotNull();
        assertThat(savedVersion.getUpdatedAt()).isEqualTo(savedVersion.getCreatedAt());
        assertThat(documentDetail.getRootVersion()).isSameAs(savedVersion);
        assertThat(documentDetail.getFinalVersion()).isSameAs(savedVersion);

        assertThat(response.documentVersionId()).isEqualTo(documentVersionId);
        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.title()).isEqualTo("First draft");
        assertThat(response.content()).isEqualTo("Initial content");
        assertThat(response.versionType()).isEqualTo("INITIAL");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.createdByUserId()).isEqualTo(createdByUserId);
        assertThat(response.createdByName()).isEqualTo("Owner");
    }

    @Test
    void createDocumentVersionIncrementsVersionNumberAndKeepsExistingRootVersion() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID rootVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID newVersionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersion rootVersion = documentVersion(
                rootVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                createdBy
        );
        ReflectionTestUtils.setField(documentDetail, "rootVersion", rootVersion);
        ReflectionTestUtils.setField(documentDetail, "finalVersion", rootVersion);
        DocumentVersionCreateRequest request = new DocumentVersionCreateRequest(
                "Second draft",
                "Updated content",
                createdByUserId
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.of(createdBy));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        createdBy,
                        ProjectRole.PROJECT_ADMIN
                )));
        when(documentVersionRepository.findMaxVersionNumber(documentDetailId)).thenReturn(1);
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> {
            DocumentVersion documentVersion = invocation.getArgument(0);
            ReflectionTestUtils.setField(documentVersion, "documentVersionId", newVersionId);
            return documentVersion;
        });

        DocumentVersionResponse response = documentVersionService.createDocumentVersion(
                projectId,
                documentDetailId,
                request
        );

        assertThat(response.versionNumber()).isEqualTo(2);
        assertThat(response.versionType()).isEqualTo("REVISION");
        assertThat(documentDetail.getRootVersion()).isSameAs(rootVersion);
        assertThat(documentDetail.getFinalVersion().getDocumentVersionId()).isEqualTo(newVersionId);
    }

    @Test
    void createDocumentVersionThrowsBusinessExceptionWhenDocumentDoesNotExistInProject() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        DocumentVersionCreateRequest request = new DocumentVersionCreateRequest(
                "First draft",
                "Initial content",
                createdByUserId
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project(projectId)));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentVersionService.createDocumentVersion(projectId, documentDetailId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND);
    }

    @Test
    void createDocumentVersionThrowsBusinessExceptionWhenCreatedByIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Reader");
        DocumentVersionCreateRequest request = new DocumentVersionCreateRequest(
                "First draft",
                "Initial content",
                createdByUserId
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(userRepository.findById(createdByUserId)).thenReturn(Optional.of(createdBy));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        createdBy,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentVersionService.createDocumentVersion(projectId, documentDetailId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void getDocumentVersionsReturnsNonDeletedVersionsInVersionNumberOrder() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersion firstVersion = documentVersion(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                documentDetail,
                1,
                "First draft",
                "Initial content",
                createdBy
        );
        DocumentVersion secondVersion = documentVersion(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                documentDetail,
                2,
                "Second draft",
                "Updated content",
                createdBy
        );
        ReflectionTestUtils.setField(secondVersion, "versionType", DocumentVersionType.REVISION);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDeletedAtIsNullOrderByVersionNumberAsc(documentDetailId))
                .thenReturn(List.of(firstVersion, secondVersion));

        List<DocumentVersionResponse> responses = documentVersionService.getDocumentVersions(projectId, documentDetailId);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).versionNumber()).isEqualTo(1);
        assertThat(responses.get(0).title()).isEqualTo("First draft");
        assertThat(responses.get(0).versionType()).isEqualTo("INITIAL");
        assertThat(responses.get(1).versionNumber()).isEqualTo(2);
        assertThat(responses.get(1).title()).isEqualTo("Second draft");
        assertThat(responses.get(1).versionType()).isEqualTo("REVISION");
    }

    @Test
    void getDocumentVersionReturnsVersionInProjectDocument() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersion documentVersion = documentVersion(
                documentVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                createdBy
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(documentVersion));

        DocumentVersionResponse response = documentVersionService.getDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId
        );

        assertThat(response.documentVersionId()).isEqualTo(documentVersionId);
        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.title()).isEqualTo("First draft");
        assertThat(response.content()).isEqualTo("Initial content");
        assertThat(response.versionType()).isEqualTo("INITIAL");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.createdByUserId()).isEqualTo(createdByUserId);
        assertThat(response.createdByName()).isEqualTo("Owner");
    }

    @Test
    void getDocumentVersionThrowsBusinessExceptionWhenVersionDoesNotExistInDocument() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentVersionService.getDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DOCUMENT_VERSION_NOT_FOUND);
    }

    @Test
    void updateDocumentVersionUpdatesTitleContentAndUpdatedAt() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersion documentVersion = documentVersion(
                documentVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                createdBy
        );
        OffsetDateTime beforeUpdatedAt = documentVersion.getUpdatedAt();
        DocumentVersionUpdateRequest request = new DocumentVersionUpdateRequest(
                createdByUserId,
                "Updated draft",
                "Updated content"
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(documentVersion));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        createdBy,
                        ProjectRole.PARTICIPANT
                )));

        DocumentVersionResponse response = documentVersionService.updateDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                request
        );

        assertThat(documentVersion.getTitle()).isEqualTo("Updated draft");
        assertThat(documentVersion.getContent()).isEqualTo("Updated content");
        assertThat(documentVersion.getUpdatedAt()).isAfter(beforeUpdatedAt);
        assertThat(response.documentVersionId()).isEqualTo(documentVersionId);
        assertThat(response.documentDetailId()).isEqualTo(documentDetailId);
        assertThat(response.title()).isEqualTo("Updated draft");
        assertThat(response.content()).isEqualTo("Updated content");
        assertThat(response.updatedAt()).isEqualTo(documentVersion.getUpdatedAt());
    }

    @Test
    void updateDocumentVersionThrowsBusinessExceptionWhenRequesterIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User requester = user(requesterUserId, "Reader");
        DocumentVersion documentVersion = documentVersion(
                documentVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                requester
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(documentVersion));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        requester,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentVersionService.updateDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionUpdateRequest(requesterUserId, "Updated draft", "Updated content")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void deleteDocumentVersionSoftDeletesVersion() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID createdByUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User createdBy = user(createdByUserId, "Owner");
        DocumentVersion documentVersion = documentVersion(
                documentVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                createdBy
        );
        OffsetDateTime beforeDeletedAt = documentVersion.getCreatedAt();
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(documentVersion));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, createdByUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        createdBy,
                        ProjectRole.PROJECT_ADMIN
                )));

        documentVersionService.deleteDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionDeleteRequest(createdByUserId)
        );

        assertThat(documentVersion.getDeletedAt()).isNotNull();
        assertThat(documentVersion.getDeletedAt()).isAfter(beforeDeletedAt);
    }

    @Test
    void deleteDocumentVersionThrowsBusinessExceptionWhenRequesterIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User requester = user(requesterUserId, "Reader");
        DocumentVersion documentVersion = documentVersion(
                documentVersionId,
                documentDetail,
                1,
                "First draft",
                "Initial content",
                requester
        );
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(documentVersion));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        requester,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentVersionService.deleteDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionDeleteRequest(requesterUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void updateFinalDocumentVersionSetsDocumentFinalVersionWhenRequesterIsProjectAdmin() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User admin = user(requesterUserId, "Admin");
        DocumentVersion finalVersion = documentVersion(
                documentVersionId,
                documentDetail,
                2,
                "Second draft",
                "Updated content",
                admin
        );
        ReflectionTestUtils.setField(finalVersion, "versionType", DocumentVersionType.REVISION);
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        admin,
                        ProjectRole.PROJECT_ADMIN
                )));
        when(documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                ))
                .thenReturn(Optional.of(finalVersion));

        DocumentVersionResponse response = documentVersionService.updateFinalDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionFinalUpdateRequest(requesterUserId)
        );

        assertThat(documentDetail.getFinalVersion()).isSameAs(finalVersion);
        assertThat(documentDetail.getUpdatedAt()).isAfter(documentDetail.getCreatedAt());
        assertThat(response.documentVersionId()).isEqualTo(documentVersionId);
        assertThat(response.versionNumber()).isEqualTo(2);
        assertThat(response.versionType()).isEqualTo("REVISION");
    }

    @Test
    void updateFinalDocumentVersionThrowsBusinessExceptionWhenRequesterIsParticipant() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User participant = user(requesterUserId, "Member");
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        participant,
                        ProjectRole.PARTICIPANT
                )));

        assertThatThrownBy(() -> documentVersionService.updateFinalDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionFinalUpdateRequest(requesterUserId)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void updateFinalDocumentVersionThrowsBusinessExceptionWhenRequesterIsReadOnly() {
        UUID projectId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID documentDetailId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID documentVersionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID requesterUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = project(projectId);
        DocumentDetail documentDetail = documentDetail(documentDetailId, project);
        User readOnly = user(requesterUserId, "Reader");
        when(projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)).thenReturn(Optional.of(project));
        when(documentDetailRepository.findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(
                projectId,
                documentDetailId
        )).thenReturn(Optional.of(documentDetail));
        when(projectMemberRepository.findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId))
                .thenReturn(Optional.of(projectMember(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        project,
                        readOnly,
                        ProjectRole.READ_ONLY
                )));

        assertThatThrownBy(() -> documentVersionService.updateFinalDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                new DocumentVersionFinalUpdateRequest(requesterUserId)
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

    private DocumentDetail documentDetail(UUID documentDetailId, Project project) {
        DocumentDetail documentDetail = BeanUtils.instantiateClass(DocumentDetail.class);
        ReflectionTestUtils.setField(documentDetail, "documentDetailId", documentDetailId);
        ReflectionTestUtils.setField(documentDetail, "project", project);
        ReflectionTestUtils.setField(documentDetail, "name", "Guide");
        ReflectionTestUtils.setField(documentDetail, "description", "Project guide");
        ReflectionTestUtils.setField(documentDetail, "status", DocumentStatus.DRAFT);
        ReflectionTestUtils.setField(documentDetail, "createdBy", user(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Creator"));
        OffsetDateTime now = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        ReflectionTestUtils.setField(documentDetail, "createdAt", now);
        ReflectionTestUtils.setField(documentDetail, "updatedAt", now);
        return documentDetail;
    }

    private DocumentVersion documentVersion(
            UUID documentVersionId,
            DocumentDetail documentDetail,
            int versionNumber,
            String title,
            String content,
            User createdBy
    ) {
        DocumentVersion documentVersion = BeanUtils.instantiateClass(DocumentVersion.class);
        ReflectionTestUtils.setField(documentVersion, "documentVersionId", documentVersionId);
        ReflectionTestUtils.setField(documentVersion, "documentDetail", documentDetail);
        ReflectionTestUtils.setField(documentVersion, "versionNumber", versionNumber);
        ReflectionTestUtils.setField(documentVersion, "title", title);
        ReflectionTestUtils.setField(documentVersion, "content", content);
        ReflectionTestUtils.setField(documentVersion, "versionType", DocumentVersionType.INITIAL);
        ReflectionTestUtils.setField(documentVersion, "status", DocumentStatus.DRAFT);
        ReflectionTestUtils.setField(documentVersion, "uploadedBy", createdBy);
        ReflectionTestUtils.setField(documentVersion, "lastModifiedBy", createdBy);
        OffsetDateTime now = OffsetDateTime.parse("2026-06-26T09:00:00+09:00");
        ReflectionTestUtils.setField(documentVersion, "createdAt", now);
        ReflectionTestUtils.setField(documentVersion, "updatedAt", now);
        return documentVersion;
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

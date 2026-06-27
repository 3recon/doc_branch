package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.document.DocumentVersion;
import com.docbranch.domain.document.DocumentVersionType;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
import com.docbranch.repository.document.DocumentVersionRepository;
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
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentDetailRepository documentDetailRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public DocumentVersionService(
            DocumentVersionRepository documentVersionRepository,
            DocumentDetailRepository documentDetailRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentDetailRepository = documentDetailRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DocumentVersionResponse createDocumentVersion(
            UUID projectId,
            UUID documentDetailId,
            DocumentVersionCreateRequest request
    ) {
        findActiveProject(projectId);
        DocumentDetail documentDetail = documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        User createdBy = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateProjectWritableMember(projectId, request.createdByUserId());

        int nextVersionNumber = documentVersionRepository.findMaxVersionNumber(documentDetailId) + 1;
        DocumentVersionType versionType = nextVersionNumber == 1
                ? DocumentVersionType.INITIAL
                : DocumentVersionType.REVISION;
        OffsetDateTime now = OffsetDateTime.now();
        DocumentVersion documentVersion = documentVersionRepository.save(DocumentVersion.create(
                documentDetail,
                nextVersionNumber,
                request.title(),
                request.content(),
                versionType,
                createdBy,
                now
        ));
        documentDetail.registerVersion(documentVersion, now);

        return toResponse(documentVersion);
    }

    public List<DocumentVersionResponse> getDocumentVersions(
            UUID projectId,
            UUID documentDetailId,
            DocumentReadRequest request
    ) {
        findActiveProject(projectId);
        validateProjectMember(projectId, request.requesterUserId());
        documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));

        return documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDeletedAtIsNullOrderByVersionNumberAsc(documentDetailId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentVersionResponse getDocumentVersion(
            UUID projectId,
            UUID documentDetailId,
            UUID documentVersionId,
            DocumentReadRequest request
    ) {
        findActiveProject(projectId);
        validateProjectMember(projectId, request.requesterUserId());
        documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        DocumentVersion documentVersion = documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_VERSION_NOT_FOUND));

        return toResponse(documentVersion);
    }

    @Transactional
    public DocumentVersionResponse updateDocumentVersion(
            UUID projectId,
            UUID documentDetailId,
            UUID documentVersionId,
            DocumentVersionUpdateRequest request
    ) {
        findActiveProject(projectId);
        documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        DocumentVersion documentVersion = documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_VERSION_NOT_FOUND));
        validateProjectWritableMember(projectId, request.requesterUserId());

        documentVersion.updateContent(request.title(), request.content(), OffsetDateTime.now());

        return toResponse(documentVersion);
    }

    @Transactional
    public DocumentVersionResponse updateFinalDocumentVersion(
            UUID projectId,
            UUID documentDetailId,
            UUID documentVersionId,
            DocumentVersionFinalUpdateRequest request
    ) {
        findActiveProject(projectId);
        DocumentDetail documentDetail = documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        validateProjectAdmin(projectId, request.requesterUserId());
        DocumentVersion documentVersion = documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_VERSION_NOT_FOUND));

        documentDetail.updateFinalVersion(documentVersion, OffsetDateTime.now());

        return toResponse(documentVersion);
    }

    @Transactional
    public void deleteDocumentVersion(
            UUID projectId,
            UUID documentDetailId,
            UUID documentVersionId,
            DocumentVersionDeleteRequest request
    ) {
        findActiveProject(projectId);
        documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        DocumentVersion documentVersion = documentVersionRepository
                .findByDocumentDetailDocumentDetailIdAndDocumentVersionIdAndDeletedAtIsNull(
                        documentDetailId,
                        documentVersionId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_VERSION_NOT_FOUND));
        validateProjectWritableMember(projectId, request.requesterUserId());

        documentVersion.delete(OffsetDateTime.now());
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

    private void validateProjectWritableMember(UUID projectId, UUID requesterUserId) {
        ProjectMember requester = projectMemberRepository
                .findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));
        if (requester.getRole() == ProjectRole.READ_ONLY) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private void validateProjectMember(UUID projectId, UUID requesterUserId) {
        projectMemberRepository
                .findByProjectProjectIdAndUserUserIdAndRemovedAtIsNull(projectId, requesterUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    private DocumentVersionResponse toResponse(DocumentVersion documentVersion) {
        User createdBy = documentVersion.getUploadedBy();
        return new DocumentVersionResponse(
                documentVersion.getDocumentVersionId(),
                documentVersion.getDocumentDetail().getDocumentDetailId(),
                documentVersion.getVersionNumber(),
                documentVersion.getTitle(),
                documentVersion.getContent(),
                documentVersion.getVersionType().name(),
                documentVersion.getStatus().name(),
                createdBy.getUserId(),
                createdBy.getName(),
                documentVersion.getCreatedAt(),
                documentVersion.getUpdatedAt()
        );
    }
}

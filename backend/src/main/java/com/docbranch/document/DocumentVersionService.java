package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.document.DocumentVersion;
import com.docbranch.domain.document.DocumentVersionType;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
import com.docbranch.repository.document.DocumentVersionRepository;
import com.docbranch.repository.project.ProjectRepository;
import com.docbranch.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentDetailRepository documentDetailRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DocumentVersionService(
            DocumentVersionRepository documentVersionRepository,
            DocumentDetailRepository documentDetailRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentDetailRepository = documentDetailRepository;
        this.projectRepository = projectRepository;
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

    private Project findActiveProject(UUID projectId) {
        return projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
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

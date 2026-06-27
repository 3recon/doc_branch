package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.project.ProjectRole;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
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
public class DocumentService {

    private final DocumentDetailRepository documentDetailRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public DocumentService(
            DocumentDetailRepository documentDetailRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.documentDetailRepository = documentDetailRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DocumentResponse createDocument(UUID projectId, DocumentCreateRequest request) {
        Project project = findActiveProject(projectId);
        User createdBy = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateProjectWritableMember(projectId, request.createdByUserId());
        OffsetDateTime now = OffsetDateTime.now();
        DocumentDetail documentDetail = documentDetailRepository.save(DocumentDetail.create(
                project,
                request.name(),
                request.description(),
                createdBy,
                now
        ));

        return toResponse(documentDetail);
    }

    public List<DocumentResponse> getDocuments(UUID projectId, DocumentReadRequest request) {
        findActiveProject(projectId);
        validateProjectMember(projectId, request.requesterUserId());
        return documentDetailRepository
                .findByProjectProjectIdAndDeletedAtIsNullOrderByUpdatedAtDescNameAsc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentResponse getDocument(UUID projectId, UUID documentDetailId, DocumentReadRequest request) {
        findActiveProject(projectId);
        validateProjectMember(projectId, request.requesterUserId());
        DocumentDetail documentDetail = documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));

        return toResponse(documentDetail);
    }

    @Transactional
    public DocumentResponse updateDocument(
            UUID projectId,
            UUID documentDetailId,
            DocumentUpdateRequest request
    ) {
        findActiveProject(projectId);
        DocumentDetail documentDetail = documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        validateProjectWritableMember(projectId, request.requesterUserId());

        documentDetail.updateBasicInfo(request.name(), request.description(), OffsetDateTime.now());

        return toResponse(documentDetail);
    }

    @Transactional
    public void deleteDocument(UUID projectId, UUID documentDetailId, DocumentDeleteRequest request) {
        findActiveProject(projectId);
        DocumentDetail documentDetail = documentDetailRepository
                .findByProjectProjectIdAndDocumentDetailIdAndDeletedAtIsNull(projectId, documentDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_DETAIL_NOT_FOUND));
        validateProjectWritableMember(projectId, request.requesterUserId());

        documentDetail.delete(OffsetDateTime.now());
    }

    private Project findActiveProject(UUID projectId) {
        return projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
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

    private DocumentResponse toResponse(DocumentDetail documentDetail) {
        User createdBy = documentDetail.getCreatedBy();
        return new DocumentResponse(
                documentDetail.getDocumentDetailId(),
                documentDetail.getProject().getProjectId(),
                documentDetail.getName(),
                documentDetail.getDescription(),
                documentDetail.getStatus().name(),
                createdBy.getUserId(),
                createdBy.getName(),
                documentDetail.getCreatedAt(),
                documentDetail.getUpdatedAt()
        );
    }
}

package com.docbranch.document;

import com.docbranch.common.exception.BusinessException;
import com.docbranch.common.exception.ErrorCode;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.user.User;
import com.docbranch.repository.document.DocumentDetailRepository;
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
    private final UserRepository userRepository;

    public DocumentService(
            DocumentDetailRepository documentDetailRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.documentDetailRepository = documentDetailRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DocumentResponse createDocument(UUID projectId, DocumentCreateRequest request) {
        Project project = findActiveProject(projectId);
        User createdBy = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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

    public List<DocumentResponse> getDocuments(UUID projectId) {
        findActiveProject(projectId);
        return documentDetailRepository
                .findByProjectProjectIdAndDeletedAtIsNullOrderByUpdatedAtDescNameAsc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Project findActiveProject(UUID projectId) {
        return projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
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

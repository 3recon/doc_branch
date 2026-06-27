package com.docbranch.document;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/documents/{documentDetailId}/versions")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping
    public ResponseEntity<DocumentVersionResponse> createDocumentVersion(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @Valid @RequestBody DocumentVersionCreateRequest request
    ) {
        DocumentVersionResponse response = documentVersionService.createDocumentVersion(
                projectId,
                documentDetailId,
                request
        );

        return ResponseEntity
                .created(URI.create(
                        "/api/projects/" + projectId
                                + "/documents/" + documentDetailId
                                + "/versions/" + response.documentVersionId()
                ))
                .body(response);
    }

    @GetMapping
    public List<DocumentVersionResponse> getDocumentVersions(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId
    ) {
        return documentVersionService.getDocumentVersions(projectId, documentDetailId);
    }

    @GetMapping("/{documentVersionId}")
    public DocumentVersionResponse getDocumentVersion(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @PathVariable UUID documentVersionId
    ) {
        return documentVersionService.getDocumentVersion(projectId, documentDetailId, documentVersionId);
    }

    @PatchMapping("/{documentVersionId}")
    public DocumentVersionResponse updateDocumentVersion(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @PathVariable UUID documentVersionId,
            @Valid @RequestBody DocumentVersionUpdateRequest request
    ) {
        return documentVersionService.updateDocumentVersion(projectId, documentDetailId, documentVersionId, request);
    }

    @PatchMapping("/{documentVersionId}/final")
    public DocumentVersionResponse updateFinalDocumentVersion(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @PathVariable UUID documentVersionId,
            @Valid @RequestBody DocumentVersionFinalUpdateRequest request
    ) {
        return documentVersionService.updateFinalDocumentVersion(
                projectId,
                documentDetailId,
                documentVersionId,
                request
        );
    }

    @DeleteMapping("/{documentVersionId}")
    public ResponseEntity<Void> deleteDocumentVersion(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @PathVariable UUID documentVersionId,
            @Valid @RequestBody DocumentVersionDeleteRequest request
    ) {
        documentVersionService.deleteDocumentVersion(projectId, documentDetailId, documentVersionId, request);
        return ResponseEntity.noContent().build();
    }
}

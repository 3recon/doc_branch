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
@RequestMapping("/api/projects/{projectId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable UUID projectId,
            @Valid @RequestBody DocumentCreateRequest request
    ) {
        DocumentResponse response = documentService.createDocument(projectId, request);
        return ResponseEntity
                .created(URI.create("/api/projects/" + projectId + "/documents/" + response.documentDetailId()))
                .body(response);
    }

    @GetMapping
    public List<DocumentResponse> getDocuments(@PathVariable UUID projectId) {
        return documentService.getDocuments(projectId);
    }

    @GetMapping("/{documentDetailId}")
    public DocumentResponse getDocument(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId
    ) {
        return documentService.getDocument(projectId, documentDetailId);
    }

    @PatchMapping("/{documentDetailId}")
    public DocumentResponse updateDocument(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @Valid @RequestBody DocumentUpdateRequest request
    ) {
        return documentService.updateDocument(projectId, documentDetailId, request);
    }

    @DeleteMapping("/{documentDetailId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID projectId,
            @PathVariable UUID documentDetailId,
            @Valid @RequestBody DocumentDeleteRequest request
    ) {
        documentService.deleteDocument(projectId, documentDetailId, request);
        return ResponseEntity.noContent().build();
    }
}

package com.docbranch.document;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

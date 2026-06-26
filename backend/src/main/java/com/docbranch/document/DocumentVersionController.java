package com.docbranch.document;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
}

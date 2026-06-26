package com.docbranch.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentDetailId,
        UUID projectId,
        String name,
        String description,
        String status,
        UUID createdByUserId,
        String createdByName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

package com.docbranch.project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectDetailResponse(
        UUID projectId,
        String name,
        String description,
        String status,
        String ownerName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

package com.docbranch.project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectSummaryResponse(
        UUID projectId,
        String name,
        String status,
        String ownerName,
        OffsetDateTime updatedAt
) {
}

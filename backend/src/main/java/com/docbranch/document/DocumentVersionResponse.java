package com.docbranch.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID documentVersionId,
        UUID documentDetailId,
        Integer versionNumber,
        String title,
        String content,
        String versionType,
        String status,
        UUID createdByUserId,
        String createdByName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

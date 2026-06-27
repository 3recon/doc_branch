package com.docbranch.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String name,
        String email,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

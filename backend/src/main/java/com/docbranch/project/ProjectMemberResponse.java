package com.docbranch.project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID projectMemberId,
        UUID userId,
        String name,
        String email,
        String role,
        OffsetDateTime joinedAt
) {
}

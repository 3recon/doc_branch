package com.docbranch.project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectInvitationResponse(
        UUID invitationId,
        UUID projectId,
        String invitedEmail,
        String role,
        String status,
        OffsetDateTime expiresAt
) {
}

package com.docbranch.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectInvitationCreateRequest(
        @NotNull UUID requesterUserId,
        @NotBlank @Email String invitedEmail,
        @NotNull String role,
        @NotNull OffsetDateTime expiresAt
) {
}

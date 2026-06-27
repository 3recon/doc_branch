package com.docbranch.project;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRoleUpdateRequest(
        @NotNull UUID requesterUserId,
        @NotNull String role
) {
}

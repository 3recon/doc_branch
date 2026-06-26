package com.docbranch.project;

import jakarta.validation.constraints.NotNull;

public record ProjectMemberRoleUpdateRequest(
        @NotNull String role
) {
}

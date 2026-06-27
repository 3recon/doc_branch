package com.docbranch.project;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRemoveRequest(
        @NotNull UUID requesterUserId
) {
}

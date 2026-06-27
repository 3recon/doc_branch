package com.docbranch.project;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectReadRequest(
        @NotNull UUID requesterUserId
) {
}

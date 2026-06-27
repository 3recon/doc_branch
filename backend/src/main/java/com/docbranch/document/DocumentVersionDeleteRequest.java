package com.docbranch.document;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DocumentVersionDeleteRequest(
        @NotNull UUID requesterUserId
) {
}

package com.docbranch.document;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DocumentVersionFinalUpdateRequest(
        @NotNull UUID requesterUserId
) {
}

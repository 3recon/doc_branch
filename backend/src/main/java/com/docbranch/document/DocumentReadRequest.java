package com.docbranch.document;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DocumentReadRequest(
        @NotNull UUID requesterUserId
) {
}

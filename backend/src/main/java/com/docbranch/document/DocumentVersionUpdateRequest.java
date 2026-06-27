package com.docbranch.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DocumentVersionUpdateRequest(
        @NotNull UUID requesterUserId,
        @NotBlank @Size(max = 150) String title,
        @NotBlank String content
) {
}

package com.docbranch.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentVersionUpdateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank String content
) {
}

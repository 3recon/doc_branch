package com.docbranch.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentUpdateRequest(
        @NotBlank @Size(max = 150) String name,
        String description
) {
}

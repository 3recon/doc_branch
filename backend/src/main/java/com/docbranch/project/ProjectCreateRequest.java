package com.docbranch.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProjectCreateRequest(
        @NotNull UUID ownerUserId,
        @NotBlank @Size(max = 150) String name,
        String description
) {
}

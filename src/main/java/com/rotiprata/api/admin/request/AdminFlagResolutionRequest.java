package com.rotiprata.api.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the admin flag resolution request payload exchanged by the API layer.
 */
public record AdminFlagResolutionRequest(
    @NotBlank String status,
    @Size(max = 500) String feedback
) {}

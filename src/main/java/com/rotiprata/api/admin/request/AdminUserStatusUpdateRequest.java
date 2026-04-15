package com.rotiprata.api.admin.request;

import jakarta.validation.constraints.NotBlank;

public record AdminUserStatusUpdateRequest(
    @NotBlank String status
) {}

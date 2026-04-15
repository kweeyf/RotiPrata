package com.rotiprata.api.admin.request;

import com.rotiprata.security.authorization.AppRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserRoleUpdateRequest(
    @NotNull AppRole role
) {}

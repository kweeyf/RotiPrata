package com.rotiprata.api.admin.response;

import java.time.LocalDateTime;

public record AdminUserSearchHistoryResponse(
    String id,
    String query,
    LocalDateTime searchedAt
) {}

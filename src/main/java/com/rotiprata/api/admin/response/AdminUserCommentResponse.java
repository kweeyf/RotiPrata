package com.rotiprata.api.admin.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserCommentResponse(
    UUID id,
    UUID contentId,
    String contentTitle,
    String body,
    String author,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

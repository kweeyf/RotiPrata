package com.rotiprata.api.admin.request;

import java.util.UUID;

public record AdminLessonCategoryMoveRequest(
    UUID sourceCategoryId,
    UUID targetCategoryId
) {}

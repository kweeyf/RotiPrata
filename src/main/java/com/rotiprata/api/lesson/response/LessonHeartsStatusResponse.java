package com.rotiprata.api.lesson.response;

import java.time.OffsetDateTime;

public record LessonHeartsStatusResponse(
    int heartsRemaining,
    OffsetDateTime heartsRefillAt
) {}

package com.rotiprata.api.lesson.response;

import java.util.List;

public record LessonHubUnitResponse(
    String unitId,
    String title,
    Integer orderIndex,
    String accentColor,
    List<LessonHubLessonResponse> lessons
) {}

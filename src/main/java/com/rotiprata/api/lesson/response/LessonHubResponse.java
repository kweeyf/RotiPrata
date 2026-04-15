package com.rotiprata.api.lesson.response;

import java.util.List;

public record LessonHubResponse(
    List<LessonHubCategoryResponse> categories,
    LessonHubSummaryResponse summary
) {}

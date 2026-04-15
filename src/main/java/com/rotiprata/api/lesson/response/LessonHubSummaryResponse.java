package com.rotiprata.api.lesson.response;

public record LessonHubSummaryResponse(
    int totalLessons,
    int completedLessons,
    int currentStreak
) {}

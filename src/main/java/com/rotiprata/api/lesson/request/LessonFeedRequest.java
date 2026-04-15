package com.rotiprata.api.lesson.request;

public record LessonFeedRequest(
    String query,
    String difficulty,
    String duration,
    String sort,
    Integer page,
    Integer pageSize
) {}

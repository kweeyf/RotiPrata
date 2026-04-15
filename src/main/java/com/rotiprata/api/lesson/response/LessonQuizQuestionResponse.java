package com.rotiprata.api.lesson.response;

import java.util.Map;

public record LessonQuizQuestionResponse(
    String questionId,
    String questionType,
    String prompt,
    String questionText,
    Map<String, Object> payload,
    String explanation,
    Integer points,
    Integer orderIndex,
    String mediaUrl,
    Integer templateVersion
) {}

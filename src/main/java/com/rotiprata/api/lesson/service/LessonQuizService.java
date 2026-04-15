package com.rotiprata.api.lesson.service;

import com.rotiprata.api.lesson.dto.LessonHeartsStatusResponse;
import com.rotiprata.api.lesson.dto.LessonQuizAnswerRequest;
import com.rotiprata.api.lesson.dto.LessonQuizAnswerResponse;
import com.rotiprata.api.lesson.dto.LessonQuizStateResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LessonQuizService {

    ProgressMetadata getProgressMetadata(
        UUID userId,
        UUID lessonId,
        List<Map<String, Object>> sections,
        int completedSections,
        boolean isEnrolled,
        String accessToken
    );

    LessonQuizStateResponse getQuizState(UUID userId, UUID lessonId, String accessToken);

    boolean hasActiveLessonQuiz(UUID lessonId);

    LessonHeartsStatusResponse getHeartsStatus(UUID userId, String accessToken);

    LessonQuizAnswerResponse answerQuestion(
        UUID userId,
        UUID lessonId,
        LessonQuizAnswerRequest request,
        String accessToken
    );

    LessonQuizStateResponse restartQuiz(UUID userId, UUID lessonId, String mode, String accessToken);

    record ProgressMetadata(
        int totalStops,
        int completedStops,
        String currentStopId,
        int remainingStops,
        String quizStatus,
        int heartsRemaining,
        OffsetDateTime heartsRefillAt,
        String nextStopType
    ) {}
}

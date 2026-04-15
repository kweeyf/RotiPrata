package com.rotiprata.api.lesson.service;

import com.rotiprata.api.admin.dto.AdminLessonCategoryMoveRequest;
import com.rotiprata.api.admin.dto.AdminLessonCategoryMoveResponse;
import com.rotiprata.api.admin.dto.AdminLessonDraftResponse;
import com.rotiprata.api.admin.dto.AdminPublishLessonResponse;
import com.rotiprata.api.admin.dto.AdminStepSaveRequest;
import com.rotiprata.api.admin.dto.AdminStepSaveResponse;
import com.rotiprata.api.lesson.dto.LessonFeedRequest;
import com.rotiprata.api.lesson.dto.LessonFeedResponse;
import com.rotiprata.api.lesson.dto.LessonHubResponse;
import com.rotiprata.api.lesson.dto.LessonMediaStartLinkRequest;
import com.rotiprata.api.lesson.dto.LessonMediaStartResponse;
import com.rotiprata.api.lesson.dto.LessonMediaStatusResponse;
import com.rotiprata.api.lesson.dto.LessonProgressResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface LessonService {

    String findRelevantLesson(String accessToken, String question);

    List<Map<String, Object>> getLessons(String accessToken);

    LessonFeedResponse getLessonFeed(String accessToken, LessonFeedRequest request);

    LessonHubResponse getLessonHub(UUID userId, String accessToken);

    List<Map<String, Object>> getAdminLessons(UUID userId, String accessToken);

    Map<String, Object> getAdminLessonById(UUID userId, UUID lessonId, String accessToken);

    AdminLessonDraftResponse createLessonDraft(UUID userId, Map<String, Object> payload, String accessToken);

    AdminStepSaveResponse saveLessonStep(
        UUID userId,
        UUID lessonId,
        String stepKey,
        AdminStepSaveRequest request,
        String accessToken
    );

    AdminPublishLessonResponse publishLessonWithValidation(
        UUID userId,
        UUID lessonId,
        AdminStepSaveRequest request,
        String accessToken
    );

    List<Map<String, Object>> searchLessons(String query, String accessToken);

    Map<String, Object> getLessonById(UUID lessonId, String accessToken);

    List<Map<String, Object>> getLessonSections(UUID lessonId, String accessToken);

    LessonMediaStartResponse startLessonMediaUpload(
        UUID userId,
        UUID lessonId,
        MultipartFile file,
        String accessToken
    );

    LessonMediaStartResponse startLessonMediaLink(
        UUID userId,
        UUID lessonId,
        LessonMediaStartLinkRequest request,
        String accessToken
    );

    LessonMediaStatusResponse getLessonMediaStatus(
        UUID userId,
        UUID lessonId,
        UUID assetId,
        String accessToken
    );

    Map<String, Object> createLesson(UUID userId, Map<String, Object> payload, String accessToken);

    Map<String, Object> updateLesson(UUID userId, UUID lessonId, Map<String, Object> payload, String accessToken);

    AdminLessonCategoryMoveResponse moveLessonToCategory(
        UUID userId,
        UUID lessonId,
        AdminLessonCategoryMoveRequest request,
        String accessToken
    );

    void deleteLesson(UUID userId, UUID lessonId, String accessToken);

    Map<String, Object> createLessonQuiz(UUID userId, UUID lessonId, Map<String, Object> payload, String accessToken);

    List<Map<String, Object>> getActiveLessonQuizQuestions(UUID userId, UUID lessonId, String accessToken);

    List<Map<String, Object>> getAdminQuizQuestionTypes(UUID userId, String accessToken);

    List<Map<String, Object>> replaceLessonQuiz(
        UUID userId,
        UUID lessonId,
        Map<String, Object> payload,
        String accessToken
    );

    LessonProgressResponse getLessonProgress(UUID userId, UUID lessonId, String accessToken);

    LessonProgressResponse completeLessonSection(UUID userId, UUID lessonId, String sectionId, String accessToken);

    void enrollLesson(UUID userId, UUID lessonId, String accessToken);

    void updateLessonProgress(UUID userId, UUID lessonId, int progress, String accessToken);

    void saveLesson(UUID userId, UUID lessonId, String accessToken);

    Map<String, Integer> getUserLessonProgress(UUID userId, String accessToken);

    Map<String, Integer> getUserStats(UUID userId, String accessToken);
}

package com.rotiprata.api.lesson.service;

import com.rotiprata.api.admin.dto.AdminPublishLessonResponse;
import com.rotiprata.api.admin.dto.AdminStepSaveRequest;
import com.rotiprata.api.generalutils.EmbeddingService;
import com.rotiprata.api.lesson.utils.LessonFlowConstants;
import com.rotiprata.application.MediaProcessingService;
import com.rotiprata.infrastructure.supabase.SupabaseAdminRestClient;
import com.rotiprata.infrastructure.supabase.SupabaseRestClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    private static final String ACCESS_TOKEN = "token";

    @Mock
    private SupabaseRestClient supabaseRestClient;

    @Mock
    private SupabaseAdminRestClient supabaseAdminRestClient;

    @Mock
    private LessonQuizService lessonQuizService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private MediaProcessingService mediaProcessingService;

    private LessonService lessonService;
    private UUID adminUserId;
    private UUID lessonId;
    private UUID quizId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        lessonService = new LessonService(
            supabaseRestClient,
            supabaseAdminRestClient,
            lessonQuizService,
            embeddingService,
            mediaProcessingService
        );
        adminUserId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        when(supabaseAdminRestClient.getList(eq("user_roles"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(supabaseAdminRestClient.getList(eq("lesson_section_blocks"), anyString(), any()))
            .thenReturn(List.of());
    }

    @Test
    void createLesson_ShouldGenerateEmbedding_WhenLessonIsPublished() {
        // Published lesson creation should restore the original embedding behavior.

        // arrange
        Map<String, Object> payload = createLessonPayload(true);
        Map<String, Object> createdLesson = completeLesson(true);
        when(supabaseAdminRestClient.postList(eq("lessons"), any(), any()))
            .thenReturn(List.of(createdLesson));
        when(supabaseAdminRestClient.postList(eq("quizzes"), any(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.postList(eq("quiz_questions"), any(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[] {0.25f, 0.75f});
        when(embeddingService.toPgVector(any(float[].class))).thenReturn("[0.25,0.75]");

        // act
        Map<String, Object> result = lessonService.createLesson(adminUserId, payload, ACCESS_TOKEN);

        // assert
        assertEquals("[0.25,0.75]", result.get("embedding"));

        // verify
        verify(embeddingService).generateEmbedding(anyString());
        verify(supabaseAdminRestClient).patchList(
            eq("lessons"),
            anyString(),
            org.mockito.ArgumentMatchers.argThat(this::isEmbeddingPatch),
            any()
        );
    }

    @Test
    void createLesson_ShouldSkipEmbedding_WhenPublishFallsBackToDraft() {
        // Incomplete lesson content should downgrade publish to draft and skip embeddings.

        // arrange
        Map<String, Object> payload = createLessonPayload(true);
        payload.remove("summary");
        Map<String, Object> createdLesson = completeLesson(false);
        createdLesson.remove("summary");
        when(supabaseAdminRestClient.postList(eq("lessons"), any(), any()))
            .thenReturn(List.of(createdLesson));
        when(supabaseAdminRestClient.postList(eq("quizzes"), any(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.postList(eq("quiz_questions"), any(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));

        // act
        Map<String, Object> result = lessonService.createLesson(adminUserId, payload, ACCESS_TOKEN);

        // assert
        assertEquals(false, result.get("is_published"));

        // verify
        verifyNoInteractions(embeddingService);
        verify(supabaseAdminRestClient, never()).patchList(
            eq("lessons"),
            anyString(),
            org.mockito.ArgumentMatchers.argThat(this::isEmbeddingPatch),
            any()
        );
    }

    @Test
    void publishLessonWithValidation_ShouldGenerateEmbedding_WhenPublishSucceeds() {
        // Publishing a validated lesson should write an embedding just like the original stable flow.

        // arrange
        Map<String, Object> draftLesson = completeLesson(false);
        Map<String, Object> publishedLesson = completeLesson(true);
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(draftLesson), List.of(publishedLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(validQuestion()));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(publishedLesson));
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[] {0.3f, 0.7f});
        when(embeddingService.toPgVector(any(float[].class))).thenReturn("[0.3,0.7]");

        // act
        AdminPublishLessonResponse response = lessonService.publishLessonWithValidation(
            adminUserId,
            lessonId,
            new AdminStepSaveRequest(Map.of(), List.of()),
            ACCESS_TOKEN
        );

        // assert
        assertTrue(response.success());
        assertEquals("[0.3,0.7]", response.lessonSnapshot().get("embedding"));

        // verify
        verify(embeddingService).generateEmbedding(anyString());
        verify(supabaseAdminRestClient, times(2)).patchList(eq("lessons"), anyString(), any(), any());
    }

    @Test
    void publishLessonWithValidation_ShouldSkipEmbedding_WhenSkipEmbeddingIsTrue() {
        // The publish path should honor the skip flag without blocking the publish itself.

        // arrange
        Map<String, Object> draftLesson = completeLesson(false);
        Map<String, Object> publishedLesson = completeLesson(true);
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(draftLesson), List.of(publishedLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(validQuestion()));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(publishedLesson));

        // act
        AdminPublishLessonResponse response = lessonService.publishLessonWithValidation(
            adminUserId,
            lessonId,
            new AdminStepSaveRequest(Map.of("skip_embedding", true), List.of()),
            ACCESS_TOKEN
        );

        // assert
        assertTrue(response.success());
        assertFalse(response.lessonSnapshot().containsKey("embedding"));

        // verify
        verifyNoInteractions(embeddingService);
        verify(supabaseAdminRestClient, times(1)).patchList(eq("lessons"), anyString(), any(), any());
    }

    @Test
    void updateLesson_ShouldReembed_WhenPublishedLessonContentChanges() {
        // Editing embedding-relevant lesson text should refresh the vector for published lessons.

        // arrange
        Map<String, Object> existingLesson = completeLesson(true);
        Map<String, Object> updatedLesson = completeLesson(true);
        updatedLesson.put("description", "Updated description");
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(existingLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(updatedLesson));
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[] {0.4f, 0.6f});
        when(embeddingService.toPgVector(any(float[].class))).thenReturn("[0.4,0.6]");

        // act
        Map<String, Object> result = lessonService.updateLesson(
            adminUserId,
            lessonId,
            Map.of("description", "Updated description"),
            ACCESS_TOKEN
        );

        // assert
        assertEquals("[0.4,0.6]", result.get("embedding"));

        // verify
        verify(embeddingService).generateEmbedding(anyString());
        verify(supabaseAdminRestClient, times(2)).patchList(eq("lessons"), anyString(), any(), any());
    }

    @Test
    void updateLesson_ShouldReembed_WhenContentSectionsChangeOnPublishedLesson() {
        // Structured content edits should also refresh embeddings for published lessons.

        // arrange
        Map<String, Object> existingLesson = completeLesson(true);
        Map<String, Object> updatedLesson = completeLesson(true);
        updatedLesson.put("origin_content", "Fresh intro text");
        updatedLesson.put("definition_content", "Fresh definition");
        updatedLesson.put("usage_examples", List.of("Fresh usage"));
        updatedLesson.put("lore_content", "Fresh lore");
        updatedLesson.put("evolution_content", "Fresh evolution");
        updatedLesson.put("comparison_content", "Fresh comparison");
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(existingLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(updatedLesson));
        when(supabaseAdminRestClient.deleteList(eq("lesson_section_blocks"), anyString(), any()))
            .thenReturn(List.of());
        when(supabaseAdminRestClient.postList(eq("lesson_section_blocks"), any(), any()))
            .thenReturn(List.of());
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[] {0.1f, 0.9f});
        when(embeddingService.toPgVector(any(float[].class))).thenReturn("[0.1,0.9]");

        // act
        Map<String, Object> result = lessonService.updateLesson(
            adminUserId,
            lessonId,
            Map.of("content_sections", fullContentSections("Fresh")),
            ACCESS_TOKEN
        );

        // assert
        assertEquals("[0.1,0.9]", result.get("embedding"));

        // verify
        verify(supabaseAdminRestClient).deleteList(eq("lesson_section_blocks"), anyString(), any());
        verify(supabaseAdminRestClient).postList(eq("lesson_section_blocks"), any(), any());
        verify(embeddingService).generateEmbedding(anyString());
    }

    @Test
    void updateLesson_ShouldNotReembed_WhenOnlyNonContentFieldsChange() {
        // Non-content metadata edits should keep the existing embedding untouched.

        // arrange
        Map<String, Object> existingLesson = completeLesson(true);
        Map<String, Object> updatedLesson = completeLesson(true);
        updatedLesson.put("estimated_minutes", 15);
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(existingLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(updatedLesson));

        // act
        Map<String, Object> result = lessonService.updateLesson(
            adminUserId,
            lessonId,
            Map.of("estimated_minutes", 15),
            ACCESS_TOKEN
        );

        // assert
        assertEquals(15, result.get("estimated_minutes"));

        // verify
        verifyNoInteractions(embeddingService);
        verify(supabaseAdminRestClient, times(1)).patchList(eq("lessons"), anyString(), any(), any());
    }

    @Test
    void updateLesson_ShouldNotReembed_WhenLessonIsDraft() {
        // Draft lessons should remain non-embedded even when content fields change.

        // arrange
        Map<String, Object> existingLesson = completeLesson(false);
        Map<String, Object> updatedLesson = completeLesson(false);
        updatedLesson.put("description", "Draft update");
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(existingLesson));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(updatedLesson));

        // act
        Map<String, Object> result = lessonService.updateLesson(
            adminUserId,
            lessonId,
            Map.of("description", "Draft update"),
            ACCESS_TOKEN
        );

        // assert
        assertEquals("Draft update", result.get("description"));

        // verify
        verifyNoInteractions(embeddingService);
        verify(supabaseAdminRestClient, times(1)).patchList(eq("lessons"), anyString(), any(), any());
    }

    @Test
    void updateLesson_ShouldSkipReembed_WhenSkipEmbeddingIsTrue() {
        // The skip flag should suppress re-embedding even when published content changes.

        // arrange
        Map<String, Object> existingLesson = completeLesson(true);
        Map<String, Object> updatedLesson = completeLesson(true);
        updatedLesson.put("description", "Skip this embedding");
        when(supabaseAdminRestClient.getList(eq("lessons"), anyString(), any()))
            .thenReturn(List.of(existingLesson));
        when(supabaseAdminRestClient.getList(eq("quizzes"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", quizId.toString())));
        when(supabaseAdminRestClient.getList(eq("quiz_questions"), anyString(), any()))
            .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        when(supabaseAdminRestClient.patchList(eq("lessons"), anyString(), any(), any()))
            .thenReturn(List.of(updatedLesson));

        // act
        Map<String, Object> result = lessonService.updateLesson(
            adminUserId,
            lessonId,
            Map.of("description", "Skip this embedding", "skip_embedding", true),
            ACCESS_TOKEN
        );

        // assert
        assertEquals("Skip this embedding", result.get("description"));

        // verify
        verifyNoInteractions(embeddingService);
        verify(supabaseAdminRestClient, times(1)).patchList(eq("lessons"), anyString(), any(), any());
    }

    private Map<String, Object> createLessonPayload(boolean publish) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Roti Basics");
        payload.put("summary", "Summary");
        payload.put("description", "Description");
        payload.put("learning_objectives", List.of("Understand roti"));
        payload.put("estimated_minutes", 10);
        payload.put("xp_reward", 25);
        payload.put("badge_name", "Roti Starter");
        payload.put("difficulty_level", 2);
        payload.put("category_id", categoryId.toString());
        payload.put("origin_content", "Origin");
        payload.put("definition_content", "Definition");
        payload.put("usage_examples", List.of("Usage"));
        payload.put("lore_content", "Lore");
        payload.put("evolution_content", "Evolution");
        payload.put("comparison_content", "Comparison");
        payload.put("is_published", publish);
        payload.put("questions", List.of(validQuestion()));
        return payload;
    }

    private Map<String, Object> completeLesson(boolean published) {
        Map<String, Object> lesson = new LinkedHashMap<>();
        lesson.put("id", lessonId.toString());
        lesson.put("title", "Roti Basics");
        lesson.put("summary", "Summary");
        lesson.put("description", "Description");
        lesson.put("learning_objectives", List.of("Understand roti"));
        lesson.put("estimated_minutes", 10);
        lesson.put("xp_reward", 25);
        lesson.put("badge_name", "Roti Starter");
        lesson.put("difficulty_level", 2);
        lesson.put("category_id", categoryId.toString());
        lesson.put("origin_content", "Origin");
        lesson.put("definition_content", "Definition");
        lesson.put("usage_examples", List.of("Usage"));
        lesson.put("lore_content", "Lore");
        lesson.put("evolution_content", "Evolution");
        lesson.put("comparison_content", "Comparison");
        lesson.put("is_published", published);
        lesson.put("is_active", true);
        return lesson;
    }

    private Map<String, Object> validQuestion() {
        Map<String, Object> question = new LinkedHashMap<>();
        question.put("question_type", "multiple_choice");
        question.put("question_text", "What is roti prata?");
        question.put("explanation", "It is a flatbread.");
        question.put("points", 10);
        question.put("correct_answer", "A");
        question.put("options", Map.of("choices", Map.of("A", "Flatbread", "B", "Noodle")));
        return question;
    }

    private List<Map<String, Object>> fullContentSections(String prefix) {
        return List.of(
            textSection(LessonFlowConstants.SECTION_INTRO, prefix + " intro"),
            textSection(LessonFlowConstants.SECTION_DEFINITION, prefix + " definition"),
            textSection(LessonFlowConstants.SECTION_USAGE, prefix + " usage"),
            textSection(LessonFlowConstants.SECTION_LORE, prefix + " lore"),
            textSection(LessonFlowConstants.SECTION_EVOLUTION, prefix + " evolution"),
            textSection(LessonFlowConstants.SECTION_COMPARISON, prefix + " comparison")
        );
    }

    private Map<String, Object> textSection(String sectionKey, String text) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("blockType", "text");
        block.put("textContent", text);

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("sectionKey", sectionKey);
        section.put("blocks", List.of(block));
        return section;
    }

    private boolean isEmbeddingPatch(Object body) {
        return body instanceof Map<?, ?> map && map.containsKey("embedding");
    }
}

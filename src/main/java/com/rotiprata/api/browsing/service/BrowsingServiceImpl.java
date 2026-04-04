package com.rotiprata.api.browsing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rotiprata.api.browsing.dto.ContentSearchDTO;
import com.rotiprata.api.browsing.dto.GetHistoryDTO;
import com.rotiprata.api.browsing.dto.SaveHistoryDTO;
import com.rotiprata.api.content.service.ContentService;
import com.rotiprata.api.lesson.service.LessonService;
import com.rotiprata.infrastructure.supabase.SupabaseRestClient;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of BrowsingService.
 * Handles search operations and manages search history using Supabase.
 */
@Service
public class BrowsingServiceImpl implements BrowsingService {

    private final ContentService contentService;
    private final LessonService lessonService;
    private final SupabaseRestClient supabaseRestClient;

    /**
     * Constructor for dependency injection.
     */
    public BrowsingServiceImpl(
            ContentService contentService,
            LessonService lessonService,
            SupabaseRestClient supabaseRestClient
    ) {
        this.contentService = contentService;
        this.lessonService = lessonService;
        this.supabaseRestClient = supabaseRestClient;
    }

    // ================= SEARCH =================

    @Override
    public List<ContentSearchDTO> search(String query, String filter, String accessToken) {
        List<ContentSearchDTO> results = new ArrayList<>();
        String normalizedFilter = filter == null ? "" : filter.trim().toLowerCase();

        if (normalizedFilter.isBlank()) {
            results.addAll(contentService.getFilteredContent(query, null, accessToken));
            results.addAll(mapLessonsToSearchResults(
                    lessonService.searchLessons(query, accessToken)
            ));
            return results;
        }

        if ("video".equals(normalizedFilter)) {
            results.addAll(contentService.getFilteredContent(query, "video", accessToken));
            return results;
        }

        if ("lesson".equals(normalizedFilter)) {
            results.addAll(mapLessonsToSearchResults(
                    lessonService.searchLessons(query, accessToken)
            ));
        }

        return results;
    }

    // ================= SAVE HISTORY =================

    @Override
    public void saveHistory(String userId, String query, Instant searchedAt, String accessToken) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) return;

        SaveHistoryDTO dto = new SaveHistoryDTO();
        dto.setUserId(userId);
        dto.setQuery(normalizedQuery);
        dto.setSearchedAt(searchedAt != null ? searchedAt : Instant.now());

        String existingQuery = UriComponentsBuilder.newInstance()
                .queryParam("user_id", "eq." + userId)
                .queryParam("query", "eq." + normalizedQuery)
                .queryParam("order", "searched_at.desc")
                .queryParam("limit", "1")
                .build()
                .encode()
                .toUriString()
                .replaceFirst("^\\?", "");

        List<Map<String, Object>> existing = supabaseRestClient.getList(
                "search_history",
                existingQuery,
                accessToken,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        // Update existing entry timestamp if found
        if (!existing.isEmpty()) {
            String id = existing.get(0).get("id") == null ? null : existing.get(0).get("id").toString();
            if (id != null && !id.isBlank()) {
                supabaseRestClient.patchList(
                        "search_history",
                        UriComponentsBuilder.newInstance()
                                .queryParam("id", "eq." + id)
                                .queryParam("user_id", "eq." + userId)
                                .build()
                                .encode()
                                .toUriString()
                                .replaceFirst("^\\?", ""),
                        Map.of("searched_at", dto.getSearchedAt()),
                        accessToken,
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                return;
            }
        }

        // Insert new history entry if none exists
        supabaseRestClient.postList(
                "search_history",
                dto,
                accessToken,
                new TypeReference<List<Map<String, Object>>>() {}
        );
    }

    // ================= FETCH HISTORY =================

    @Override
    public List<GetHistoryDTO> fetchHistory(String userId, String accessToken) {
        String query = "user_id=eq." + userId + "&order=searched_at.desc&limit=5";
        return supabaseRestClient.getList(
                "search_history",
                query,
                accessToken,
                new TypeReference<List<GetHistoryDTO>>() {}
        );
    }

    // ================= CLEAR HISTORY =================

    @Override
    public void deleteHistoryById(String id, String userId, String accessToken) {
        if (id == null || id.isBlank()) return;

        String query = "id=eq." + id + "&user_id=eq." + userId;
        supabaseRestClient.deleteList(
                "search_history",
                query,
                accessToken,
                new TypeReference<List<Map<String, Object>>>() {}
        );
    }

    // ================= INTERNAL HELPERS =================

    /**
     * Converts raw lesson maps to ContentSearchDTO objects.
     */
    private List<ContentSearchDTO> mapLessonsToSearchResults(List<Map<String, Object>> lessons) {
        List<ContentSearchDTO> results = new ArrayList<>();
        for (Map<String, Object> lesson : lessons) {
            String id = toStringValue(lesson.get("id"));
            String title = toStringValue(lesson.get("title"));
            String description = toStringValue(lesson.get("description"));

            results.add(new ContentSearchDTO(
                    id,
                    "lesson",
                    title,
                    description,
                    buildSnippet(description)
            ));
        }
        return results;
    }

    /**
     * Truncates description to a snippet of max 100 characters.
     */
    private String buildSnippet(String description) {
        if (description == null) return null;
        return description.length() > 100
                ? description.substring(0, 100) + "..."
                : description;
    }

    /**
     * Safely converts an object to string.
     */
    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
package com.rotiprata.api.content.service;

import com.rotiprata.api.browsing.dto.ContentSearchDTO;
import com.rotiprata.api.content.dto.ContentCommentCreateRequest;
import com.rotiprata.api.content.dto.ContentCommentResponse;
import com.rotiprata.api.content.dto.ContentFlagRequest;
import com.rotiprata.api.content.dto.ContentPlaybackEventRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ContentService {

    Map<String, Object> getContentById(UUID userId, UUID contentId, String accessToken);

    List<Map<String, Object>> getSimilarContent(UUID userId, UUID contentId, String accessToken, Integer limit);

    List<Map<String, Object>> getProfileContentCollection(UUID userId, String accessToken, String collection);

    List<ContentSearchDTO> getFilteredContent(String query, String filter, String accessToken);

    void trackView(UUID userId, UUID contentId);

    void recordPlaybackEvent(UUID userId, UUID contentId, ContentPlaybackEventRequest request);

    void likeContent(UUID userId, UUID contentId, String accessToken);

    void unlikeContent(UUID userId, UUID contentId, String accessToken);

    void saveContent(UUID userId, UUID contentId, String accessToken);

    void unsaveContent(UUID userId, UUID contentId, String accessToken);

    void shareContent(UUID userId, UUID contentId, String accessToken);

    void flagContent(UUID userId, UUID contentId, ContentFlagRequest request, String accessToken);

    List<ContentCommentResponse> listComments(
        UUID userId,
        UUID contentId,
        int limit,
        int offset,
        String accessToken
    );

    ContentCommentResponse createComment(
        UUID userId,
        UUID contentId,
        ContentCommentCreateRequest request,
        String accessToken
    );

    void deleteComment(UUID userId, UUID contentId, UUID commentId, String accessToken);

    List<Map<String, Object>> getFlaggedContentByMonthAndYear(String accessToken, String month, String year);
}

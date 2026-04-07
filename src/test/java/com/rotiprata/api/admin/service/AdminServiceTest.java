package com.rotiprata.api.admin.service;

import com.rotiprata.api.content.service.ContentCreatorEnrichmentService;
import com.rotiprata.api.content.service.ContentService;
import com.rotiprata.api.user.service.UserService;
import com.rotiprata.domain.AppRole;
import com.rotiprata.infrastructure.supabase.SupabaseAdminClient;
import com.rotiprata.infrastructure.supabase.SupabaseAdminRestClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService getFlagReviewByContent tests")
class AdminServiceTest {

    @Mock
    private SupabaseAdminClient supabaseAdminClient;

    @Mock
    private SupabaseAdminRestClient supabaseAdminRestClient;

    @Mock
    private ContentCreatorEnrichmentService contentCreatorEnrichmentService;

    @Mock
    private ContentService contentService;

    @Mock
    private UserService userService;

    @Mock
    private AdminLoggingService adminLoggingService;

    private AdminService adminService;
    private UUID adminUserId;
    private UUID contentId;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(
            supabaseAdminClient,
            supabaseAdminRestClient,
            contentCreatorEnrichmentService,
            contentService,
            userService,
            adminLoggingService
        );
        adminUserId = UUID.randomUUID();
        contentId = UUID.randomUUID();

        lenient().when(userService.getRoles(adminUserId, "token")).thenReturn(List.of(AppRole.ADMIN));
        lenient().when(contentCreatorEnrichmentService.enrichWithCreatorProfiles(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // Verifies actionable review details are produced when selected period has a pending report.
    @Test
    void getFlagReviewByContent_ShouldReturnPendingReview_WhenSelectedMonthHasPendingFlag() {
        // arrange
        UUID pendingFlagId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        when(supabaseAdminRestClient.getList(eq("content_flags"), anyString(), any())).thenReturn(List.of(
            Map.of(
                "id", pendingFlagId.toString(),
                "content_id", contentId.toString(),
                "status", "pending",
                "reported_by", reporterId.toString(),
                "reason", "Spam",
                "description", "Needs action",
                "created_at", "2026-04-10T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            ),
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "resolved",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Spam",
                "description", " ",
                "created_at", "2026-04-09T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            )
        ));

        // act
        Map<String, Object> review =
            adminService.getFlagReviewByContent(adminUserId, contentId, 4, 2026, "token");

        // assert
        assertEquals(contentId, review.get("contentId"));
        assertEquals("pending", review.get("status"));
        assertEquals(2, review.get("reportCount"));
        assertEquals(1, review.get("notesCount"));
        assertEquals(List.of("Spam"), review.get("reasons"));
        assertEquals("2026-04-10T12:00:00Z", review.get("latestReportAt"));
        assertEquals(pendingFlagId.toString(), review.get("actionableFlagId"));
        assertTrue((Boolean) review.get("canResolve"));
        assertTrue((Boolean) review.get("canTakeDown"));
        assertNotNull(review.get("content"));

        // verify
        verify(contentCreatorEnrichmentService, times(1)).enrichWithCreatorProfiles(any());
        verify(supabaseAdminRestClient, times(1)).getList(eq("content_flags"), anyString(), any());
    }

    // Verifies review is read-only when selected period has only resolved reports.
    @Test
    void getFlagReviewByContent_ShouldReturnResolvedReview_WhenSelectedMonthHasNoPendingFlag() {
        // arrange
        when(supabaseAdminRestClient.getList(eq("content_flags"), anyString(), any())).thenReturn(List.of(
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "resolved",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Abuse",
                "description", "Handled",
                "created_at", "2026-04-10T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            ),
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "resolved",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Spam",
                "description", "  ",
                "created_at", "2026-04-09T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            )
        ));

        // act
        Map<String, Object> review =
            adminService.getFlagReviewByContent(adminUserId, contentId, 4, 2026, "token");

        // assert
        assertEquals("resolved", review.get("status"));
        assertNull(review.get("actionableFlagId"));
        assertFalse((Boolean) review.get("canResolve"));
        assertFalse((Boolean) review.get("canTakeDown"));
        assertEquals(List.of("Abuse", "Spam"), review.get("reasons"));

        // verify
        verify(contentCreatorEnrichmentService, times(1)).enrichWithCreatorProfiles(any());
    }

    // Verifies period filtering returns not found when no row is in the requested month/year.
    @Test
    void getFlagReviewByContent_ShouldThrowNotFound_WhenNoFlagsMatchSelectedMonth() {
        // arrange
        when(supabaseAdminRestClient.getList(eq("content_flags"), anyString(), any())).thenReturn(List.of(
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "pending",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Spam",
                "description", "Out of scope",
                "created_at", "2026-05-01T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            )
        ));

        // act
        ResponseStatusException thrown = assertThrows(
            ResponseStatusException.class,
            () -> adminService.getFlagReviewByContent(adminUserId, contentId, 4, 2026, "token")
        );

        // assert
        assertEquals(404, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("Flag review not found"));

        // verify
        verify(contentCreatorEnrichmentService, never()).enrichWithCreatorProfiles(any());
    }

    // Verifies null month/year mode falls back to pending-only rows.
    @Test
    void getFlagReviewByContent_ShouldFilterPendingOnly_WhenMonthAndYearAreNull() {
        // arrange
        UUID pendingFlagId = UUID.randomUUID();
        when(supabaseAdminRestClient.getList(eq("content_flags"), anyString(), any())).thenReturn(List.of(
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "resolved",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Abuse",
                "description", "Already resolved",
                "created_at", "2026-05-02T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            ),
            Map.of(
                "id", pendingFlagId.toString(),
                "content_id", contentId.toString(),
                "status", "pending",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Spam",
                "description", "Pending only",
                "created_at", "2026-04-01T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            )
        ));

        // act
        Map<String, Object> review =
            adminService.getFlagReviewByContent(adminUserId, contentId, null, null, "token");

        // assert
        assertEquals("pending", review.get("status"));
        assertEquals(1, review.get("reportCount"));
        assertEquals(pendingFlagId.toString(), review.get("actionableFlagId"));

        // verify
        verify(contentCreatorEnrichmentService, times(1)).enrichWithCreatorProfiles(any());
    }

    // Verifies null month/year mode throws not found when no pending row exists.
    @Test
    void getFlagReviewByContent_ShouldThrowNotFound_WhenMonthAndYearAreNullAndNoPendingFlags() {
        // arrange
        when(supabaseAdminRestClient.getList(eq("content_flags"), anyString(), any())).thenReturn(List.of(
            Map.of(
                "id", UUID.randomUUID().toString(),
                "content_id", contentId.toString(),
                "status", "resolved",
                "reported_by", UUID.randomUUID().toString(),
                "reason", "Spam",
                "description", "Resolved only",
                "created_at", "2026-04-01T12:00:00Z",
                "content", Map.of("id", contentId.toString(), "title", "Roti")
            )
        ));

        // act
        ResponseStatusException thrown = assertThrows(
            ResponseStatusException.class,
            () -> adminService.getFlagReviewByContent(adminUserId, contentId, null, null, "token")
        );

        // assert
        assertEquals(404, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("Flag review not found"));

        // verify
        verify(contentCreatorEnrichmentService, never()).enrichWithCreatorProfiles(any());
    }

    // Verifies bad request is returned when month and year are not provided together.
    @Test
    void getFlagReviewByContent_ShouldThrowBadRequest_WhenOnlyMonthIsProvided() {
        // act
        ResponseStatusException thrown = assertThrows(
            ResponseStatusException.class,
            () -> adminService.getFlagReviewByContent(adminUserId, contentId, 4, null, "token")
        );

        // assert
        assertEquals(400, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("Month and year are required together"));

        // verify
        verify(supabaseAdminRestClient, never()).getList(eq("content_flags"), anyString(), any());
    }

    // Verifies bad request is returned when the provided month is out of range.
    @Test
    void getFlagReviewByContent_ShouldThrowBadRequest_WhenMonthIsOutOfRange() {
        // act
        ResponseStatusException thrown = assertThrows(
            ResponseStatusException.class,
            () -> adminService.getFlagReviewByContent(adminUserId, contentId, 13, 2026, "token")
        );

        // assert
        assertEquals(400, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("Month must be between 1 and 12"));

        // verify
        verify(supabaseAdminRestClient, never()).getList(eq("content_flags"), anyString(), any());
    }

    // Verifies non-admin users cannot access review endpoints.
    @Test
    void getFlagReviewByContent_ShouldThrowForbidden_WhenUserIsNotAdmin() {
        // arrange
        UUID nonAdminUserId = UUID.randomUUID();
        when(userService.getRoles(nonAdminUserId, "token")).thenReturn(List.of(AppRole.USER));

        // act
        ResponseStatusException thrown = assertThrows(
            ResponseStatusException.class,
            () -> adminService.getFlagReviewByContent(nonAdminUserId, contentId, 4, 2026, "token")
        );

        // assert
        assertEquals(403, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("Admin role required"));

        // verify
        verify(supabaseAdminRestClient, never()).getList(eq("content_flags"), anyString(), any());
    }
}
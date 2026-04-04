package com.rotiprata.api.admin.service;

import com.rotiprata.api.admin.service.AdminLoggingService.AdminAction;
import com.rotiprata.api.admin.service.AdminLoggingService.TargetType;

import com.rotiprata.infrastructure.supabase.SupabaseAdminRestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("AdminLoggingServiceImpl Unit Tests - Cleaner Version")
class AdminLoggingServiceImplTest {

    private static final TypeReference<List<Map<String, Object>>> MAP_LIST = new TypeReference<>() {};

    @Mock
    private SupabaseAdminRestClient supabaseAdminRestClient;

    @InjectMocks
    private AdminLoggingServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logAdminAction_ShouldCallPostList_WithCorrectParameters() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AdminAction action = AdminAction.DELETE_CONTENT;
        TargetType targetType = TargetType.CONTENT;
        String description = "Removed inappropriate content";

        // Act
        service.logAdminAction(adminId, action, targetId, targetType, description);

        // Capture the argument passed to postList
        ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(supabaseAdminRestClient, times(1)).postList(eq("audit_logs"), captor.capture(), eq(MAP_LIST));

        List<Map<String, Object>> capturedRows = captor.getValue();
        assertEquals(1, capturedRows.size());

        Map<String, Object> entry = capturedRows.get(0);
        assertEquals(adminId, entry.get("admin_id"));
        assertEquals(action.name(), entry.get("action"));
        assertEquals(targetId, entry.get("target_id"));
        assertEquals(targetType.name(), entry.get("target_type"));
        assertEquals(description, entry.get("description"));
        assertTrue(entry.get("created_at") instanceof OffsetDateTime);
    }
}
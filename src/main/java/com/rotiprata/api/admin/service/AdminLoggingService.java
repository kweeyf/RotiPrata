package com.rotiprata.api.admin.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rotiprata.infrastructure.supabase.SupabaseAdminRestClient;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class AdminLoggingService {

    private final SupabaseAdminRestClient supabaseAdminRestClient;
    private static final TypeReference<List<Map<String, Object>>> MAP_LIST = new TypeReference<>() {};

    public enum AdminAction {
        // User Related
        SUSPEND_USER,
        UPDATE_USER_ROLE,
        UPDATE_USER_STATUS,
        RESET_USER_LESSON_PROGRESS,

        // Content Related
        APPROVE_CONTENT,
        UPDATE_CONTENT,
        REJECT_CONTENT,
        DELETE_CONTENT,

        // Flag Related
        TAKE_DOWN_CONTENT, 
        RESOLVE_FLAG,
    }

    public enum TargetType {
        USER,
        CONTENT,
        FLAG,
    }

    public AdminLoggingService(SupabaseAdminRestClient supabaseAdminRestClient) {
        this.supabaseAdminRestClient = supabaseAdminRestClient;
    }

    public void logAdminAction(
            UUID adminId,
            AdminAction action,
            UUID targetId,
            TargetType targetType,
            String description
    ) {
        Map<String, Object> logEntry = Map.of(
                "admin_id", adminId,
                "action", action.name(),
                "target_id", targetId,
                "target_type", targetType.name(),
                "description", description,
                "created_at", OffsetDateTime.now()
        );

        List<Map<String, Object>> rows = List.of(logEntry);

        supabaseAdminRestClient.postList("audit_logs", rows, MAP_LIST);
    }
}
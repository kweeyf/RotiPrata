package com.rotiprata.api.admin.response;

public record AdminStatsResponse(
    int totalUsers,
    int activeUsers,
    int totalContent,
    int pendingModeration,
    int totalLessons,
    int contentApprovalRate
) {}

package com.rotiprata.api.lesson.response;

import java.util.UUID;

public record LessonMediaStartResponse(UUID assetId, String status, String pollUrl) {}

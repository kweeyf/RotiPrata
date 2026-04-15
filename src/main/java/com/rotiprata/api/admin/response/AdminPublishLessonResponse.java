package com.rotiprata.api.admin.response;

import com.rotiprata.api.admin.validation.AdminValidationError;

import java.util.List;
import java.util.Map;

public record AdminPublishLessonResponse(
    boolean success,
    String firstInvalidStep,
    List<AdminValidationError> errors,
    Map<String, Object> lessonSnapshot
) {}

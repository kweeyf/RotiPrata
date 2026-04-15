package com.rotiprata.api.admin.validation;

public record AdminValidationError(
    String step,
    String fieldPath,
    String message,
    Integer questionIndex
) {}

package com.rotiprata.api.content.response;

public record ContentQuizSubmitResponse(
    int score,
    int maxScore,
    double percentage,
    boolean passed
) {}

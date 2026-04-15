package com.rotiprata.api.content.response;

public record ContentMediaStatusResponse(
    String status,
    String hlsUrl,
    String thumbnailUrl,
    String errorMessage
) {}

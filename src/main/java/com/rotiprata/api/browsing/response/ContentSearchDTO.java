package com.rotiprata.api.browsing.response;

public record ContentSearchDTO(
    String id,
    String content_type,
    String title,
    String description,
    String snippet
) {}

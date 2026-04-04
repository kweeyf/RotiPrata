package com.rotiprata.api.content.dto;

public record ContentSearchDTO(
    String id,
    String content_type,
    String title,
    String description,
    String snippet
) {}

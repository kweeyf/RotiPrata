package com.rotiprata.api.content.request;

import jakarta.validation.constraints.NotBlank;

public record ContentMediaStartLinkRequest(@NotBlank String sourceUrl) {}

package com.rotiprata.api.content.response;

import java.util.UUID;

public record ContentMediaStartResponse(UUID contentId, String status, String pollUrl) {}

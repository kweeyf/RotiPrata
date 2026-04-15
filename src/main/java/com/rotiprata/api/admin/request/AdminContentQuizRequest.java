package com.rotiprata.api.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AdminContentQuizRequest(
    @JsonProperty("questions") List<AdminContentQuizQuestionRequest> questions
) {}

package com.rotiprata.application;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.openai.OpenAIClient;

@Service
public class ChatService {

    private final OpenAIClient openAIClient;
    private final LessonService lessonService;

    @Autowired
    public ChatService(OpenAIClient openAIClient, LessonService lessonService) {
        this.openAIClient = openAIClient;
        this.lessonService = lessonService;
    }

    public String ask(String question) {

        String context = lessonService.findRelevantLesson(question);

        // Combine into a prompt
        String prompt = """
                Answer the question using the context.
                If the answer is not in the context, say "I don't know".

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        return openAIClient.completion(request -> {
            request.model("gpt-4o-mini"); 
            request.prompt(prompt);
        }).block().getText(); 
    }
}
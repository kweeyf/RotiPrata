package com.rotiprata.application;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rotiprata.infrastructure.supabase.SupabaseRestClient;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import com.fasterxml.jackson.core.type.TypeReference;

import com.rotiprata.api.dto.ChatbotMessageDTO;

@Service
public class ChatService {

    private final OpenAiChatModel openAiChatModel;
    private final LessonService lessonService;
    private final SupabaseRestClient supabaseRestClient;

    public ChatService(OpenAiChatModel openAiChatModel, LessonService lessonService, SupabaseRestClient supabaseRestClient) {
        this.openAiChatModel = openAiChatModel;
        this.lessonService = lessonService;
        this.supabaseRestClient = supabaseRestClient;
    }
  
    public String ask(String accessToken, String question) {

        saveMessages(accessToken, question, "user");

        System.out.println(question);
        String context = lessonService.findRelevantLesson(question);
        
        System.out.println("===== QUESTION =====");
        System.out.println(question);

        System.out.println("===== CONTEXT =====");
        System.out.println(context);

        System.out.println("====================");

        String prompt = """
            You are a learning assistant.

            Answer the question ONLY using the provided context.
            If the answer is not in the context, reply with "I don't know".
            Always be positive and supportive.

            Context:
            %s

            Question:
            %s
            """.formatted(context, question);
    
        String result = openAiChatModel.call(new Prompt(new UserMessage(prompt)))
                        .getResult()
                        .getOutput()
                        // to check if the below is correct
                        .getText();

        saveMessages(accessToken, result, "assistant");

        System.out.println(result);
        return result;
    }

    public void saveMessages(String accessToken, String message, String role) {

        ChatbotMessageDTO dto = new ChatbotMessageDTO(
            role,
            message, 
            Instant.now()
        );

        List<ChatbotMessageDTO> messages = List.of(dto);

        supabaseRestClient.postList(
            "user_chatbot_history",
            messages,
            accessToken,
            new TypeReference<List<ChatbotMessageDTO>>() {}
        );
    }

    public List<ChatbotMessageDTO> getHistory(String accessToken, String userId) {

        String query = "user_id=eq." + userId + "&order=timestamp.asc";

        return supabaseRestClient.getList(
            "user_chatbot_history",
            query,
            accessToken,
            new TypeReference<List<ChatbotMessageDTO>>() {}
        );
    }
}
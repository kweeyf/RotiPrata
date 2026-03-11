package com.rotiprata.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.rotiprata.application.ChatService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@AuthenticationPrincipal Jwt jwt , @RequestBody String question) {
        String accessToken = jwt.getTokenValue();
        return chatService.ask(accessToken, question);
    }
    
}

package com.example.api_project.controller;

import com.example.api_project.dto.ApiResponse;
import com.example.api_project.dto.chat.ChatRequest;
import com.example.api_project.dto.chat.ChatResponse;
import com.example.api_project.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatBotService chatBotService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> ask(@RequestBody ChatRequest request) {
        String reply = chatBotService.reply(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply)));
    }
}
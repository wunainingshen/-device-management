package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.entity.ChatMessage;
import com.devicemanagement.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/conversation/{friendId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getConversation(Authentication authentication,
                                                                           @PathVariable Long friendId) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(chatService.getConversation(userId, friendId)));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getUnread(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(chatService.getUnreadMessages(userId)));
    }

    @PostMapping("/read/{friendId}")
    public ResponseEntity<ApiResponse<Void>> markAsRead(Authentication authentication,
                                                         @PathVariable Long friendId) {
        Long userId = (Long) authentication.getCredentials();
        chatService.markAsRead(userId, friendId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/unread-counts")
    public ResponseEntity<ApiResponse<Map<Long, Long>>> getUnreadCounts(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(chatService.getUnreadCounts(userId)));
    }
}

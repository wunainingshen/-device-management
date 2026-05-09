package com.devicemanagement.service;

import com.devicemanagement.entity.ChatMessage;
import com.devicemanagement.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getConversation(Long userId, Long friendId) {
        return chatMessageRepository.findConversation(userId, friendId);
    }

    public List<ChatMessage> getUnreadMessages(Long userId) {
        return chatMessageRepository.findUnreadMessages(userId);
    }

    @Transactional
    public int markAsRead(Long userId, Long friendId) {
        return chatMessageRepository.markAsRead(userId, friendId);
    }

    public Map<Long, Long> getUnreadCounts(Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(userId);
        Map<Long, Long> counts = new HashMap<>();
        for (ChatMessage msg : unreadMessages) {
            counts.merge(msg.getSenderId(), 1L, Long::sum);
        }
        return counts;
    }
}

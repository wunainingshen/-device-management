package com.devicemanagement.handler;

import com.devicemanagement.entity.ChatMessage;
import com.devicemanagement.entity.User;
import com.devicemanagement.repository.BlacklistRepository;
import com.devicemanagement.repository.ChatMessageRepository;
import com.devicemanagement.repository.UserRepository;
import com.devicemanagement.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    // userId -> WebSocketSession
    private static final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();
    // sessionId -> userId
    private static final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineUsers.put(userId, session);
            sessionUserMap.put(session.getId(), userId);
            updateUserOnlineStatus(userId, true);
            broadcastUserStatus(userId, true);
            log.debug("User {} connected", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) return;

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.getOrDefault("type", "CHAT");

        switch (type) {
            case "CHAT":
                handleChatMessage(senderId, payload);
                break;
            case "MARK_READ":
                handleMarkRead(senderId, payload);
                break;
            case "TYPING":
                handleTyping(senderId, payload);
                break;
            default:
                break;
        }
    }

    private void handleChatMessage(Long senderId, Map<String, Object> payload) throws IOException {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = (String) payload.get("content");

        // Check if sender is blocked by receiver
        if (blacklistRepository.existsByUserIdAndBlockedUserId(receiverId, senderId)) {
            WebSocketSession senderSession = onlineUsers.get(senderId);
            if (senderSession != null && senderSession.isOpen()) {
                Map<String, Object> errorMsg = Map.of(
                    "type", "ERROR",
                    "message", "您已被对方拉黑，无法发送消息"
                );
                senderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
            }
            return;
        }

        // Check if receiver is blocked by sender - still allow sending but won't be delivered
        boolean isBlocked = blacklistRepository.existsByUserIdAndBlockedUserId(senderId, receiverId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setMessageType("TEXT");
        chatMessage.setIsRead(false);
        chatMessage.setSendTime(LocalDateTime.now());
        chatMessage = chatMessageRepository.save(chatMessage);

        // Send to receiver if online and not blocked
        if (!isBlocked) {
            WebSocketSession receiverSession = onlineUsers.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                Map<String, Object> msg = buildMessageResponse(chatMessage);
                receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        }

        // Send acknowledgment to sender
        WebSocketSession senderSession = onlineUsers.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            Map<String, Object> msg = buildMessageResponse(chatMessage);
            msg.put("type", "CHAT_ACK");
            senderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    private void handleMarkRead(Long userId, Map<String, Object> payload) {
        Long friendId = Long.valueOf(payload.get("friendId").toString());
        int updated = chatService.markAsRead(userId, friendId);

        // Notify the sender that messages were read
        WebSocketSession friendSession = onlineUsers.get(friendId);
        if (friendSession != null && friendSession.isOpen()) {
            try {
                Map<String, Object> readReceipt = Map.of(
                    "type", "MESSAGE_READ",
                    "readBy", userId,
                    "timestamp", LocalDateTime.now().toString()
                );
                friendSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(readReceipt)));
            } catch (IOException e) {
                log.error("Failed to send read receipt", e);
            }
        }
    }

    private void handleTyping(Long senderId, Map<String, Object> payload) throws IOException {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        boolean isTyping = (boolean) payload.getOrDefault("isTyping", false);

        WebSocketSession receiverSession = onlineUsers.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            Map<String, Object> typingMsg = Map.of(
                "type", "TYPING",
                "userId", senderId,
                "isTyping", isTyping
            );
            receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(typingMsg)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            onlineUsers.remove(userId);
            updateUserOnlineStatus(userId, false);
            broadcastUserStatus(userId, false);
            log.debug("User {} disconnected", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "userId".equals(pair[0])) {
                    return Long.parseLong(pair[1]);
                }
            }
        }
        return null;
    }

    private void updateUserOnlineStatus(Long userId, boolean online) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(online);
            if (!online) {
                user.setLastOnlineTime(LocalDateTime.now());
            }
            userRepository.save(user);
        });
    }

    private void broadcastUserStatus(Long userId, boolean online) {
        Map<String, Object> statusMsg = Map.of(
            "type", "USER_STATUS",
            "userId", userId,
            "online", online,
            "timestamp", LocalDateTime.now().toString()
        );

        onlineUsers.forEach((id, session) -> {
            if (session.isOpen() && !id.equals(userId)) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(statusMsg)));
                } catch (IOException e) {
                    log.error("Failed to broadcast user status", e);
                }
            }
        });
    }

    private Map<String, Object> buildMessageResponse(ChatMessage msg) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("type", "CHAT");
        result.put("id", msg.getId());
        result.put("senderId", msg.getSenderId());
        result.put("receiverId", msg.getReceiverId());
        result.put("content", msg.getContent());
        result.put("sendTime", msg.getSendTime() != null ? msg.getSendTime().toString() : LocalDateTime.now().toString());
        result.put("isRead", msg.getIsRead());
        return result;
    }

    public static boolean isUserOnline(Long userId) {
        WebSocketSession session = onlineUsers.get(userId);
        return session != null && session.isOpen();
    }

    public static Map<Long, WebSocketSession> getOnlineUsers() {
        return onlineUsers;
    }
}

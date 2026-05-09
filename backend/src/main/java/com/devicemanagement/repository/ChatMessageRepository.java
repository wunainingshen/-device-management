package com.devicemanagement.repository;

import com.devicemanagement.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId AND m.receiverId = :friendId) OR " +
           "(m.senderId = :friendId AND m.receiverId = :userId) " +
           "ORDER BY m.sendTime ASC")
    List<ChatMessage> findConversation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "m.receiverId = :userId AND m.isRead = false " +
           "ORDER BY m.sendTime DESC")
    List<ChatMessage> findUnreadMessages(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.senderId = :friendId AND m.receiverId = :userId AND m.isRead = false")
    int markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    long countBySenderIdAndReceiverIdAndIsRead(Long senderId, Long receiverId, Boolean isRead);
}

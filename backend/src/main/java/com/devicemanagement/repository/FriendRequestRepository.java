package com.devicemanagement.repository;

import com.devicemanagement.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, String status);
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, String status);

    @Query("SELECT r FROM FriendRequest r WHERE " +
           "(r.senderId = :userId AND r.receiverId = :friendId) OR " +
           "(r.senderId = :friendId AND r.receiverId = :userId) " +
           "ORDER BY r.createdAt DESC")
    List<FriendRequest> findRequestsBetweenUsers(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT r FROM FriendRequest r WHERE r.receiverId = :userId ORDER BY r.createdAt DESC")
    List<FriendRequest> findAllRequestsForUser(@Param("userId") Long userId);
}

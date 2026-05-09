package com.devicemanagement.repository;

import com.devicemanagement.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserIdAndStatus(Long userId, String status);
    List<Friend> findByFriendIdAndStatus(Long friendId, String status);

    @Query("SELECT f FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId")
    Optional<Friend> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    List<Friend> findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    boolean existsByUserIdAndFriendIdAndStatus(Long userId, Long friendId, String status);
}

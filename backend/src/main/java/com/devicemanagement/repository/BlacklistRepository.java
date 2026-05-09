package com.devicemanagement.repository;

import com.devicemanagement.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    List<Blacklist> findByUserId(Long userId);

    @Query("SELECT b FROM Blacklist b WHERE b.userId = :userId AND b.blockedUserId = :blockedUserId")
    Optional<Blacklist> findByUserIdAndBlockedUserId(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);

    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
}

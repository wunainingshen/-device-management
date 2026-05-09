package com.devicemanagement.service;

import com.devicemanagement.entity.Blacklist;
import com.devicemanagement.entity.Friend;
import com.devicemanagement.entity.FriendRequest;
import com.devicemanagement.entity.User;
import com.devicemanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private UserRepository userRepository;

    // Send friend request
    public FriendRequest sendFriendRequest(Long senderId, Long receiverId, String remark) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // Check if already friends
        List<Friend> existingFriendship = friendRepository.findFriendship(senderId, receiverId);
        for (Friend f : existingFriendship) {
            if ("ACCEPTED".equals(f.getStatus())) {
                throw new RuntimeException("你们已经是好友了");
            }
        }

        // Check if already sent
        List<FriendRequest> existingRequests = friendRequestRepository.findRequestsBetweenUsers(senderId, receiverId);
        for (FriendRequest req : existingRequests) {
            if ("PENDING".equals(req.getStatus())) {
                throw new RuntimeException("已发送过好友请求，请等待对方处理");
            }
        }

        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setRemark(remark);
        request.setStatus("PENDING");
        return friendRequestRepository.save(request);
    }

    // Handle friend request
    @Transactional
    public void handleFriendRequest(Long requestId, Long userId, boolean accept) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("请求不存在"));

        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权操作此请求");
        }

        if (accept) {
            // Create bidirectional friendship
            Friend friend1 = new Friend();
            friend1.setUserId(request.getSenderId());
            friend1.setFriendId(request.getReceiverId());
            friend1.setStatus("ACCEPTED");
            friendRepository.save(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(request.getReceiverId());
            friend2.setFriendId(request.getSenderId());
            friend2.setStatus("ACCEPTED");
            friendRepository.save(friend2);

            request.setStatus("ACCEPTED");
        } else {
            request.setStatus("REJECTED");
        }
        friendRequestRepository.save(request);
    }

    // Get pending friend requests
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        return requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("senderId", req.getSenderId());
            map.put("remark", req.getRemark());
            map.put("status", req.getStatus());
            map.put("createdAt", req.getCreatedAt());
            userRepository.findById(req.getSenderId()).ifPresent(sender -> {
                map.put("senderUsername", sender.getUsername());
                map.put("senderNickname", sender.getNickname());
                map.put("senderAvatar", sender.getAvatar());
            });
            return map;
        }).collect(Collectors.toList());
    }

    // Get friend list
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<Friend> friends = friendRepository.findByUserIdAndStatus(userId, "ACCEPTED");
        return friends.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("friendshipId", f.getId());
            map.put("friendId", f.getFriendId());
            map.put("createdAt", f.getCreatedAt());
            userRepository.findById(f.getFriendId()).ifPresent(friend -> {
                map.put("username", friend.getUsername());
                map.put("nickname", friend.getNickname());
                map.put("avatar", friend.getAvatar());
                map.put("email", friend.getEmail());
                map.put("isOnline", friend.getIsOnline());
                map.put("lastOnlineTime", friend.getLastOnlineTime());
            });
            return map;
        }).collect(Collectors.toList());
    }

    // Delete friend
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        List<Friend> friendships = friendRepository.findFriendship(userId, friendId);
        friendRepository.deleteAll(friendships);
    }

    // Block user (add to blacklist)
    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        if (blacklistRepository.existsByUserIdAndBlockedUserId(userId, blockedUserId)) {
            throw new RuntimeException("该用户已在黑名单中");
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setBlockedUserId(blockedUserId);
        blacklist.setCreatedAt(LocalDateTime.now());
        blacklistRepository.save(blacklist);

        // Also delete friendship if exists
        List<Friend> friendships = friendRepository.findFriendship(userId, blockedUserId);
        if (!friendships.isEmpty()) {
            friendRepository.deleteAll(friendships);
        }
    }

    // Unblock user and restore friendship
    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        blacklistRepository.findByUserIdAndBlockedUserId(userId, blockedUserId)
                .ifPresent(blacklistRepository::delete);

        // Restore friendship if not already friends
        List<Friend> existingFriendship = friendRepository.findFriendship(userId, blockedUserId);
        boolean alreadyFriends = existingFriendship.stream().anyMatch(f -> "ACCEPTED".equals(f.getStatus()));
        if (!alreadyFriends) {
            // Delete any existing (non-accepted) friendships first
            if (!existingFriendship.isEmpty()) {
                friendRepository.deleteAll(existingFriendship);
            }
            Friend friend1 = new Friend();
            friend1.setUserId(userId);
            friend1.setFriendId(blockedUserId);
            friend1.setStatus("ACCEPTED");
            friend1.setCreatedAt(LocalDateTime.now());
            friendRepository.save(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(blockedUserId);
            friend2.setFriendId(userId);
            friend2.setStatus("ACCEPTED");
            friend2.setCreatedAt(LocalDateTime.now());
            friendRepository.save(friend2);
        }
    }

    // Get blacklist
    public List<Map<String, Object>> getBlacklist(Long userId) {
        List<Blacklist> blacklists = blacklistRepository.findByUserId(userId);
        return blacklists.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("blockedUserId", b.getBlockedUserId());
            map.put("createdAt", b.getCreatedAt());
            userRepository.findById(b.getBlockedUserId()).ifPresent(user -> {
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
            });
            return map;
        }).collect(Collectors.toList());
    }

    public boolean isBlocked(Long userId, Long targetUserId) {
        return blacklistRepository.existsByUserIdAndBlockedUserId(userId, targetUserId);
    }
}

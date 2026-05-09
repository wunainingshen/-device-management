package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.entity.FriendRequest;
import com.devicemanagement.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendRequest>> sendRequest(Authentication authentication,
                                                                   @RequestBody Map<String, Object> request) {
        Long senderId = (Long) authentication.getCredentials();
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String remark = (String) request.getOrDefault("remark", "");
        try {
            FriendRequest fr = friendService.sendFriendRequest(senderId, receiverId, remark);
            return ResponseEntity.ok(ApiResponse.success("好友请求已发送", fr));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/request/{requestId}/handle")
    public ResponseEntity<ApiResponse<Void>> handleRequest(Authentication authentication,
                                                            @PathVariable Long requestId,
                                                            @RequestParam boolean accept) {
        Long userId = (Long) authentication.getCredentials();
        try {
            friendService.handleFriendRequest(requestId, userId, accept);
            String msg = accept ? "已同意好友请求" : "已拒绝好友请求";
            return ResponseEntity.ok(ApiResponse.success(msg, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRequests(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(friendService.getPendingRequests(userId)));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFriends(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(friendService.getFriendList(userId)));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(Authentication authentication,
                                                           @PathVariable Long friendId) {
        Long userId = (Long) authentication.getCredentials();
        friendService.deleteFriend(userId, friendId);
        return ResponseEntity.ok(ApiResponse.success("好友已删除", null));
    }

    @PostMapping("/block/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(Authentication authentication,
                                                        @PathVariable Long blockedUserId) {
        Long userId = (Long) authentication.getCredentials();
        try {
            friendService.blockUser(userId, blockedUserId);
            return ResponseEntity.ok(ApiResponse.success("已拉黑用户", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unblock/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(Authentication authentication,
                                                          @PathVariable Long blockedUserId) {
        Long userId = (Long) authentication.getCredentials();
        friendService.unblockUser(userId, blockedUserId);
        return ResponseEntity.ok(ApiResponse.success("已解除拉黑", null));
    }

    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBlacklist(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        return ResponseEntity.ok(ApiResponse.success(friendService.getBlacklist(userId)));
    }

    @GetMapping("/check-block/{targetUserId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBlocked(Authentication authentication,
                                                              @PathVariable Long targetUserId) {
        Long userId = (Long) authentication.getCredentials();
        boolean blocked = friendService.isBlocked(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(blocked));
    }
}

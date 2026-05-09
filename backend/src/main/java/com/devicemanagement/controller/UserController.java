package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.dto.RegisterRequest;
import com.devicemanagement.entity.User;
import com.devicemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<User>> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getCredentials();
        User user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<User>> updateUser(Authentication authentication,
                                                         @RequestBody User updatedUser) {
        Long userId = (Long) authentication.getCredentials();
        User user = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                             @RequestBody Map<String, String> request) {
        Long userId = (Long) authentication.getCredentials();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(keyword)));
    }
}

package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.dto.RegisterRequest;
import com.devicemanagement.entity.Device;
import com.devicemanagement.entity.User;
import com.devicemanagement.service.DeviceService;
import com.devicemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    // ====== User Management ======
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.adminCreateUser(request);
            return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long userId,
                                                                @RequestParam boolean enabled) {
        userService.toggleUserEnabled(userId, enabled);
        String msg = enabled ? "用户已启用" : "用户已禁用";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("用户已删除", null));
    }

    // ====== Device Management ======
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<Device>>> getAllDevices() {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getAllDevices()));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<ApiResponse<Device>> getDevice(@PathVariable Long id) {
        try {
            Device device = deviceService.getDeviceById(id);
            return ResponseEntity.ok(ApiResponse.success(device));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/devices")
    public ResponseEntity<ApiResponse<Device>> createDevice(@RequestBody Device device) {
        Device saved = deviceService.createDevice(device);
        return ResponseEntity.ok(ApiResponse.success("设备添加成功", saved));
    }

    @PutMapping("/devices/{id}")
    public ResponseEntity<ApiResponse<Device>> updateDevice(@PathVariable Long id,
                                                             @RequestBody Device device) {
        try {
            Device updated = deviceService.updateDevice(id, device);
            return ResponseEntity.ok(ApiResponse.success("设备更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success("设备已删除", null));
    }

    @GetMapping("/devices/search")
    public ResponseEntity<ApiResponse<List<Device>>> searchDevices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.searchDevices(name, category, status)));
    }
}

package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.entity.Device;
import com.devicemanagement.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Device>>> getDevices() {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getAllDevices()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Device>>> searchDevices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.searchDevices(name, category, status)));
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportDevices() {
        try {
            byte[] data = deviceService.exportToExcel();
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devices_export.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<List<Device>>> importDevices(@RequestParam("file") MultipartFile file) {
        try {
            List<Device> devices = deviceService.importFromExcel(file);
            return ResponseEntity.ok(ApiResponse.success("导入成功，共导入" + devices.size() + "条记录", devices));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }
}

package com.devicemanagement.controller;

import com.devicemanagement.dto.ApiResponse;
import com.devicemanagement.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
            String reply = deepSeekService.chat(message, history);
            return ResponseEntity.ok(ApiResponse.success(reply));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("AI服务异常: " + e.getMessage()));
        }
    }
}

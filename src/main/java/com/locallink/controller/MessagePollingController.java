package com.locallink.controller;

import com.alibaba.fastjson2.JSONObject;
import com.locallink.common.Result;
import com.locallink.entity.ChatMessage;
import com.locallink.service.MessagePollingService;
import com.locallink.service.OnlineStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息轮询控制器（HTTP长轮询方案）
 */
@Slf4j
@RestController
@RequestMapping("/api/polling")
@Tag(name = "消息轮询", description = "HTTP长轮询相关接口")
public class MessagePollingController {
    
    @Autowired
    private MessagePollingService messagePollingService;
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    /**
     * 长轮询获取新消息
     */
    @PostMapping("/messages")
    @Operation(summary = "长轮询获取消息", description = "阻塞最多30秒，有新消息立即返回")
    public Result<List<JSONObject>> pollMessages(
            @RequestBody PollRequest request) {
        
        log.debug("用户{}开始轮询消息, lastMessageId: {}", 
                request.getUserId(), request.getLastMessageId());
        
        // 更新心跳
        onlineStatusService.updateHeartbeat(request.getUserId());
        
        // 长轮询获取消息
        List<JSONObject> messages = messagePollingService.pollMessages(
                request.getUserId(),
                request.getLastMessageId(),
                request.getTimeout() != null ? request.getTimeout() : 30
        );
        
        return Result.success(messages);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "发送私聊或群聊消息")
    public Result<Void> sendMessage(@RequestBody ChatMessage message) {
        log.info("收到消息发送请求: sender={}, receiver={}", 
                message.getSenderId(), message.getReceiverId());
        
        // 设置发送时间
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0);
        
        // 保存并推送消息
        messagePollingService.saveAndPushMessage(message);
        
        return Result.success();
    }
    
    /**
     * 心跳保活
     */
    @PostMapping("/heartbeat")
    @Operation(summary = "心跳保活", description = "定期发送心跳保持在线状态")
    public Result<Void> heartbeat(@RequestBody HeartbeatRequest request) {
        onlineStatusService.updateHeartbeat(request.getUserId());
        log.debug("用户{}心跳", request.getUserId());
        return Result.success();
    }
    
    /**
     * 查询在线状态
     */
    @GetMapping("/online-status")
    @Operation(summary = "查询在线状态", description = "批量查询用户在线状态")
    public Result<Map<Long, Boolean>> getOnlineStatus(
            @RequestParam List<Long> userIds) {
        
        Map<Long, Boolean> statusMap = onlineStatusService.checkOnlineStatus(userIds);
        return Result.success(statusMap);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户主动下线")
    public Result<Void> logout(@RequestBody LogoutRequest request) {
        onlineStatusService.userLogout(request.getUserId());
        messagePollingService.unregisterUser(request.getUserId());
        log.info("用户{}登出", request.getUserId());
        return Result.success();
    }
    
    // ==================== 请求DTO ====================
    
    @lombok.Data
    static class PollRequest {
        private Long userId;
        private Long lastMessageId;
        private Integer timeout; // 超时时间（秒），默认30
    }
    
    @lombok.Data
    static class HeartbeatRequest {
        private Long userId;
    }
    
    @lombok.Data
    static class LogoutRequest {
        private Long userId;
    }
}

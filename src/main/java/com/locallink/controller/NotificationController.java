package com.locallink.controller;

import com.locallink.common.Result;
import com.locallink.entity.SysNotification;
import com.locallink.service.SysNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "通知管理", description = "系统通知相关接口")
public class NotificationController {
    
    @Autowired
    private SysNotificationService notificationService;
    
    @Operation(summary = "获取通知列表", description = "获取用户的通知列表")
    @GetMapping("/list")
    public Result<List<SysNotification>> getNotifications(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "通知类型（可选）") @RequestParam(required = false) Integer type) {
        List<SysNotification> notifications = notificationService.getUserNotifications(userId, type);
        return Result.success(notifications);
    }
    
    @Operation(summary = "获取未读通知数量", description = "获取用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读")
    @PostMapping("/mark-read/{notificationId}")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID", required = true) @PathVariable @NotNull(message = "通知ID不能为空") Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Result.success();
    }
    
    @Operation(summary = "全部标记为已读", description = "标记用户所有通知为已读")
    @PostMapping("/mark-all-read")
    public Result<Void> markAllAsRead(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}

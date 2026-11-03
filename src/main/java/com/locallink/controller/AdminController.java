package com.locallink.controller;

import com.locallink.common.Result;
import com.locallink.entity.UserReport;
import com.locallink.entity.UserInfo;
import com.locallink.service.AdminService;
import com.locallink.service.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserReportService userReportService;
    
    // ==================== 用户管理 ====================
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result<List<UserInfo>> getAllUsers() {
        List<UserInfo> users = adminService.getAllUsers();
        return Result.success(users);
    }
    
    /**
     * 封号用户
     */
    @PostMapping("/ban-user")
    public Result<Void> banUser(@RequestParam Long userId, 
                                @RequestParam Long adminId,
                                @RequestParam String reason) {
        try {
            adminService.banUser(userId, adminId, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 解封用户
     */
    @PostMapping("/unban-user")
    public Result<Void> unbanUser(@RequestParam Long userId,
                                  @RequestParam Long adminId) {
        try {
            adminService.unbanUser(userId, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 禁言用户
     */
    @PostMapping("/mute-user")
    public Result<Void> muteUser(@RequestParam Long userId,
                                 @RequestParam Long adminId,
                                 @RequestParam String reason,
                                 @RequestParam(required = false) Long durationMinutes) {
        try {
            LocalDateTime expireTime = null;
            if (durationMinutes != null && durationMinutes > 0) {
                expireTime = LocalDateTime.now().plusMinutes(durationMinutes);
            }
            adminService.muteUser(userId, adminId, reason, expireTime);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 解除禁言
     */
    @PostMapping("/unmute-user")
    public Result<Void> unmuteUser(@RequestParam Long userId,
                                   @RequestParam Long adminId) {
        try {
            adminService.unmuteUser(userId, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 设置用户角色
     */
    @PostMapping("/set-role")
    public Result<Void> setUserRole(@RequestParam Long userId,
                                    @RequestParam Long adminId,
                                    @RequestParam Integer role) {
        try {
            adminService.setUserRole(userId, adminId, role);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    // ==================== 举报管理 ====================
    
    /**
     * 提交举报
     */
    @PostMapping("/report")
    public Result<Void> submitReport(@RequestParam Long reporterId,
                                     @RequestParam(required = false) Long reportedUserId,
                                     @RequestParam(required = false) Long messageId,
                                     @RequestParam Integer reportType,
                                     @RequestParam Integer reasonCode,
                                     @RequestParam(required = false) String reason,
                                     @RequestParam(required = false, defaultValue = "0") Integer includeMessages) {
        try {
            userReportService.submitReport(reporterId, reportedUserId, messageId, reportType, 
                                          reasonCode, reason, includeMessages);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取待处理的举报列表
     */
    @GetMapping("/reports/pending")
    public Result<List<UserReport>> getPendingReports() {
        List<UserReport> reports = userReportService.getPendingReports();
        return Result.success(reports);
    }
    
    /**
     * 获取所有举报列表
     */
    @GetMapping("/reports/all")
    public Result<List<UserReport>> getAllReports() {
        List<UserReport> reports = userReportService.getAllReports();
        return Result.success(reports);
    }
    
    /**
     * 处理举报
     */
    @PostMapping("/report/handle")
    public Result<Void> handleReport(@RequestParam Long reportId,
                                     @RequestParam Long handlerId,
                                     @RequestParam Integer status,
                                     @RequestParam String handleResult) {
        try {
            userReportService.handleReport(reportId, handlerId, status, handleResult);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 处理举报并执行处罚
     */
    @PostMapping("/report/handle-with-penalty")
    public Result<Void> handleReportWithPenalty(@RequestParam Long reportId,
                                                @RequestParam Long handlerId,
                                                @RequestParam Integer status,
                                                @RequestParam String handleResult,
                                                @RequestParam(required = false) Integer penaltyType) {
        try {
            userReportService.handleReportWithPenalty(reportId, handlerId, status, handleResult, penaltyType);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    // ==================== 申诉管理 ====================
    
    /**
     * 提交申诉（用户向管理员发起申诉）
     */
    @PostMapping("/appeal")
    public Result<Void> submitAppeal(@RequestParam Long userId,
                                     @RequestParam String content) {
        try {
            // 创建一条特殊的消息，发送给所有管理员
            adminService.submitAppeal(userId, content);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取待处理的申诉列表（仅管理员可见）
     */
    @GetMapping("/appeals/pending")
    public Result<List<?>> getPendingAppeals() {
        try {
            List<?> appeals = adminService.getPendingAppeals();
            return Result.success(appeals);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}

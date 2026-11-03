package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService extends IService<UserInfo> {
    
    /**
     * 封号用户
     */
    void banUser(Long userId, Long adminId, String reason);
    
    /**
     * 解封用户
     */
    void unbanUser(Long userId, Long adminId);
    
    /**
     * 禁言用户
     */
    void muteUser(Long userId, Long adminId, String reason, LocalDateTime expireTime);
    
    /**
     * 解除禁言
     */
    void unmuteUser(Long userId, Long adminId);
    
    /**
     * 获取所有用户列表（管理员可见）
     */
    List<UserInfo> getAllUsers();
    
    /**
     * 设置用户角色
     */
    void setUserRole(Long userId, Long adminId, Integer role);
    
    /**
     * 提交申诉（用户向管理员发起申诉）
     */
    void submitAppeal(Long userId, String content);
    
    /**
     * 获取待处理的申诉列表
     */
    java.util.List<?> getPendingAppeals();
}

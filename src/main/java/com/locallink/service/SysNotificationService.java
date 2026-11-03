package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.SysNotification;

import java.util.List;

public interface SysNotificationService extends IService<SysNotification> {
    
    /**
     * 创建通知并推送给指定用户
     */
    void createAndPushNotification(Long userId, Integer type, String title, String content, Long relatedId);
    
    /**
     * 获取用户的通知列表
     */
    List<SysNotification> getUserNotifications(Long userId, Integer type);
    
    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);
    
    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);
}

package com.locallink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.entity.SysNotification;
import com.locallink.mapper.SysNotificationMapper;
import com.locallink.service.SysNotificationService;
import com.locallink.websocket.WebSocketSessionManager;
import com.alibaba.fastjson2.JSONObject;
import com.locallink.common.WsMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.websocket.Session;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification> implements SysNotificationService {
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    @Override
    public void createAndPushNotification(Long userId, Integer type, String title, String content, Long relatedId) {
        // 1. 创建通知记录
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        
        this.save(notification);
        log.info("创建通知成功: userId={}, type={}, title={}", userId, type, title);
        
        // 2. 通过 WebSocket 推送给用户
        try {
            Session session = sessionManager.getSession(userId);
            if (session != null && session.isOpen()) {
                JSONObject message = new JSONObject();
                message.put("type", WsMessageType.NOTIFICATION);
                message.put("data", notification);
                session.getBasicRemote().sendText(message.toJSONString());
                log.info("推送通知成功: userId={}, notificationId={}", userId, notification.getId());
            } else {
                log.warn("用户不在线，通知已保存: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("推送通知失败: userId={}", userId, e);
        }
    }
    
    @Override
    public List<SysNotification> getUserNotifications(Long userId, Integer type) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
               .orderByDesc(SysNotification::getCreateTime);
        
        if (type != null) {
            wrapper.eq(SysNotification::getType, type);
        }
        
        return this.list(wrapper);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
               .eq(SysNotification::getIsRead, 0);
        return Math.toIntExact(this.count(wrapper));
    }
    
    @Override
    public void markAsRead(Long notificationId) {
        SysNotification notification = this.getById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            this.updateById(notification);
            log.info("标记通知为已读: notificationId={}", notificationId);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
               .eq(SysNotification::getIsRead, 0);
        
        List<SysNotification> notifications = this.list(wrapper);
        for (SysNotification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
        }
        this.updateBatchById(notifications);
        log.info("标记所有通知为已读: userId={}, count={}", userId, notifications.size());
    }
}

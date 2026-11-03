package com.locallink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线状态管理服务（HTTP长轮询方案）
 * 替代WebSocket SessionManager，管理用户在线状态
 */
@Slf4j
@Service
public class OnlineStatusService {
    
    /**
     * 用户最后心跳时间
     */
    private final Map<Long, LocalDateTime> userLastHeartbeatMap = new ConcurrentHashMap<>();
    
    /**
     * 用户离线阈值（秒），超过此时间未心跳视为离线
     */
    private static final long OFFLINE_THRESHOLD_SECONDS = 60;
    
    /**
     * 更新用户心跳时间
     */
    public void updateHeartbeat(Long userId) {
        userLastHeartbeatMap.put(userId, LocalDateTime.now());
        log.debug("用户{}心跳更新", userId);
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        LocalDateTime lastHeartbeat = userLastHeartbeatMap.get(userId);
        if (lastHeartbeat == null) {
            return false;
        }
        
        long secondsSinceLastHeartbeat = java.time.Duration.between(
            lastHeartbeat, 
            LocalDateTime.now()
        ).getSeconds();
        
        return secondsSinceLastHeartbeat < OFFLINE_THRESHOLD_SECONDS;
    }
    
    /**
     * 获取所有在线用户ID
     */
    public Set<Long> getOnlineUserIds() {
        cleanupOfflineUsers();
        return new HashSet<>(userLastHeartbeatMap.keySet());
    }
    
    /**
     * 批量检查用户在线状态
     */
    public Map<Long, Boolean> checkOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> statusMap = new HashMap<>();
        for (Long userId : userIds) {
            statusMap.put(userId, isOnline(userId));
        }
        return statusMap;
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        cleanupOfflineUsers();
        return userLastHeartbeatMap.size();
    }
    
    /**
     * 用户下线（主动登出时调用）
     */
    public void userLogout(Long userId) {
        userLastHeartbeatMap.remove(userId);
        log.info("用户{}主动下线", userId);
    }
    
    /**
     * 清理离线用户
     */
    private void cleanupOfflineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);
        userLastHeartbeatMap.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(threshold)) {
                log.debug("清理离线用户: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}

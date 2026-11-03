package com.locallink.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理所有在线用户的WebSocket会话
 */
@Slf4j
@Component
public class WebSocketSessionManager {
    
    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();
    
    /**
     * 添加会话
     */
    public void addSession(Long userId, Session session) {
        sessionMap.put(userId, session);
        log.info("用户{}上线，当前在线人数: {}", userId, sessionMap.size());
    }
    
    /**
     * 移除会话
     */
    public void removeSession(Long userId) {
        sessionMap.remove(userId);
        log.info("用户{}下线，当前在线人数: {}", userId, sessionMap.size());
    }
    
    /**
     * 获取会话
     */
    public Session getSession(Long userId) {
        return sessionMap.get(userId);
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        Session session = sessionMap.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }
    
    /**
     * 获取所有在线用户ID
     */
    public java.util.Set<Long> getOnlineUserIds() {
        // 返回一个副本，避免并发修改异常
        return new java.util.HashSet<>(sessionMap.keySet());
    }
    
    /**
     * 清理无效的会话（已断开的连接）
     */
    public void cleanupInvalidSessions() {
        sessionMap.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session == null || !session.isOpen()) {
                log.info("清理无效会话: 用户{}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}

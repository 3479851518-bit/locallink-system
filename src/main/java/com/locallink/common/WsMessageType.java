package com.locallink.common;

/**
 * WebSocket消息类型常量
 */
public class WsMessageType {
    
    /**
     * 聊天消息
     */
    public static final String CHAT = "chat";
    
    /**
     * 心跳消息
     */
    public static final String HEARTBEAT = "heartbeat";
    
    /**
     * 上线通知
     */
    public static final String ONLINE = "online";
    
    /**
     * 下线通知
     */
    public static final String OFFLINE = "offline";
    
    /**
     * 错误消息
     */
    public static final String ERROR = "error";
    
    /**
     * 通知消息
     */
    public static final String NOTIFICATION = "notification";
    
    private WsMessageType() {
        // 防止实例化
    }
}

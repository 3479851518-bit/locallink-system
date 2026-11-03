package com.locallink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WebSocket配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {
    
    /**
     * 心跳间隔（毫秒）
     */
    private long heartbeatInterval = 30000;
    
    /**
     * 消息撤回时间限制（秒）
     */
    private int recallTimeLimit = 120;
}

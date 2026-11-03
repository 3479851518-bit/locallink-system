package com.locallink.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.locallink.common.WsMessageType;
import com.locallink.entity.ChatMessage;
import com.locallink.entity.FriendRelation;
import com.locallink.entity.UserInfo;
import com.locallink.enums.FriendStatusEnum;
import com.locallink.service.ChatMessageService;
import com.locallink.service.FriendRelationService;
import com.locallink.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.locallink.websocket.WebSocketSessionManager;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/chat/{userId}", configurator = SpringConfigurator.class)
public class ChatWebSocketHandler {
    
    // 使用静态变量存储 SessionManager，但通过 Spring 注入初始化
    private static WebSocketSessionManager sessionManager;
    private static ChatMessageService chatMessageService;
    private static FriendRelationService friendRelationService;
    private static UserInfoService userInfoService;
    
    @Autowired
    public void setSessionManager(WebSocketSessionManager sessionManager) {
        ChatWebSocketHandler.sessionManager = sessionManager;
    }
    
    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocketHandler.chatMessageService = chatMessageService;
    }
    
    @Autowired
    public void setFriendRelationService(FriendRelationService friendRelationService) {
        ChatWebSocketHandler.friendRelationService = friendRelationService;
    }
    
    @Autowired
    public void setUserInfoService(UserInfoService userInfoService) {
        ChatWebSocketHandler.userInfoService = userInfoService;
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        log.info("用户{} WebSocket连接建立", userId);
        
        // 先清理无效会话，避免向已断开的连接发送消息
        if (sessionManager != null) {
            sessionManager.cleanupInvalidSessions();
        }
        
        sessionManager.addSession(userId, session);
        
        // 异步广播在线状态，避免阻塞主连接
        new Thread(() -> {
            try {
                JSONObject message = new JSONObject();
                message.put("type", WsMessageType.ONLINE);
                message.put("userId", userId);
                broadcast(message.toJSONString());
            } catch (Exception e) {
                log.error("广播在线状态失败", e);
            }
        }).start();
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        log.info("用户{} WebSocket连接关闭", userId);
        sessionManager.removeSession(userId);
        
        // 异步广播离线状态
        new Thread(() -> {
            try {
                JSONObject message = new JSONObject();
                message.put("type", WsMessageType.OFFLINE);
                message.put("userId", userId);
                broadcast(message.toJSONString());
            } catch (Exception e) {
                log.error("广播离线状态失败", e);
            }
        }).start();
    }
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到用户{}的消息: {}", userId, message);
        
        try {
            // 1. 校验用户权限（封号、禁言）
            String permissionError = userInfoService.validateMessagePermission(userId);
            if (permissionError != null) {
                JSONObject errorMsg = new JSONObject();
                errorMsg.put("type", WsMessageType.ERROR);
                errorMsg.put("message", permissionError);
                session.getBasicRemote().sendText(errorMsg.toJSONString());
                return;
            }
            
            JSONObject json = JSON.parseObject(message);
            String type = json.getString("type");
            
            if (WsMessageType.CHAT.equals(type)) {
                Long receiverId = json.getLong("receiverId");
                Integer chatType = json.getInteger("chatType");
                
                // 2. 私聊时检查拉黑状态
                if (chatType != null && chatType == 1 && receiverId != null) {
                    FriendRelation relation = friendRelationService.lambdaQuery()
                            .eq(FriendRelation::getUserId, receiverId)
                            .eq(FriendRelation::getFriendId, userId)
                            .eq(FriendRelation::getStatus, com.locallink.enums.FriendStatusEnum.ACCEPTED.getCode())
                            .one();
                    
                    if (relation != null && relation.getIsBlocked() != null && relation.getIsBlocked() == 1) {
                        log.warn("用户{}被用户{}拉黑，消息发送失败", userId, receiverId);
                        JSONObject errorMsg = new JSONObject();
                        errorMsg.put("type", WsMessageType.ERROR);
                        errorMsg.put("message", "您已被对方拉黑，无法发送消息");
                        session.getBasicRemote().sendText(errorMsg.toJSONString());
                        return;
                    }
                }
                
                // 3. 保存并转发消息
                UserInfo sender = userInfoService.getById(userId);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderId(userId);
                chatMessage.setReceiverId(receiverId);
                chatMessage.setGroupId(json.getString("groupId"));
                chatMessage.setChatType(chatType);
                chatMessage.setMessageType(json.getInteger("messageType"));
                chatMessage.setContent(json.getString("content"));
                chatMessage.setSendTime(LocalDateTime.now());
                chatMessage.setIsRead(0);
                
                chatMessageService.save(chatMessage);
                
                // 添加消息ID、时间戳、发送者昵称和头像
                json.put("id", chatMessage.getId());
                json.put("sendTime", chatMessage.getSendTime());
                json.put("senderNickname", sender.getNickname());
                json.put("senderAvatar", sender.getAvatar());
                String messageWithId = json.toJSONString();
                
                // 群聊广播，私聊点对点
                if (chatMessage.getChatType() != null && chatMessage.getChatType() == 2) {
                    broadcast(messageWithId);
                } else {
                    sendMessage(receiverId, messageWithId);
                    sendMessage(userId, messageWithId); // 回传给发送者
                    
                    // 4. 如果是管理员回复申诉，同时发送系统通知给用户
                    if (sender.getRole() != null && sender.getRole() == 1 && receiverId != null) {
                        // 检查接收者是否是被禁言的用户
                        UserInfo receiver = userInfoService.getById(receiverId);
                        if (receiver != null && receiver.getIsMuted() != null && receiver.getIsMuted() == 1) {
                            // 发送系统通知
                            JSONObject notification = new JSONObject();
                            notification.put("type", WsMessageType.NOTIFICATION);
                            notification.put("notificationType", 8); // 处罚通知类型
                            notification.put("title", "申诉回复");
                            notification.put("content", "管理员回复您的申诉：" + chatMessage.getContent());
                            notification.put("timestamp", LocalDateTime.now());
                            sendMessage(receiverId, notification.toJSONString());
                            log.info("向用户{}发送申诉回复通知", receiverId);
                        }
                    }
                }
            } else if (WsMessageType.HEARTBEAT.equals(type)) {
                JSONObject response = new JSONObject();
                response.put("type", WsMessageType.HEARTBEAT);
                response.put("timestamp", System.currentTimeMillis());
                session.getBasicRemote().sendText(response.toJSONString());
            }
        } catch (Exception e) {
            log.error("消息处理异常", e);
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        log.error("WebSocket异常,用户{}", userId, error);
        // 发生错误时，确保清理会话
        sessionManager.removeSession(userId);
    }
    
    /**
     * 发送消息给指定用户（供其他 Service 调用）
     */
    public static void sendMessage(Long userId, String message) {
        if (sessionManager != null) {
            Session session = sessionManager.getSession(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }
    
    private void broadcast(String message) {
        if (sessionManager != null) {
            // 创建副本以避免并发修改异常
            java.util.Set<Long> onlineUserIds = new java.util.HashSet<>(sessionManager.getOnlineUserIds());
            
            for (Long userId : onlineUserIds) {
                try {
                    Session session = sessionManager.getSession(userId);
                    if (session != null && session.isOpen()) {
                        session.getBasicRemote().sendText(message);
                    } else {
                        // 清理无效会话
                        sessionManager.removeSession(userId);
                    }
                } catch (Exception e) {
                    log.warn("向用户{}广播消息失败，清理会话: {}", userId, e.getMessage());
                    // 发生异常时清理无效会话，但不中断其他用户的广播
                    sessionManager.removeSession(userId);
                }
            }
        }
    }
}

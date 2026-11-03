package com.locallink.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.locallink.entity.ChatMessage;
import com.locallink.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 消息长轮询服务
 * 实现HTTP长轮询获取新消息
 */
@Slf4j
@Service
public class MessagePollingService {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 用户消息队列映射（userId -> 消息队列）
     */
    private final ConcurrentHashMap<Long, LinkedBlockingQueue<ChatMessage>> userMessageQueues = new ConcurrentHashMap<>();
    
    /**
     * 最大等待时间（秒）
     */
    private static final int MAX_WAIT_SECONDS = 30;
    
    /**
     * 注册用户的消息队列
     */
    public void registerUser(Long userId) {
        userMessageQueues.putIfAbsent(userId, new LinkedBlockingQueue<>());
        log.debug("用户{}注册消息队列", userId);
    }
    
    /**
     * 移除用户的消息队列
     */
    public void unregisterUser(Long userId) {
        userMessageQueues.remove(userId);
        log.debug("用户{}注销消息队列", userId);
    }
    
    /**
     * 推送消息到用户队列
     */
    public void pushMessage(Long userId, ChatMessage message) {
        LinkedBlockingQueue<ChatMessage> queue = userMessageQueues.get(userId);
        if (queue != null) {
            queue.offer(message);
            log.debug("消息推送给用户{}, 队列大小: {}", userId, queue.size());
        }
    }
    
    /**
     * 长轮询获取消息（阻塞最多MAX_WAIT_SECONDS）
     * 
     * @param userId 用户ID
     * @param lastMessageId 最后一条消息ID（用于去重）
     * @param timeout 超时时间（秒）
     * @return 消息列表
     */
    public List<JSONObject> pollMessages(Long userId, Long lastMessageId, int timeout) {
        // 确保用户已注册
        registerUser(userId);
        
        List<JSONObject> result = new ArrayList<>();
        LinkedBlockingQueue<ChatMessage> queue = userMessageQueues.get(userId);
        
        if (queue == null) {
            return result;
        }
        
        long waitTime = Math.min(timeout, MAX_WAIT_SECONDS) * 1000L;
        long startTime = System.currentTimeMillis();
        
        try {
            // 尝试从队列中获取消息
            while (System.currentTimeMillis() - startTime < waitTime) {
                ChatMessage message = queue.poll(1, TimeUnit.SECONDS);
                if (message != null) {
                    // 过滤已接收的消息
                    if (lastMessageId == null || message.getId() > lastMessageId) {
                        JSONObject msgJson = convertToJsonObject(message);
                        result.add(msgJson);
                        
                        // 如果已经有消息，立即返回（不等待更多消息）
                        if (!result.isEmpty()) {
                            break;
                        }
                    }
                }
                
                // 检查是否超时
                if (System.currentTimeMillis() - startTime >= waitTime) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("用户{}消息轮询被中断", userId);
        }
        
        log.debug("用户{}轮询返回{}条消息", userId, result.size());
        return result;
    }
    
    /**
     * 将ChatMessage转换为JSONObject（包含发送者信息）
     */
    private JSONObject convertToJsonObject(ChatMessage message) {
        JSONObject json = new JSONObject();
        json.put("id", message.getId());
        json.put("senderId", message.getSenderId());
        json.put("receiverId", message.getReceiverId());
        json.put("groupId", message.getGroupId());
        json.put("chatType", message.getChatType());
        json.put("messageType", message.getMessageType());
        json.put("content", message.getContent());
        json.put("sendTime", message.getSendTime());
        json.put("isRead", message.getIsRead());
        
        // 添加发送者信息
        try {
            UserInfo sender = userInfoService.getById(message.getSenderId());
            if (sender != null) {
                json.put("senderNickname", sender.getNickname());
                json.put("senderAvatar", sender.getAvatar());
            }
        } catch (Exception e) {
            log.error("获取发送者信息失败", e);
        }
        
        return json;
    }
    
    /**
     * 保存并推送消息
     */
    public void saveAndPushMessage(ChatMessage message) {
        // 保存消息
        chatMessageService.save(message);
        
        // 根据聊天类型推送消息
        if (message.getChatType() != null && message.getChatType() == 2) {
            // 群聊：需要查询群成员并推送给所有成员
            // TODO: 这里需要注入GroupMemberService来查询群成员
            // 暂时简化处理，推送给所有注册用户
            pushToAllUsers(message);
        } else {
            // 私聊：推送给接收者和发送者
            if (message.getReceiverId() != null) {
                pushMessage(message.getReceiverId(), message);
            }
            pushMessage(message.getSenderId(), message);
        }
        
        log.info("消息保存并推送成功, messageId: {}", message.getId());
    }
    
    /**
     * 推送消息给所有用户（群聊场景）
     */
    private void pushToAllUsers(ChatMessage message) {
        for (Long userId : userMessageQueues.keySet()) {
            pushMessage(userId, message);
        }
    }
}

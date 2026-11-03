package com.locallink.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.entity.ChatMessage;
import com.locallink.entity.UserInfo;
import com.locallink.enums.ChatTypeEnum;
import com.locallink.mapper.ChatMessageMapper;
// import com.locallink.rabbitmq.MessageSender; // RabbitMQ 已禁用
import com.locallink.service.ChatMessageService;
import com.locallink.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    // @Autowired(required = false)
    // private MessageSender messageSender; // RabbitMQ 已禁用
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Override
    public void sendMessage(ChatMessage message) {
        message.setSendTime(LocalDateTime.now());
        this.save(message);
        
        // RabbitMQ 消息发送已禁用，使用 WebSocket 和 HTTP 轮询
        // if (messageSender != null) {
        //     messageSender.sendChatMessage(message);
        // }
        
        log.info("发送消息: {} -> {}", message.getSenderId(), message.getReceiverId());
    }
    
    @Override
    public List<ChatMessage> getPrivateHistory(Long userId, Long friendId, Integer limit) {
        return this.lambdaQuery()
                .eq(ChatMessage::getChatType, ChatTypeEnum.PRIVATE.getCode())
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId)
                    .eq(ChatMessage::getReceiverId, friendId)
                    .or()
                    .eq(ChatMessage::getSenderId, friendId)
                    .eq(ChatMessage::getReceiverId, userId)
                )
                .orderByDesc(ChatMessage::getSendTime)
                .last("LIMIT " + Math.min(limit != null ? limit : 50, 100))
                .list();
    }
    
    @Override
    public List<JSONObject> getPrivateHistoryWithUserInfo(Long userId, Long friendId, Integer limit) {
        List<ChatMessage> messages = getPrivateHistory(userId, friendId, limit);
        
        // 为每条消息添加发送者昵称和头像
        List<JSONObject> result = new ArrayList<>();
        Map<Long, UserInfo> userCache = new HashMap<>();
        
        for (ChatMessage msg : messages) {
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(msg));
            
            // 从缓存或数据库获取发送者信息
            UserInfo sender = userCache.get(msg.getSenderId());
            if (sender == null) {
                sender = userInfoService.getById(msg.getSenderId());
                if (sender != null) {
                    userCache.put(msg.getSenderId(), sender);
                }
            }
            
            String nickname = sender != null ? sender.getNickname() : "未知用户";
            String avatar = sender != null ? sender.getAvatar() : null;
            
            json.put("senderNickname", nickname);
            json.put("senderAvatar", avatar);
            result.add(json);
        }
        
        return result;
    }
    
    @Override
    public List<ChatMessage> getGroupHistory(String groupId, Integer limit) {
        return this.lambdaQuery()
                .eq(ChatMessage::getChatType, ChatTypeEnum.GROUP.getCode())
                .eq(ChatMessage::getGroupId, groupId)
                .orderByDesc(ChatMessage::getSendTime)
                .last("LIMIT " + Math.min(limit != null ? limit : 50, 100))
                .list();
    }
    
    @Override
    public List<JSONObject> getGroupHistoryWithNickname(String groupId, Integer limit) {
        List<ChatMessage> messages = getGroupHistory(groupId, limit);
        
        // 为每条消息添加发送者昵称和头像
        List<JSONObject> result = new ArrayList<>();
        Map<Long, UserInfo> userCache = new HashMap<>();
        
        for (ChatMessage msg : messages) {
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(msg));
            
            // 从缓存或数据库获取发送者信息
            UserInfo sender = userCache.get(msg.getSenderId());
            if (sender == null) {
                sender = userInfoService.getById(msg.getSenderId());
                if (sender != null) {
                    userCache.put(msg.getSenderId(), sender);
                }
            }
            
            String nickname = sender != null ? sender.getNickname() : "未知用户";
            String avatar = sender != null ? sender.getAvatar() : null;
            
            json.put("senderNickname", nickname);
            json.put("senderAvatar", avatar);
            result.add(json);
        }
        
        return result;
    }
    
    @Override
    public void recallMessage(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能撤回自己发送的消息");
        }
        
        // 检查是否超过2分钟
        long minutesDiff = java.time.Duration.between(message.getSendTime(), LocalDateTime.now()).toMinutes();
        if (minutesDiff > 2) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }
        
        // 标记为已撤回，而不是删除
        message.setIsRecalled(1);
        message.setContent("[消息已撤回]");
        this.updateById(message);
        
        log.info("用户{}撤回了消息{}", userId, messageId);
    }
    
    @Override
    public void deletePrivateMessages(Long userId, Long friendId) {
        log.info("删除用户{}和{}之间的私聊消息记录", userId, friendId);
        
        this.lambdaUpdate()
                .eq(ChatMessage::getSenderId, userId)
                .eq(ChatMessage::getReceiverId, friendId)
                .remove();
        
        this.lambdaUpdate()
                .eq(ChatMessage::getSenderId, friendId)
                .eq(ChatMessage::getReceiverId, userId)
                .remove();
        
        log.info("成功删除用户{}和{}之间的私聊消息记录", userId, friendId);
    }
    
    @Override
    public void deleteGroupMessages(String groupId) {
        log.info("删除群组{}的所有消息记录", groupId);
        
        this.lambdaUpdate()
                .eq(ChatMessage::getGroupId, groupId)
                .remove();
        
        log.info("成功删除群组{}的所有消息记录", groupId);
    }
    
    @Override
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 只能删除自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能删除自己发送的消息");
        }
        
        // 物理删除消息
        this.removeById(messageId);
        
        log.info("用户{}删除了消息{}", userId, messageId);
    }
}
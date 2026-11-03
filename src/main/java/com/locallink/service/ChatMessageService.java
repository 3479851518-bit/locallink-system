package com.locallink.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    
    void sendMessage(ChatMessage message);
    
    List<ChatMessage> getPrivateHistory(Long userId, Long friendId, Integer limit);
    
    /**
     * 获取私聊历史消息（包含发送者昵称和头像）
     */
    List<JSONObject> getPrivateHistoryWithUserInfo(Long userId, Long friendId, Integer limit);
    
    List<ChatMessage> getGroupHistory(String groupId, Integer limit);
    
    /**
     * 获取群聊历史消息（包含发送者昵称）
     */
    List<JSONObject> getGroupHistoryWithNickname(String groupId, Integer limit);
    
    void recallMessage(Long messageId, Long userId);
    
    /**
     * 删除私聊消息记录
     */
    void deletePrivateMessages(Long userId, Long friendId);
    
    /**
     * 删除群聊消息记录
     */
    void deleteGroupMessages(String groupId);
    
    /**
     * 删除单条消息
     */
    void deleteMessage(Long messageId, Long userId);
}

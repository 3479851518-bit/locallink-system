package com.locallink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.entity.ChatMessage;
import com.locallink.entity.UserInfo;
import com.locallink.mapper.ChatMessageMapper;
import com.locallink.mapper.UserInfoMapper;
import com.locallink.service.AdminService;
import com.locallink.service.SysNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements AdminService {
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private SysNotificationService notificationService;
    
    @Override
    public void banUser(Long userId, Long adminId, String reason) {
        // 不能封号自己
        if (userId.equals(adminId)) {
            throw new BusinessException("不能封号自己");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (user.getRole() != null && user.getRole() == 1) {
            throw new BusinessException("不能封号管理员");
        }
        
        user.setIsBanned(1);
        user.setBanReason(reason);
        user.setBanTime(LocalDateTime.now());
        
        this.updateById(user);
        log.info("管理员{}封号了用户{}，原因：{}", adminId, userId, reason);
        
        // 发送处罚通知
        String banReason = reason != null && !reason.isEmpty() ? reason : "违反平台规定";
        notificationService.createAndPushNotification(
            userId, 
            8, // 处罚通知
            "账号封禁通知", 
            "您的账号已被管理员封禁。原因：" + banReason, 
            null
        );
    }
    
    @Override
    public void unbanUser(Long userId, Long adminId) {
        // 不能解封自己（虽然逻辑上不太可能，但加上更安全）
        if (userId.equals(adminId)) {
            throw new BusinessException("不能解封自己");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 使用 UpdateWrapper 强制更新 null 值
        this.lambdaUpdate()
            .eq(UserInfo::getId, userId)
            .set(UserInfo::getIsBanned, 0)
            .set(UserInfo::getBanReason, null)
            .set(UserInfo::getBanTime, null)
            .update();
        
        log.info("管理员{}解封了用户{}", adminId, userId);
    }
    
    @Override
    public void muteUser(Long userId, Long adminId, String reason, LocalDateTime expireTime) {
        // 不能禁言自己
        if (userId.equals(adminId)) {
            throw new BusinessException("不能禁言自己");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (user.getRole() != null && user.getRole() == 1) {
            throw new BusinessException("不能禁言管理员");
        }
        
        user.setIsMuted(1);
        user.setMuteReason(reason);
        user.setMuteTime(LocalDateTime.now());
        user.setMuteExpireTime(expireTime);
        
        this.updateById(user);
        
        String expireInfo = expireTime != null ? "，到期时间：" + expireTime : "（永久禁言）";
        log.info("管理员{}禁言了用户{}，原因：{}{}", adminId, userId, reason, expireInfo);
        
        // 发送处罚通知
        String muteReason = reason != null && !reason.isEmpty() ? reason : "违反平台规定";
        String content = "您的账号已被禁言。原因：" + muteReason + expireInfo;
        notificationService.createAndPushNotification(
            userId, 
            8, // 处罚通知
            "禁言通知", 
            content, 
            null
        );
    }
    
    @Override
    public void unmuteUser(Long userId, Long adminId) {
        // 不能解禁自己
        if (userId.equals(adminId)) {
            throw new BusinessException("不能解禁自己");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 使用 UpdateWrapper 强制更新 null 值
        this.lambdaUpdate()
            .eq(UserInfo::getId, userId)
            .set(UserInfo::getIsMuted, 0)
            .set(UserInfo::getMuteReason, null)
            .set(UserInfo::getMuteTime, null)
            .set(UserInfo::getMuteExpireTime, null)
            .update();
        
        log.info("管理员{}解除了用户{}的禁言", adminId, userId);
    }
    
    @Override
    public List<UserInfo> getAllUsers() {
        return this.list();
    }
    
    @Override
    public void setUserRole(Long userId, Long adminId, Integer role) {
        // 不能修改自己的角色
        if (userId.equals(adminId)) {
            throw new BusinessException("不能修改自己的角色");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (role != 0 && role != 1) {
            throw new BusinessException("无效的角色值");
        }
        
        user.setRole(role);
        this.updateById(user);
        
        log.info("管理员{}将用户{}的角色设置为{}", adminId, userId, role == 1 ? "管理员" : "普通用户");
    }
    
    @Override
    public void submitAppeal(Long userId, String content) {
        // 获取所有管理员
        List<UserInfo> admins = this.list(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getRole, 1));
        
        if (admins.isEmpty()) {
            throw new BusinessException("没有可用的管理员");
        }
        
        // 为每个管理员创建一条申诉消息
        for (UserInfo admin : admins) {
            ChatMessage message = new ChatMessage();
            message.setSenderId(userId);
            message.setReceiverId(admin.getId());
            message.setChatType(1); // 私聊
            message.setMessageType(3); // 特殊类型：申诉消息
            message.setContent("[申诉] " + content);
            message.setSendTime(LocalDateTime.now());
            message.setIsRecalled(0);
            
            chatMessageMapper.insert(message);
            log.info("用户{}向管理员{}提交申诉: {}", userId, admin.getId(), content);
        }
        
        // 发送通知给用户，告知申诉已受理
        notificationService.createAndPushNotification(
            userId, 
            8, // 处罚通知（复用该类型用于系统反馈）
            "申诉受理通知", 
            "您的申诉已提交并受理，管理员将尽快处理。申诉内容：" + content, 
            null
        );
    }
    
    @Override
    public List<?> getPendingAppeals() {
        // 获取所有申诉类型的消息（未读/未处理）
        List<ChatMessage> appeals = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getMessageType, 3) // 申诉消息
                        .orderByDesc(ChatMessage::getSendTime)
        );
        
        // 按用户去重，只保留每个用户最新的一条申诉
        Map<Long, ChatMessage> latestAppealByUser = new LinkedHashMap<>();
        for (ChatMessage appeal : appeals) {
            Long userId = appeal.getSenderId();
            // 如果这个用户还没有记录，或者当前消息更新，则更新
            if (!latestAppealByUser.containsKey(userId)) {
                latestAppealByUser.put(userId, appeal);
            }
        }
        
        // 转换为包含用户信息的VO
        return latestAppealByUser.values().stream().map(appeal -> {
            Map<String, Object> appealInfo = new HashMap<>();
            appealInfo.put("id", appeal.getId());
            appealInfo.put("userId", appeal.getSenderId());
            appealInfo.put("content", appeal.getContent());
            appealInfo.put("sendTime", appeal.getSendTime());
            
            // 获取用户信息
            UserInfo user = this.getById(appeal.getSenderId());
            if (user != null) {
                appealInfo.put("nickname", user.getNickname());
                appealInfo.put("avatar", user.getAvatar());
                appealInfo.put("account", user.getAccount());
            }
            
            return appealInfo;
        }).collect(Collectors.toList());
    }
}

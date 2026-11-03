package com.locallink.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.common.ResultCode;
import com.locallink.entity.UserInfo;
import com.locallink.mapper.UserInfoMapper;
import com.locallink.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    
    @Override
    public UserInfo register(UserInfo userInfo) {
        // 安全检查：确保注册用户不能是管理员
        if (userInfo.getRole() != null && userInfo.getRole() == 1) {
            log.warn("尝试通过注册创建管理员账号: {}", userInfo.getAccount());
            throw new BusinessException("管理员账号不能通过注册创建，请联系系统管理员添加");
        }
        
        // 强制设置为普通用户
        userInfo.setRole(0);
        
        UserInfo existUser = this.getUserByAccount(userInfo.getAccount());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        
        if (userInfo.getUsername() == null || userInfo.getUsername().isBlank()) {
            userInfo.setUsername(userInfo.getAccount());
        }
        if (userInfo.getNickname() == null || userInfo.getNickname().isBlank()) {
            userInfo.setNickname(userInfo.getAccount());
        }
        
        userInfo.setPassword(BCrypt.hashpw(userInfo.getPassword()));
        userInfo.setStatus(0);
        this.save(userInfo);
        
        log.info("用户注册成功: {}", userInfo.getAccount());
        return userInfo;
    }
    
    @Override
    public UserInfo login(String account, String password) {
        UserInfo userInfo = this.getUserByAccount(account);
        if (userInfo == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        if (!BCrypt.checkpw(password, userInfo.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 检查用户是否被封号
        if (userInfo.getIsBanned() != null && userInfo.getIsBanned() == 1) {
            String banMessage = "您的账号已被封号，无法登录";
            if (userInfo.getBanReason() != null && !userInfo.getBanReason().isEmpty()) {
                banMessage += "。原因：" + userInfo.getBanReason();
            }
            if (userInfo.getBanTime() != null) {
                banMessage += "。封号时间：" + userInfo.getBanTime();
            }
            banMessage += "。如有异议，请联系管理员申诉。";
            throw new BusinessException(banMessage);
        }
        
        log.info("用户登录成功: {}", account);
        return userInfo;
    }
    
    @Override
    public void updateOnlineStatus(Long userId, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setStatus(status);
        this.updateById(userInfo);
        
        log.info("更新用户{}在线状态: {}", userId, status);
    }
    
    @Override
    public UserInfo getUserByAccount(String account) {
        return this.lambdaQuery()
                .eq(UserInfo::getAccount, account)
                .one();
    }
    
    @Override
    public void updateNickname(Long userId, String nickname) {
        log.info("用户{}尝试修改昵称为: {}", userId, nickname);
        
        UserInfo userInfo = this.getById(userId);
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BusinessException("昵称不能为空");
        }
        
        userInfo.setNickname(nickname);
        this.updateById(userInfo);
        
        log.info("用户{}成功修改昵称为: {}", userId, nickname);
    }
    
    @Override
    public void updateAvatar(Long userId, String avatar) {
        log.info("用户{}尝试修改头像", userId);
        
        UserInfo userInfo = this.getById(userId);
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        
        userInfo.setAvatar(avatar);
        this.updateById(userInfo);
        
        log.info("用户{}成功修改头像", userId);
    }
    
    @Override
    public String validateMessagePermission(Long userId) {
        UserInfo user = this.getById(userId);
        if (user == null) {
            return "用户不存在";
        }
        
        // 检查是否被封号
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            return "您的账号已被封号，无法发送消息。原因：" + 
                (user.getBanReason() != null ? user.getBanReason() : "未知");
        }
        
        // 检查是否被禁言
        if (user.getIsMuted() != null && user.getIsMuted() == 1) {
            // 检查禁言是否过期
            if (user.getMuteExpireTime() == null || user.getMuteExpireTime().isAfter(LocalDateTime.now())) {
                String muteInfo = "您的账号已被禁言";
                if (user.getMuteReason() != null) {
                    muteInfo += "，原因：" + user.getMuteReason();
                }
                if (user.getMuteExpireTime() != null) {
                    muteInfo += "，解禁时间：" + user.getMuteExpireTime();
                } else {
                    muteInfo += "（永久禁言）";
                }
                return muteInfo;
            } else {
                // 禁言已过期，自动解除
                user.setIsMuted(0);
                user.setMuteExpireTime(null);
                user.setMuteReason(null);
                this.updateById(user);
                log.info("用户{}的禁言已过期，自动解除", userId);
            }
        }
        
        return null; // 校验通过
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("用户{}尝试修改密码", userId);
        
        UserInfo userInfo = this.getById(userId);
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, userInfo.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        
        // 加密并更新新密码
        userInfo.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(userInfo);
        
        log.info("用户{}成功修改密码", userId);
    }
}

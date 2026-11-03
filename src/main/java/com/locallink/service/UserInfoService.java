package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.UserInfo;

public interface UserInfoService extends IService<UserInfo> {
    
    UserInfo register(UserInfo userInfo);
    
    UserInfo login(String account, String password);
    
    void updateOnlineStatus(Long userId, Integer status);
    
    UserInfo getUserByAccount(String account);
    
    /**
     * 修改用户昵称
     */
    void updateNickname(Long userId, String nickname);
    
    /**
     * 修改用户头像
     */
    void updateAvatar(Long userId, String avatar);
    
    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 校验用户发送消息的权限（封号、禁言等）
     * @param userId 用户ID
     * @return 校验结果，如果失败则抛出异常或返回错误信息
     */
    String validateMessagePermission(Long userId);
}

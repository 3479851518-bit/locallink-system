package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_info")
public class UserInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String account;
    
    private String password;
    
    private String avatar;
    
    private String ip;
    
    private Integer port;
    
    private String mac;
    
    private Integer status;
    
    private Integer role;  // 角色:0普通用户,1管理员
    
    private Integer isBanned;  // 是否封号:0未封号,1已封号
    
    private String banReason;  // 封号原因
    
    private LocalDateTime banTime;  // 封号时间
    
    private Integer isMuted;  // 是否禁言:0未禁言,1已禁言
    
    private String muteReason;  // 禁言原因
    
    private LocalDateTime muteTime;  // 禁言时间
    
    private LocalDateTime muteExpireTime;  // 禁言到期时间
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

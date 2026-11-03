package com.locallink.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestVO {
    
    private Long id;
    
    private Long userId;
    
    private Long friendId;
    
    private String applicantName;
    
    private String applicantAccount;
    
    private String applicantNickname;
    
    private String applicantAvatar;
    
    private Integer status;
    
    private LocalDateTime applyTime;
}

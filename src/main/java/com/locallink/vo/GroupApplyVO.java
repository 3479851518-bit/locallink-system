package com.locallink.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupApplyVO {
    
    private Long id;
    
    private String groupId;
    
    private String groupName;
    
    private Long userId;
    
    private String applicantName;
    
    private String applicantAccount;
    
    private String applicantNickname;
    
    private String applicantAvatar;
    
    private Integer applyStatus;
    
    private LocalDateTime applyTime;
    
    private LocalDateTime auditTime;
}

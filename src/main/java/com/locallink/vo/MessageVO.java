package com.locallink.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    
    private Long id;
    
    private Long senderId;
    
    private String senderNickname;
    
    private Long receiverId;
    
    private String groupId;
    
    private Integer chatType;
    
    private Integer messageType;
    
    private String content;
    
    private String filePath;
    
    private LocalDateTime sendTime;
}

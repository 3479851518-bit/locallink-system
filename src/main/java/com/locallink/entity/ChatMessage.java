package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String groupId;
    
    private Integer chatType;
    
    private Integer messageType;
    
    private String content;
    
    private String filePath;
    
    private Integer fileStatus;
    
    private LocalDateTime sendTime;
    
    private Integer isRead;
    
    private Integer isRecalled;  // 是否已撤回:0未撤回,1已撤回
    
    @TableLogic
    private Integer deleted;
}

package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_transfer")
public class FileTransfer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskId;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String fileName;
    
    private Long fileSize;
    
    private String filePath;
    
    private Long transferredBytes;
    
    private Integer transferType;
    
    private Integer status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime completeTime;
    
    @TableLogic
    private Integer deleted;
}

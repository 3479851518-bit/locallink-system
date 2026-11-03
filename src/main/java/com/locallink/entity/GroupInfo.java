package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_info")
public class GroupInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String groupId;
    
    private String groupName;
    
    private String groupAvatar;
    
    private Long creatorId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

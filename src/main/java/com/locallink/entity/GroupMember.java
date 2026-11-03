package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_member")
public class GroupMember {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String groupId;
    
    private Long userId;
    
    private Integer role;
    
    private Integer applyStatus;  // 申请状态:0待审核,1已通过,2已拒绝
    
    private LocalDateTime applyTime;  // 申请时间
    
    private LocalDateTime auditTime;  // 审核时间
    
    private LocalDateTime joinTime;
    
    @TableLogic
    private Integer deleted;
}

package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friend_relation")
public class FriendRelation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long friendId;
    
    private String remark;
    
    private Integer status;
    
    private Integer isBlocked;  // 是否拉黑:0未拉黑,1已拉黑
    
    private LocalDateTime applyTime;
    
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

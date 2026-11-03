package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class SysNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;  // 接收用户ID
    
    private Integer type;  // 通知类型:1好友申请,2好友通过,3群组邀请,4入群申请,5入群通过,6入群拒绝,7系统通知,8处罚通知
    
    private String title;  // 通知标题
    
    private String content;  // 通知内容
    
    private Long relatedId;  // 关联ID(好友申请ID/群组ID/处罚记录ID等)
    
    private Integer isRead;  // 是否已读:0未读,1已读
    
    private LocalDateTime createTime;  // 创建时间
    
    private LocalDateTime readTime;  // 阅读时间
}

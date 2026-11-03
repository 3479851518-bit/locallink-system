package com.locallink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_report")
public class UserReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reporterId;  // 举报人ID
    
    private Long reportedUserId;  // 被举报用户ID
    
    private Long messageId;  // 被举报消息ID
    
    private Integer reportType;  // 举报类型:1用户举报,2消息举报
    
    private Integer reasonCode;  // 举报原因代码:1发布垃圾广告,2骚扰他人,3欺诈行为,4不当内容,5冒充他人,6侵犯隐私,7其他原因
    
    private String reason;  // 举报详细说明
    
    private Integer status;  // 处理状态:0待处理,1已处理,2已驳回
    
    private String handleResult;  // 处理结果
    
    private Long handlerId;  // 处理人ID
    
    private LocalDateTime handleTime;  // 处理时间
    
    private Integer includeMessages;  // 是否包含聊天记录:0不包含,1包含
    
    private LocalDateTime createTime;  // 举报时间
    
    private Integer penaltyType;  // 处罚类型:0无处罚,1警告,2禁言,3封号,4永久封号
}

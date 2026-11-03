package com.locallink.vo;

import lombok.Data;

@Data
public class FriendVO {
    
    private Long id;
    
    private Long userId;
    
    private String nickname;
    
    private String avatar;
    
    private String ip;
    
    private Integer status;
    
    private String remark;
    
    private Boolean isBlocked;  // 是否被拉黑
}

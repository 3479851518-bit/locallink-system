package com.locallink.enums;

import lombok.Getter;

@Getter
public enum FriendStatusEnum {
    
    PENDING(0, "待验证"),
    ACCEPTED(1, "已通过"),
    REJECTED(2, "已拒绝");
    
    private final Integer code;
    private final String desc;
    
    FriendStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

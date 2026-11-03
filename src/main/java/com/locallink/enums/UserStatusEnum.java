package com.locallink.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌");
    
    private final Integer code;
    private final String desc;
    
    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

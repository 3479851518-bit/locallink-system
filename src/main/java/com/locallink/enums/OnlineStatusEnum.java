package com.locallink.enums;

import lombok.Getter;

@Getter
public enum OnlineStatusEnum {
    
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    AWAY(2, "离开"),
    BUSY(3, "忙碌"),
    DO_NOT_DISTURB(4, "请勿打扰");
    
    private final Integer code;
    private final String desc;
    
    OnlineStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static OnlineStatusEnum fromCode(Integer code) {
        for (OnlineStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return OFFLINE;
    }
}

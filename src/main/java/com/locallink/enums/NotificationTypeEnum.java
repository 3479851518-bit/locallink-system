package com.locallink.enums;

import lombok.Getter;

@Getter
public enum NotificationTypeEnum {
    
    FRIEND_REQUEST(1, "好友申请"),
    FRIEND_ACCEPTED(2, "好友通过"),
    GROUP_INVITE(3, "群组邀请"),
    GROUP_APPLY(4, "入群申请"),
    GROUP_APPROVED(5, "入群通过"),
    GROUP_REJECTED(6, "入群拒绝"),
    SYSTEM_NOTICE(7, "系统通知"),
    PENALTY_NOTICE(8, "处罚通知");
    
    private final Integer code;
    private final String desc;
    
    NotificationTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static NotificationTypeEnum fromCode(Integer code) {
        for (NotificationTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SYSTEM_NOTICE;
    }
}

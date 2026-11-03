package com.locallink.enums;

import lombok.Getter;

@Getter
public enum GroupApplyStatusEnum {
    
    PENDING(0, "待审核"),
    ACCEPTED(1, "已通过"),
    REJECTED(2, "已拒绝");
    
    private final Integer code;
    private final String desc;
    
    GroupApplyStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

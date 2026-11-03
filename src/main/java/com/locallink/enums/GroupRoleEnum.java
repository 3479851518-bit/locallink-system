package com.locallink.enums;

import lombok.Getter;

@Getter
public enum GroupRoleEnum {
    
    MEMBER(0, "普通成员"),
    ADMIN(1, "管理员"),
    OWNER(2, "群主");
    
    private final Integer code;
    private final String desc;
    
    GroupRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

package com.locallink.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");
    
    private final Integer code;
    private final String desc;
    
    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static GenderEnum fromCode(Integer code) {
        for (GenderEnum gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}

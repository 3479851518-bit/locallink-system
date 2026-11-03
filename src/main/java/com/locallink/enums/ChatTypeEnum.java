package com.locallink.enums;

import lombok.Getter;

@Getter
public enum ChatTypeEnum {
    
    PRIVATE(1, "私聊"),
    GROUP(2, "群聊");
    
    private final Integer code;
    private final String desc;
    
    ChatTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

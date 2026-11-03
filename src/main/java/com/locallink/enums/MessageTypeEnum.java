package com.locallink.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    FILE(3, "文件"),
    VOICE(4, "语音");
    
    private final Integer code;
    private final String desc;
    
    MessageTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

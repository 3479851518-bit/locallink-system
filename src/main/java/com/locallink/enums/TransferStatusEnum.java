package com.locallink.enums;

import lombok.Getter;

@Getter
public enum TransferStatusEnum {
    
    WAITING(0, "待传输"),
    TRANSFERRING(1, "传输中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");
    
    private final Integer code;
    private final String desc;
    
    TransferStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

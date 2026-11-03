package com.locallink.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {
    
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    RESOLVED(2, "已解决"),
    REJECTED(3, "已驳回");
    
    private final Integer code;
    private final String desc;
    
    ReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static ReportStatusEnum fromCode(Integer code) {
        for (ReportStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}

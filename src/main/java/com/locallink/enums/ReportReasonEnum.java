package com.locallink.enums;

import lombok.Getter;

@Getter
public enum ReportReasonEnum {
    
    SPAM(1, "发布垃圾广告"),
    HARASSMENT(2, "骚扰他人"),
    FRAUD(3, "欺诈行为"),
    INAPPROPRIATE_CONTENT(4, "不当内容"),
    IMPERSONATION(5, "冒充他人"),
    PRIVACY_VIOLATION(6, "侵犯隐私"),
    OTHER(7, "其他原因");
    
    private final Integer code;
    private final String desc;
    
    ReportReasonEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

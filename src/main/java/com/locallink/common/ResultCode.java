package com.locallink.common;

public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    FRIEND_NOT_EXIST(2001, "好友不存在"),
    FRIEND_ALREADY_EXIST(2002, "已是好友"),
    FRIEND_REQUEST_PENDING(2003, "好友申请待处理"),
    GROUP_NOT_EXIST(3001, "群组不存在"),
    FILE_NOT_EXIST(4001, "文件不存在"),
    TRANSFER_ERROR(4002, "传输错误");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

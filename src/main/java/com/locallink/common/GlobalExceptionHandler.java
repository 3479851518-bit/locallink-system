package com.locallink.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        String message = e.getMessage() != null ? e.getMessage() : "业务处理失败";
        return Result.fail(e.getCode(), message);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(ParamException.class)
    public Result<Void> handleParamException(ParamException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(UserException.class)
    public Result<Void> handleUserException(UserException e) {
        log.warn("用户异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(FriendException.class)
    public Result<Void> handleFriendException(FriendException e) {
        log.warn("好友异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(GroupException.class)
    public Result<Void> handleGroupException(GroupException e) {
        log.warn("群组异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MessageException.class)
    public Result<Void> handleMessageException(MessageException e) {
        log.warn("消息异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(FileException.class)
    public Result<Void> handleFileException(FileException e) {
        log.warn("文件异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(TransferException.class)
    public Result<Void> handleTransferException(TransferException e) {
        log.warn("传输异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(AdminException.class)
    public Result<Void> handleAdminException(AdminException e) {
        log.warn("管理员异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        String message = ExceptionUtils.resolveMessage(e);
        // 对于数据库连接等敏感信息，不直接暴露给前端
        if (message.contains("Access denied") || message.contains("Communications link failure")) {
            message = "数据库连接失败，请检查 application.yml 中的数据库配置并确保已导入 locallink.sql";
        } else if (message.contains("Table") && message.contains("doesn't exist")) {
            message = "数据库表不存在，请先执行 locallink.sql 初始化数据库";
        } else if ("NullPointerException".equals(message)) {
            message = "系统内部错误，请查看后端日志";
        }
        return Result.fail(message);
    }
}

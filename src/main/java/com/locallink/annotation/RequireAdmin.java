package com.locallink.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限校验注解
 * 用于标记需要管理员权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
    
    /**
     * 错误消息
     */
    String message() default "需要管理员权限";
}

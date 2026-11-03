package com.locallink.aspect;

import com.locallink.annotation.RequireAdmin;
import com.locallink.common.UnauthorizedException;
import com.locallink.entity.UserInfo;
import com.locallink.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 权限校验切面
 * 处理@RequireAdmin注解的权限校验
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Before("@annotation(com.locallink.annotation.RequireAdmin) || @within(com.locallink.annotation.RequireAdmin)")
    public void checkAdminPermission(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("无法获取请求上下文");
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 从请求参数中获取userId（根据实际情况调整）
        Long userId = getUserIdFromRequest(request, joinPoint);
        
        if (userId == null) {
            throw new UnauthorizedException("未提供用户ID");
        }
        
        // 查询用户信息
        UserInfo user = userInfoService.getById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        
        // 检查是否为管理员（role=1表示管理员）
        if (user.getRole() == null || user.getRole() != 1) {
            RequireAdmin requireAdmin = getRequireAdminAnnotation(joinPoint);
            String message = requireAdmin != null ? requireAdmin.message() : "需要管理员权限";
            log.warn("用户{}尝试访问管理员接口被拒绝: {}", userId, request.getRequestURI());
            throw new UnauthorizedException(message);
        }
        
        log.debug("用户{}通过管理员权限校验", userId);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request, JoinPoint joinPoint) {
        // 尝试从请求参数获取
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            try {
                return Long.parseLong(userIdParam);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID格式: {}", userIdParam);
            }
        }
        
        // 尝试从方法参数获取
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        
        return null;
    }
    
    /**
     * 获取RequireAdmin注解
     */
    private RequireAdmin getRequireAdminAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        RequireAdmin annotation = method.getAnnotation(RequireAdmin.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequireAdmin.class);
        }
        
        return annotation;
    }
}

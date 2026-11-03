package com.locallink.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志切面
 * 记录Controller层的请求和响应日志
 */
@Slf4j
@Aspect
@Component
public class LogAspect {
    
    @Pointcut("execution(* com.locallink.controller..*.*(..))")
    public void controllerPointcut() {
    }
    
    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        log.info("========== 请求开始 ==========");
        log.info("URL: {}", request.getRequestURL().toString());
        log.info("HTTP Method: {}", request.getMethod());
        log.info("IP: {}", request.getRemoteAddr());
        log.info("Class Method: {}.{}", 
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName());
        log.info("Request Params: {}", Arrays.toString(joinPoint.getArgs()));
    }
    
    @AfterReturning(returning = "ret", pointcut = "controllerPointcut()")
    public void doAfterReturning(Object ret) {
        log.info("Response Data: {}", ret);
        log.info("========== 请求结束 ==========");
    }
    
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}: {}", 
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            e.getMessage(),
            e);
    }
}

package com.epidemic.material.aspect;

import com.epidemic.common.feign.LogFeignClient;
import com.epidemic.material.annotation.OperateLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截所有标注 @OperateLog 注解的方法，记录操作日志
 */
@Aspect
@Component
public class OperateLogAspect {

    private final LogFeignClient logFeignClient;

    public OperateLogAspect(LogFeignClient logFeignClient) {
        this.logFeignClient = logFeignClient;
    }

    /**
     * 定义切入点：所有标注 @OperateLog 注解的方法
     */
    @Pointcut("@annotation(com.epidemic.material.annotation.OperateLog)")
    public void operateLogPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("operateLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        com.epidemic.common.entity.OperateLog operateLog = new com.epidemic.common.entity.OperateLog();

        try {
            // 获取请求信息
            HttpServletRequest request = getHttpServletRequest();

            // 获取注解信息
            OperateLog annotation = getMethodAnnotation(joinPoint);
            operateLog.setModule(annotation.module());
            operateLog.setOperation(annotation.operation());

            // 获取用户信息（从请求头）
            operateLog.setUserId(getUserId(request));
            operateLog.setUsername(getUsername(request));

            // 获取请求参数
            operateLog.setParams(getParams(joinPoint));

            // 获取 IP 地址
            operateLog.setIp(getIpAddress(request));

            // 获取请求方法
            operateLog.setMethod(getMethodName(joinPoint));

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 设置成功状态
            operateLog.setStatus(1);

            return result;

        } catch (Throwable e) {
            // 设置失败状态
            operateLog.setStatus(0);
            operateLog.setErrorMsg(e.getMessage());
            throw e;

        } finally {
            // 设置执行时长
            long executeTime = System.currentTimeMillis() - startTime;
            operateLog.setExecuteTime(executeTime);
            operateLog.setOperateTime(LocalDateTime.now());

            // 异步保存日志（避免影响业务响应）
            saveLogAsync(operateLog);
        }
    }

    /**
     * 异步保存日志（通过 Feign 调用）
     */
    @Async
    public void saveLogAsync(com.epidemic.common.entity.OperateLog operateLog) {
        try {
            logFeignClient.saveLog(operateLog);
        } catch (Exception e) {
            // 记录日志失败不影响业务
            System.err.println("保存操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取 HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取方法上的 @OperateLog 注解
     */
    private OperateLog getMethodAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 如果是代理类，需要获取目标类的方法
        Method currentMethod = joinPoint.getTarget().getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        return currentMethod.getAnnotation(OperateLog.class);
    }

    /**
     * 获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        if (request == null) return null;
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取用户名
     */
    private String getUsername(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("X-User-Name");
    }

    /**
     * 获取请求参数
     */
    private String getParams(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || args == null || paramNames.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramNames.length; i++) {
            // 跳过 HttpServletRequest 和 Response 参数
            if (args[i] instanceof HttpServletRequest) {
                continue;
            }
            if (args[i] == null) {
                continue;
            }
            String argStr = args[i].toString();
            if (argStr.contains("HttpServlet")) {
                continue;
            }
            // 敏感参数过滤（密码等）
            if ("password".equalsIgnoreCase(paramNames[i]) || "pwd".equalsIgnoreCase(paramNames[i])) {
                argStr = "******";
            }
            if (sb.length() > 0) sb.append("&");
            sb.append(paramNames[i]).append("=").append(argStr);
        }
        return sb.toString();
    }

    /**
     * 获取 IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) return null;

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取方法名（类.方法）
     */
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getName();
    }
}
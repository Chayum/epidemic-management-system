package com.epidemic.common.util;

import com.epidemic.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 统一获取当前登录用户的信息
 * 从网关透传的Header中获取用户信息
 */
public class UserContext {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     * @throws BusinessException 如果用户未登录或Header不存在
     */
    public static Long getUserId() {
        String userIdStr = getHeader(HEADER_USER_ID);
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new BusinessException(401, "用户未登录");
        }
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "无效的用户ID");
        }
    }

    /**
     * 获取当前用户ID，如果不存在返回null
     *
     * @return 用户ID或null
     */
    public static Long getUserIdOrNull() {
        String userIdStr = getHeader(HEADER_USER_ID);
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
     * 获取当前用户名
     *
     * @return 用户名，如果不存在返回null
     */
    public static String getUsername() {
        return getHeader(HEADER_USER_NAME);
    }

    /**
     * 获取当前用户名，如果不存在返回默认值的拼接
     *
     * @param defaultPrefix 默认前缀
     * @return 用户名
     */
    public static String getUsernameOrDefault(String defaultPrefix) {
        String username = getHeader(HEADER_USER_NAME);
        if (username == null || username.isEmpty()) {
            Long userId = getUserIdOrNull();
            return userId != null ? defaultPrefix + userId : "System";
        }
        return username;
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色
     */
    public static String getUserRole() {
        return getHeader(HEADER_USER_ROLE);
    }

    /**
     * 从请求Header中获取指定名称的值
     *
     * @param headerName Header名称
     * @return Header值，如果不存在返回null
     */
    private static String getHeader(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(headerName);
    }
}

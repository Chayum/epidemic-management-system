package com.epidemic.pandemic.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.pandemic.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户通知控制器
 */
@Tag(name = "用户通知", description = "用户通知相关接口")
@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {

    @Autowired
    private UserNotificationService userNotificationService;

    @Operation(summary = "获取我的通知列表")
    @GetMapping("/user")
    public Result<PageResult<Map<String, Object>>> getMyNotifications(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        return Result.success(userNotificationService.getUserNotifications(userId, page, size, isRead));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        return Result.success(userNotificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        userNotificationService.markAsRead(id, userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        userNotificationService.markAllAsRead(userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        userNotificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
    }
}
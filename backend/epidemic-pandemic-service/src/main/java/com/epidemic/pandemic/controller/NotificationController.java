package com.epidemic.pandemic.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.util.UserContext;
import com.epidemic.pandemic.annotation.OperateLog;
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
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead) {
        Long userId = UserContext.getUserId();
        return Result.success(userNotificationService.getUserNotifications(userId, page, size, isRead));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        return Result.success(userNotificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    @OperateLog(module = "用户通知", operation = "标记已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        userNotificationService.markAsRead(id, userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    @OperateLog(module = "用户通知", operation = "标记全部已读")
    public Result<String> markAllAsRead() {
        Long userId = UserContext.getUserId();
        userNotificationService.markAllAsRead(userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @OperateLog(module = "用户通知", operation = "删除通知")
    public Result<String> deleteNotification(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        userNotificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
    }
}
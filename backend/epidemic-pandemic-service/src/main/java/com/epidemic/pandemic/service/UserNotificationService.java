package com.epidemic.pandemic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.pandemic.entity.UserNotification;

import java.util.List;
import java.util.Map;

/**
 * 用户通知服务接口
 */
public interface UserNotificationService extends IService<UserNotification> {

    /**
     * 获取用户通知列表（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param isRead 是否已读（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size, Integer isRead);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 创建用户通知（批量）
     * @param userIds 用户ID列表
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param pushRecordId 推送记录ID
     */
    void createNotifications(List<Long> userIds, String title, String content, String type, Long pushRecordId);

    /**
     * 删除用户通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void deleteNotification(Long notificationId, Long userId);
}
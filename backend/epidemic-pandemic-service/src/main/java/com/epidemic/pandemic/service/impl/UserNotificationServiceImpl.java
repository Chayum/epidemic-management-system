package com.epidemic.pandemic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.result.PageResult;
import com.epidemic.pandemic.entity.UserNotification;
import com.epidemic.pandemic.mapper.UserNotificationMapper;
import com.epidemic.pandemic.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户通知服务实现类
 */
@Service
@Slf4j
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification> implements UserNotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size, Integer isRead) {
        Page<UserNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId);

        if (isRead != null) {
            wrapper.eq(UserNotification::getIsRead, isRead);
        }
        wrapper.orderByDesc(UserNotification::getCreateTime);

        Page<UserNotification> result = baseMapper.selectPage(pageParam, wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (UserNotification notification : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notification.getId());
            map.put("title", notification.getTitle());
            map.put("content", notification.getContent());
            map.put("type", notification.getType());
            map.put("isRead", notification.getIsRead());
            map.put("createTime", notification.getCreateTime() != null ? notification.getCreateTime().format(FORMATTER) : "");
            map.put("pushRecordId", notification.getPushRecordId());
            list.add(map);
        }

        return PageResult.of(list, result.getTotal(), page, size);
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
               .eq(UserNotification::getIsRead, 0);
        return count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getId, notificationId)
               .eq(UserNotification::getUserId, userId);

        UserNotification notification = baseMapper.selectOne(wrapper);
        if (notification != null && notification.getIsRead() == 0) {
            notification.setIsRead(1);
            baseMapper.updateById(notification);
            log.info("标记通知已读：notificationId={}, userId={}", notificationId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
               .eq(UserNotification::getIsRead, 0);

        List<UserNotification> unreadList = baseMapper.selectList(wrapper);
        for (UserNotification notification : unreadList) {
            notification.setIsRead(1);
            baseMapper.updateById(notification);
        }
        log.info("标记所有通知已读：userId={}, count={}", userId, unreadList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotifications(List<Long> userIds, String title, String content, String type, Long pushRecordId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserNotification> notifications = new ArrayList<>();

        for (Long userId : userIds) {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type != null ? type : "push");
            notification.setIsRead(0);
            notification.setPushRecordId(pushRecordId);
            notification.setCreateTime(now);
            notifications.add(notification);
        }

        saveBatch(notifications);
        log.info("批量创建用户通知：count={}, pushRecordId={}", userIds.size(), pushRecordId);
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getId, notificationId)
               .eq(UserNotification::getUserId, userId);
        baseMapper.delete(wrapper);
        log.info("删除用户通知：notificationId={}, userId={}", notificationId, userId);
    }
}
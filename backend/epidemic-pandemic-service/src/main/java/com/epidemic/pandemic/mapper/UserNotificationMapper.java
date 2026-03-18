package com.epidemic.pandemic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.pandemic.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知Mapper接口
 */
@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}
package com.epidemic.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT role, COUNT(*) as count FROM sys_user WHERE status = 'active' GROUP BY role")
    List<Map<String, Object>> selectRoleCounts();
}

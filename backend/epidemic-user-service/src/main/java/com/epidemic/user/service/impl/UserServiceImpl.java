package com.epidemic.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.result.PageResult;
import com.epidemic.user.entity.User;
import com.epidemic.user.mapper.UserMapper;
import com.epidemic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 提供用户管理的核心业务逻辑，包括用户的增删改查、状态管理及批量操作
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     * 支持根据用户名、姓名、手机号、角色、状态进行模糊/精确组合查询
     *
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（模糊）
     * @param name 姓名（模糊）
     * @param phone 手机号（模糊）
     * @param role 角色
     * @param status 状态
     * @return 分页结果对象
     */
    @Override
    public PageResult<User> getUserList(Integer page, Integer size, String username, String name, String phone, String role, String status) {
        // 构建分页参数
        Page<User> pageParam = new Page<>(page, size);
        
        // 构建查询条件
        Page<User> resultPage = userMapper.selectPage(pageParam, new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(username), User::getUsername, username)
                .like(StringUtils.hasText(name), User::getName, name)
                .like(StringUtils.hasText(phone), User::getPhone, phone)
                .eq(StringUtils.hasText(role), User::getRole, role)   // 角色通常为精确匹配
                .eq(StringUtils.hasText(status), User::getStatus, status) // 状态通常为精确匹配
        );
        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal(), page, size);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户实体
     */
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 新增用户
     * 设置初始创建时间及默认激活状态
     *
     * @param user 待新增的用户信息
     */
    @Override
    public void addUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        // 默认状态为激活
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("active");
        }
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     * 自动更新修改时间
     *
     * @param user 包含更新字段的用户对象
     */
    @Override
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    /**
     * 更新单个用户状态
     * 如启用/禁用账户
     *
     * @param id 用户ID
     * @param status 新状态
     */
    @Override
    public void updateUserStatus(Long id, String status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 批量更新用户状态
     * 循环更新每个用户的状态（可优化为批量SQL）
     *
     * @param ids 用户ID列表
     * @param status 新状态
     */
    @Override
    public void batchUpdateStatus(List<Long> ids, String status) {
        // 实际生产中建议使用 MyBatis 的 foreach 批量更新以提升性能
        // 这里暂时保持循环单条更新
        for (Long id : ids) {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     */
    @Override
    public void batchDelete(List<Long> ids) {
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public List<Map<String, Object>> getRoleCounts() {
        return userMapper.selectRoleCounts();
    }

    @Override
    public List<Long> getUserIdsByRole(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, "active");
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        } else {
            wrapper.ne(User::getRole, "admin");
        }
        return userMapper.selectList(wrapper).stream().map(User::getId).collect(Collectors.toList());
    }
}

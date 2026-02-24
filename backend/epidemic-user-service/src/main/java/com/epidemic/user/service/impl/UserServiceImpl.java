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

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final UserMapper userMapper;

    @Override
    public PageResult<User> getUserList(Integer page, Integer size, String username, String name, String phone, String role, String status) {
        Page<User> pageParam = new Page<>(page, size);
        Page<User> resultPage = userMapper.selectPage(pageParam, new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(username), User::getUsername, username)
                .like(StringUtils.hasText(name), User::getName, name)
                .like(StringUtils.hasText(phone), User::getPhone, phone)
                .like(StringUtils.hasText(role), User::getRole, role)
                .like(StringUtils.hasText(status), User::getStatus, status)
        );
        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal(), page, size);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void addUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setStatus("active");
        userMapper.insert(user);
    }

    @Override
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void updateUserStatus(Long id, String status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, String status) {
        for (Long id : ids) {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    @Override
    public void batchDelete(List<Long> ids) {
        userMapper.deleteBatchIds(ids);
    }
}

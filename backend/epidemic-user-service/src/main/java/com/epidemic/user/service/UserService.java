package com.epidemic.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.user.dto.UserQueryDTO;
import com.epidemic.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    PageResult<User> getUserList(UserQueryDTO queryDTO);

    User getUserById(Long id);

    void addUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

    void updateUserStatus(Long id, String status);

    void batchUpdateStatus(java.util.List<Long> ids, String status);

    void batchDelete(java.util.List<Long> ids);

    List<Map<String, Object>> getRoleCounts();

    List<Long> getUserIdsByRole(String role);
}

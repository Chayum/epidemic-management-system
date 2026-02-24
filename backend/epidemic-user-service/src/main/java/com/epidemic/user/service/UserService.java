package com.epidemic.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.user.entity.User;
import org.springframework.stereotype.Service;

public interface UserService extends IService<User> {
    PageResult<User> getUserList(Integer page, Integer size, String username, String name, String phone, String role, String status);
    
    User getUserById(Long id);
    
    void addUser(User user);
    
    void updateUser(User user);
    
    void deleteUser(Long id);
    
    void updateUserStatus(Long id, String status);
    
    void batchUpdateStatus(java.util.List<Long> ids, String status);
    
    void batchDelete(java.util.List<Long> ids);
}

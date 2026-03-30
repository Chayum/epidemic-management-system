package com.epidemic.user.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.annotation.OperateLog;
import com.epidemic.user.dto.UserQueryDTO;
import com.epidemic.user.entity.User;
import com.epidemic.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户的增删改查、状态管理等功能接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表
     * @param queryDTO 查询参数
     * @return 分页后的用户列表
     */
    @GetMapping("/list")
    public Result<PageResult<User>> list(@ModelAttribute UserQueryDTO queryDTO) {
        return Result.success(userService.getUserList(queryDTO));
    }

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户实体对象
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     * @param user 用户实体对象
     * @return 操作结果消息
     */
    @PostMapping
    @OperateLog(module = "用户管理", operation = "创建用户")
    public Result<String> add(@RequestBody User user) {
        userService.addUser(user);
        return Result.success("添加成功");
    }

    /**
     * 更新用户信息
     * @param user 用户实体对象（必须包含ID）
     * @return 操作结果消息
     */
    @PutMapping
    @OperateLog(module = "用户管理", operation = "更新用户")
    public Result<String> update(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success("修改成功");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果消息
     */
    @DeleteMapping("/{id}")
    @OperateLog(module = "用户管理", operation = "删除用户")
    public Result<String> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 新状态
     * @return 操作结果消息
     */
    @PutMapping("/status/{id}")
    @OperateLog(module = "用户管理", operation = "更新用户状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 批量更新用户状态
     * @param params 包含ids列表和status字符串的Map
     * @return 操作结果消息
     */
    @PutMapping("/batch/status")
    @OperateLog(module = "用户管理", operation = "批量更新用户状态")
    public Result<String> batchUpdateStatus(@RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        String status = (String) params.get("status");
        userService.batchUpdateStatus(ids, status);
        return Result.success("批量状态更新成功");
    }

    /**
     * 批量删除用户
     * @param params 包含ids列表的Map
     * @return 操作结果消息
     */
    @DeleteMapping("/batch")
    @OperateLog(module = "用户管理", operation = "批量删除用户")
    public Result<String> batchDelete(@RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        userService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 获取用户角色统计
     * @return 各角色用户数量
     */
    @GetMapping("/stats/role-counts")
    public Result<List<Map<String, Object>>> getRoleCounts() {
        return Result.success(userService.getRoleCounts());
    }

    /**
     * 根据角色获取用户ID列表
     * @param role 角色（可选，为空则返回除admin外的所有用户ID）
     * @return 用户ID列表
     */
    @GetMapping("/ids/by-role")
    public Result<List<Long>> getUserIdsByRole(@RequestParam(required = false) String role) {
        return Result.success(userService.getUserIdsByRole(role));
    }
}

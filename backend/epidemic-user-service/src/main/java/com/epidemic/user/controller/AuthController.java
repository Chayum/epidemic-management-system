package com.epidemic.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.result.Result;
import com.epidemic.user.dto.LoginRequest;
import com.epidemic.user.dto.LoginResponse;
import com.epidemic.user.entity.User;
import com.epidemic.user.service.UserService;
import com.epidemic.user.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 认证管理控制器
 * 处理用户登录、登出、密码修改及个人信息管理等认证相关操作
 */
@Tag(name = "认证管理", description = "用户登录、注册、认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录接口
     * 校验用户名、密码、状态及角色权限，生成JWT令牌
     *
     * @param loginRequest 登录请求参数（包含用户名、密码、角色）
     * @return 登录结果（包含Token和用户信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 构建查询条件，根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginRequest.getUsername());
        User user = userService.getOne(wrapper);

        // 校验用户是否存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验密码（此处应使用加密比对，当前为明文比对示例）
        if (!Objects.equals(user.getPassword(), loginRequest.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 校验用户状态
        if (!"active".equals(user.getStatus())) {
            return Result.error("账户已被禁用");
        }

        // 校验登录角色权限
        // 逻辑说明：
        // 1. 若前端请求指明了角色（如管理端登录请求role=admin），则必须匹配数据库中的角色
        // 2. 管理端只能由admin角色登录，用户端只能由非admin角色登录
        String requestRole = loginRequest.getRole();
        if (StringUtils.hasText(requestRole)) {
            if ("admin".equals(requestRole)) {
                // 管理端登录，要求用户角色必须是 admin
                if (!"admin".equals(user.getRole())) {
                    return Result.error("账户或密码错误");
                }
            } else {
                // 用户端登录，要求用户角色不能是 admin
                if ("admin".equals(user.getRole())) {
                    return Result.error("账户或密码错误");
                }
            }
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户[{}]登录成功, Token已生成", user.getUsername());
        
        // 构建响应数据
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getName());
        userInfo.setRole(user.getRole());
        userInfo.setPhone(user.getPhone());
        userInfo.setUnit(user.getUnit());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCreateTime(user.getCreateTime());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(userInfo);

        return Result.success(response);
    }

    /**
     * 用户登出接口
     * 当前仅返回成功，实际Token失效通常由前端清除或配合Redis黑名单实现
     *
     * @return 登出结果
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * 根据Token解析用户ID并查询详情
     *
     * @param authHeader 请求头中的Authorization字段
     * @return 用户详情（不含密码）
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(@RequestHeader("Authorization") String authHeader) {
        // 解析Token获取UserID
        String token = authHeader.substring(7); // 去除 "Bearer " 前缀
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 查询用户并脱敏密码
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改密码接口
     *
     * @param authHeader 请求头
     * @param params 参数Map，需包含 oldPwd 和 newPwd
     * @return 修改结果
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<String> changePassword(@RequestHeader("Authorization") String authHeader, 
                                         @RequestBody java.util.Map<String, String> params) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String oldPwd = params.get("oldPwd");
        String newPwd = params.get("newPwd");

        if (!StringUtils.hasText(oldPwd) || !StringUtils.hasText(newPwd)) {
            return Result.error("密码不能为空");
        }

        User user = userService.getUserById(userId);
        
        // 校验原密码
        if (!oldPwd.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 更新新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(newPwd);
        userService.updateUser(updateUser);
        
        return Result.success("密码修改成功");
    }

    /**
     * 更新用户个人资料
     * 允许更新姓名、电话、单位等非敏感信息
     *
     * @param authHeader 请求头
     * @param updateUser 包含待更新字段的用户对象
     * @return 更新结果
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody User updateUser) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        User user = new User();
        user.setId(userId);
        
        // 仅更新非空字段
        if (StringUtils.hasText(updateUser.getName())) {
            user.setName(updateUser.getName());
        }
        if (StringUtils.hasText(updateUser.getPhone())) {
            user.setPhone(updateUser.getPhone());
        }
        if (StringUtils.hasText(updateUser.getUnit())) {
            user.setUnit(updateUser.getUnit());
        }
        
        userService.updateUser(user);
        return Result.success("更新成功");
    }
}

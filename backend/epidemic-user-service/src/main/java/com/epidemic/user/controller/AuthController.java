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

@Tag(name = "认证管理", description = "用户登录、注册、认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginRequest.getUsername());
        User user = userService.getOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!Objects.equals(user.getPassword(), loginRequest.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        if (!"active".equals(user.getStatus())) {
            return Result.error("账户已被禁用");
        }
        //生成jwt令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("jwt:#{}",token);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getName());
        userInfo.setRole(user.getRole());
        userInfo.setPhone(user.getPhone());
        userInfo.setUnit(user.getUnit());
        userInfo.setAvatar(user.getAvatar());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(userInfo);

        return Result.success(response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

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
        
        if (!oldPwd.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(newPwd);
        userService.updateUser(updateUser);
        
        return Result.success("密码修改成功");
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody User updateUser) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        User user = new User();
        user.setId(userId);
        
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

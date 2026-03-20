package com.epidemic.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.result.Result;
import com.epidemic.user.annotation.OperateLog;
import com.epidemic.user.dto.LoginRequest;
import com.epidemic.user.entity.User;
import com.epidemic.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证管理控制器
 * 处理用户登录、登出、密码修改及个人信息管理等认证相关操作
 */
@Tag(name = "认证管理", description = "用户登录、注册、认证相关接口")
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录接口
     * 校验用户名、密码、状态及角色权限
     * 网关层会拦截此接口响应，生成Token并封装为LoginResponse
     *
     * @param loginRequest 登录请求参数（包含用户名、密码、角色）
     * @return 用户信息
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperateLog(module = "认证管理", operation = "用户登录")
    public Result<User> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        
        // 检查登录失败次数
        String failKey = "auth:login:fail:" + username;
        Object failCountObj = redisTemplate.opsForValue().get(failKey);
        Long failCount = null;
        if (failCountObj != null) {
            if (failCountObj instanceof Integer) {
                failCount = ((Integer) failCountObj).longValue();
            } else if (failCountObj instanceof Long) {
                failCount = (Long) failCountObj;
            }
        }
        if (failCount != null && failCount >= 5) {
            Long expireTime = redisTemplate.getExpire(failKey);
            if (expireTime != null && expireTime > 0) {
                return Result.error("密码错误次数过多，请 " + expireTime + " 秒后再试");
            }
        }

        // 构建查询条件，根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userService.getOne(wrapper);

        // 校验用户是否存在
        if (user == null) {
            // 增加失败次数
            incrementLoginFailCount(failKey);
            return Result.error("用户不存在");
        }

        // 校验密码（兼容旧明文密码，匹配成功后自动升级为BCrypt）
        String rawPassword = loginRequest.getPassword();
        String dbPassword = user.getPassword();
        boolean passwordMatched = false;

        if (passwordEncoder.matches(rawPassword, dbPassword)) {
            // BCrypt格式密码匹配成功
            passwordMatched = true;
        } else if (rawPassword.equals(dbPassword)) {
            // 旧明文密码匹配成功，需要升级为BCrypt
            passwordMatched = true;
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setPassword(passwordEncoder.encode(rawPassword));
            userService.updateUser(updateUser);
            log.info("用户 {} 的密码已从明文升级为BCrypt加密", username);
        }

        if (!passwordMatched) {
            // 增加失败次数
            incrementLoginFailCount(failKey);
            return Result.error("用户名或密码错误");
        }

        // 校验用户状态
        if (!"active".equals(user.getStatus())) {
            return Result.error("账户已被禁用");
        }

        // 校验登录角色权限
        String requestRole = loginRequest.getRole();
        if (StringUtils.hasText(requestRole)) {
            if ("admin".equals(requestRole)) {
                if (!"admin".equals(user.getRole())) {
                    incrementLoginFailCount(failKey);
                    return Result.error("账户或密码错误");
                }
            } else {
                if ("admin".equals(user.getRole())) {
                    incrementLoginFailCount(failKey);
                    return Result.error("账户或密码错误");
                }
            }
        }
        
        // 登录成功，清除失败次数记录
        redisTemplate.delete(failKey);
        log.info("用户 {} 登录成功，已清除失败次数记录", username);
        
        // 密码脱敏
        user.setPassword(null);

        return Result.success(user);
    }

    /**
     * 增加登录失败次数
     * @param failKey Redis key
     */
    private void incrementLoginFailCount(String failKey) {
        Object countObj = redisTemplate.opsForValue().increment(failKey);
        Long count = null;
        if (countObj != null) {
            if (countObj instanceof Integer) {
                count = ((Integer) countObj).longValue();
            } else if (countObj instanceof Long) {
                count = (Long) countObj;
            }
        }
        if (count != null && count == 1) {
            // 第一次失败，设置 10 分钟过期
            redisTemplate.expire(failKey, 10, TimeUnit.MINUTES);
            log.info("用户登录失败，已记录失败次数：1");
        } else if (count != null) {
            log.info("用户登录失败，累计失败次数：{}", count);
        }
    }

    /**
     * 用户登出接口
     * 当前仅返回成功，实际Token失效通常由前端清除或配合Redis黑名单实现
     *
     * @return 登出结果
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    @OperateLog(module = "认证管理", operation = "用户登出")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     *
     * @param userIdStr 网关透传的用户ID
     * @return 用户详情（不含密码）
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(@RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        
        // 查询用户并脱敏密码
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 修改密码接口
     *
     * @param userIdStr 网关透传的用户ID
     * @param params 参数Map，需包含 oldPwd 和 newPwd
     * @return 修改结果
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    @OperateLog(module = "认证管理", operation = "修改密码")
    public Result<String> changePassword(@RequestHeader("X-User-Id") String userIdStr, 
                                         @RequestBody Map<String, String> params) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        String oldPwd = params.get("oldPwd");
        String newPwd = params.get("newPwd");

        if (!StringUtils.hasText(oldPwd) || !StringUtils.hasText(newPwd)) {
            return Result.error("密码不能为空");
        }

        User user = userService.getUserById(userId);
        
        // 校验原密码
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 更新新密码（使用BCrypt加密存储）
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPwd));
        userService.updateUser(updateUser);
        
        // 清除 Redis 中的 Token，强制用户重新登录
        String tokenKey = "auth:token:" + userId;
        redisTemplate.delete(tokenKey);
        log.info("用户 {} 修改密码成功，已清除 Redis 中的 Token", user.getUsername());
        
        return Result.success("密码修改成功");
    }

    /**
     * 更新用户个人资料
     * 允许更新姓名、电话、单位等非敏感信息
     *
     * @param userIdStr 网关透传的用户ID
     * @param updateUser 包含待更新字段的用户对象
     * @return 更新结果
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    @OperateLog(module = "认证管理", operation = "更新个人资料")
    public Result<String> updateProfile(@RequestHeader("X-User-Id") String userIdStr, @RequestBody User updateUser) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        
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

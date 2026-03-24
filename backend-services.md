# 疫情防控物资调度管理系统 - 后端微服务详细分析

## 目录

1. [epidemic-gateway（API网关）](#1-epidemic-gatewayapi网关)
2. [epidemic-common（公共模块）](#2-epidemic-common公共模块)
3. [epidemic-user-service（用户服务）](#3-epidemic-user-service用户服务)
4. [epidemic-material-service（物资服务）](#4-epidemic-material-service物资服务)
5. [epidemic-pandemic-service（疫情服务）](#5-epidemic-pandemic-service疫情服务)
6. [epidemic-log-service（日志服务）](#6-epidemic-log-service日志服务)

---

## 1. epidemic-gateway（API网关）

**服务端口**: 8080

**主要功能**: 统一入口、路由转发、认证授权、跨域处理

### 1.1 核心组件

| 文件 | 类型 | 说明 |
|------|------|------|
| GatewayApplication.java | 主应用 | Spring Boot启动类，启用Nacos服务发现 |
| AuthConfig.java | 配置类 | 认证白名单配置 |
| RedisConfig.java | 配置类 | RedisTemplate序列化配置 |
| WarmupRunner.java | 配置类 | JWT预热配置 |
| JwtUtil.java | 工具类 | JWT令牌生成与解析 |
| AuthGlobalFilter.java | 过滤器 | 全局认证过滤器（优先级-100） |
| LoginTokenGatewayFilterFactory.java | 过滤器工厂 | 登录Token处理 |
| LogoutTokenGatewayFilterFactory.java | 过滤器工厂 | 登出Token处理 |

### 1.2 JWT配置

```yaml
jwt:
  secret: epidemic-material-management-secret-key-2024-super-secret
  expiration: 7200000  # 2小时
```

### 1.3 认证过滤器流程

```
请求 -> AuthGlobalFilter.filter()
  ├─ 1. 白名单检查（放行）
  ├─ 2. 获取Token
  ├─ 3. JWT验证
  ├─ 4. Redis黑名单检查
  ├─ 5. Redis用户Token匹配验证
  └─ 6. 放入用户信息到请求头 -> chain.filter()
```

### 1.4 路由配置

| 路由ID | 目标服务 | 路径规则 |
|--------|----------|----------|
| auth-login | epidemic-user-service | /api/auth/login |
| auth-logout | epidemic-user-service | /api/auth/logout |
| user-service | epidemic-user-service | /api/user/**, /api/auth/** |
| material-service | epidemic-material-service | /api/material/**, /api/inventory/**, /api/donation/**, /api/application/** |
| pandemic-service | epidemic-pandemic-service | /api/pandemic/**, /api/notification/** |
| log-service | epidemic-log-service | /api/log/** |

### 1.5 Redis存储结构

| Key格式 | Value | 过期时间 | 说明 |
|---------|-------|----------|------|
| auth:token:{userId} | JWT Token | 2小时 | 用户当前有效Token |
| auth:blacklist:{token} | "1" | 剩余过期时间 | Token黑名单 |

---

## 2. epidemic-common（公共模块）

**主要功能**: 存放各服务共享的类、统一响应封装、全局异常处理、Feign客户端定义

### 2.1 统一响应封装

#### Result.java

```java
@Data
public class Result<T> implements Serializable {
    private Integer code;      // 状态码
    private String message;    // 提示信息
    private T data;           // 数据
    private Long timestamp;    // 时间戳
}
```

**静态工厂方法**:

| 方法 | 说明 |
|------|------|
| success() | 成功响应，无数据 |
| success(T data) | 成功响应，带数据 |
| error(String message) | 失败响应，状态码500 |
| error(Integer code, String message) | 失败响应，自定义状态码 |

#### PageResult.java

```java
@Data
public class PageResult<T> implements Serializable {
    private List<T> list;      // 数据列表
    private Long total;         // 总记录数
    private Integer page;      // 当前页
    private Integer size;       // 每页大小
}
```

### 2.2 异常处理

#### BusinessException.java

```java
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;  // 错误状态码
    public BusinessException(String message)           // 默认code=500
    public BusinessException(Integer code, String message)
}
```

#### GlobalExceptionHandler.java

| 异常类型 | 处理方法 | 返回状态码 |
|----------|----------|------------|
| BusinessException | handleBusinessException() | e.getCode() |
| MethodArgumentNotValidException | handleValidationException() | 400 |
| BindException | handleBindException() | 400 |
| Exception | handleException() | 500 |

### 2.3 Feign客户端

#### LogFeignClient.java

```java
@FeignClient(
    name = "epidemic-log-service",
    path = "/log",
    fallbackFactory = LogFeignClientFallbackFactory.class
)
public interface LogFeignClient {
    @PostMapping("/save")
    Result<Void> saveLog(@RequestBody OperateLog operateLog);

    @GetMapping("/recent")
    Result<List<OperateLog>> getRecentLogs(@RequestParam(defaultValue="10") Integer limit);
}
```

### 2.4 公共实体类

#### OperateLog.java

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志ID |
| userId | Long | 用户ID |
| username | String | 用户名 |
| module | String | 操作模块 |
| operation | String | 操作描述 |
| method | String | 请求方法 |
| params | String | 请求参数 |
| ip | String | IP地址 |
| status | Integer | 状态：0-失败，1-成功 |
| errorMsg | String | 错误信息 |
| executeTime | Long | 执行时长（毫秒） |
| operateTime | LocalDateTime | 操作时间 |

#### PandemicNews.java

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 新闻ID |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容 |
| author | String | 作者 |
| status | String | 状态 |
| viewCount | Integer | 浏览次数 |
| publishTime | LocalDateTime | 发布时间 |

---

## 3. epidemic-user-service（用户服务）

**服务端口**: 8081

**数据库**: epidemic_user

**主要功能**: 用户管理、认证授权、操作日志记录

### 3.1 Controller层

#### AuthController (/auth)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户登录 | POST | /auth/login | 校验用户名密码、登录失败计数（5次锁定10分钟） |
| 用户登出 | POST | /auth/logout | 清除Redis Token |
| 获取当前用户信息 | GET | /auth/info | 从请求头X-User-Id获取用户信息 |
| 修改密码 | PUT | /auth/password | 校验原密码、更新为BCrypt加密 |
| 更新个人资料 | PUT | /auth/profile | 更新姓名、电话、单位等非敏感信息 |

#### UserController (/user)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取用户列表 | GET | /user/list | 分页查询，支持多条件筛选 |
| 获取用户详情 | GET | /user/{id} | 根据ID获取用户 |
| 新增用户 | POST | /user | 创建用户 |
| 更新用户 | PUT | /user | 更新用户信息 |
| 删除用户 | DELETE | /user/{id} | 删除用户（物理删除） |
| 更新用户状态 | PUT | /user/status/{id} | 启用/禁用账户 |
| 批量更新状态 | PUT | /user/batch/status | 批量更新用户状态 |
| 批量删除用户 | DELETE | /user/batch | 批量删除用户 |
| 获取角色统计 | GET | /user/stats/role-counts | 统计各角色用户数量 |
| 按角色获取用户ID | GET | /user/ids/by-role | 获取用户ID列表 |

### 3.2 Service层

```java
public interface UserService extends IService<User> {
    PageResult<User> getUserList(Integer page, Integer size, String username, String name, String phone, String role, String status);
    User getUserById(Long id);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
    void updateUserStatus(Long id, String status);
    void batchUpdateStatus(List<Long> ids, String status);
    void batchDelete(List<Long> ids);
    List<Map<String, Object>> getRoleCounts();
    List<Long> getUserIdsByRole(String role);
}
```

### 3.3 实体类

#### User.java

```java
@TableName("sys_user")
public class User implements Serializable {
    private Long id;           // 用户ID (自增)
    private String username;   // 用户名
    private String password;   // 密码 (BCrypt加密)
    private String name;       // 姓名
    private String role;       // 角色: admin/hospital_user/community_staff/donor
    private String phone;      // 手机号
    private String unit;      // 所属单位
    private String avatar;     // 头像
    private String status;     // 状态: active/disabled
    @TableLogic
    private Integer deleted;  // 软删除标记
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;   // 更新时间
}
```

### 3.4 安全配置

#### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/user/stats/role-counts", "/user/ids/by-role",
                                 "/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**")
                .permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 3.5 分布式锁

#### RedissonConfig.java

```java
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
              .setAddress(address)
              .setDatabase(database)
              .setConnectionMinimumIdleSize(5)
              .setConnectionPoolSize(10);
        return Redisson.create(config);
    }
}
```

**注意**: 当前代码中 RedissonClient 已配置但实际未被使用。登录失败计数使用普通的 RedisTemplate。

---

## 4. epidemic-material-service（物资服务）

**服务端口**: 8082

**数据库**: epidemic_material

**主要功能**: 物资管理、物资申请审批、捐赠管理、库存管理、物流追踪、统计数据

### 4.1 Controller层

#### ApplicationController (/application)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取申请列表 | GET | /application/list | 分页查询，支持状态、申请人筛选 |
| 获取申请详情 | GET | /application/{id} | 根据ID获取申请详情 |
| 提交物资申请 | POST | /application | 提交新的物资申请 |
| 审核申请 | POST | /application/approve | 审核申请，支持批准/驳回 |
| 取消申请 | POST | /application/{id}/cancel | 取消待审核的申请 |
| 获取我的申请 | GET | /application/my | 获取当前用户的申请列表 |
| 获取物流追踪 | GET | /application/{id}/track | 获取申请单的物流追踪信息 |

#### DonationController (/donation)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取捐赠列表 | GET | /donation/list | 分页查询，支持多条件筛选 |
| 获取捐赠详情 | GET | /donation/{id} | 根据ID获取捐赠详情 |
| 提交捐赠申请 | POST | /donation | 提交新的捐赠申请 |
| 获取我的捐赠 | GET | /donation/my | 获取当前用户的捐赠列表 |
| 审核捐赠 | POST | /donation/approve | 审核捐赠申请 |
| 获取捐赠统计 | GET | /donation/stats | 获取捐赠统计数据 |

#### MaterialController (/material)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取物资列表 | GET | /material/list | 分页查询，支持名称、类型、状态筛选 |
| 获取物资详情 | GET | /material/{id} | 根据ID获取物资详情 |
| 获取物资类型列表 | GET | /material/types | 获取物资类型枚举 |
| 获取仓库列表 | GET | /material/warehouses | 获取仓库列表 |
| 新增物资 | POST | /material | 新增物资记录 |
| 更新物资 | PUT | /material | 更新物资信息 |
| 删除物资 | DELETE | /material/{id} | 删除物资 |

#### InventoryController (/inventory)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取库存列表 | GET | /inventory/list | 分页获取库存列表 |
| 获取库存预警列表 | GET | /inventory/warning | 获取低于阈值的库存预警物资 |
| 盘点库存 | POST | /inventory/check | 盘点库存并更新实际库存量 |

#### StockController (/stock)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询库存台账 | GET | /stock/ledger | 查询物资库存台账信息 |
| 创建出入库单 | POST | /stock/order | 创建出入库单据 |
| 审核单据 | POST | /stock/order/{id}/audit | 审核出入库单据 |
| 作废单据 | POST | /stock/order/{id}/void | 作废出入库单据 |
| 查询单据列表 | GET | /stock/order/list | 分页查询单据列表 |
| 查询库存变动日志 | GET | /stock/log/list | 分页查询库存变动记录 |

#### StatsController (/stats)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取仪表盘统计 | GET | /stats/dashboard | 获取管理端仪表盘综合统计数据 |
| 获取用户个人统计 | GET | /stats/user | 获取用户个人中心的统计数据 |
| 获取库存预警列表 | GET | /stats/warning | 获取库存预警物资列表 |
| 获取趋势数据 | GET | /stats/trend | 获取出入库趋势数据 |
| 获取物资统计 | GET | /stats/material | 获取物资分类统计数据 |
| 获取实时数据大屏 | GET | /stats/dashboard/full | 获取实时数据大屏综合数据 |

### 4.2 核心实体类

#### Application（申请单）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 申请单号，格式：S+时间戳 |
| materialId | String | 物资ID |
| materialName | String | 物资名称 |
| quantity | Integer | 申请数量 |
| applicantId | Long | 申请人ID |
| applicantName | String | 申请人姓名 |
| status | String | 状态：pending/approved/rejected/cancelled |
| urgency | String | 紧急程度 |
| address | String | 收货地址 |
| receiver | String | 收货人 |
| receiverPhone | String | 联系电话 |

#### Donation（捐赠单）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 捐赠单号 |
| materialName | String | 物资名称 |
| type | String | 物资类型 |
| quantity | Integer | 捐赠数量 |
| donorId | Long | 捐赠人ID |
| donorUnit | String | 捐赠单位 |
| status | String | 状态：pending/approved/rejected |
| source | String | 来源：personal/enterprise |

#### Material（物资）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 物资ID |
| name | String | 物资名称 |
| type | String | 物资类型 |
| specification | String | 规格 |
| unit | String | 单位 |
| stock | Integer | 当前库存 |
| threshold | Integer | 库存预警阈值 |
| warehouse | String | 仓库 |
| status | String | 状态：normal/warning |

#### StockOrder（库存单据）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 单据号（UUID） |
| type | String | 单据类型：inbound/outbound |
| sourceType | String | 来源类型：purchase/donation/application/transfer/check/manual |
| status | String | 状态：draft/pending/approved/void |
| totalAmount | BigDecimal | 总金额 |
| handlerId | Long | 经办人ID |
| handlerName | String | 经办人姓名 |

### 4.3 库存管理和申请审批流程

#### 物资申请流程

```
1. 用户提交申请 (submitApplication)
   ├─ 校验物资存在性
   ├─ 校验库存充足性
   └─ 创建申请单，状态设为 "pending"

2. 管理员审核申请 (approveApplication)
   ├─ 校验申请单存在性和状态
   ├─ 若审核通过：
   │   ├─ 再次校验库存
   │   ├─ 创建出库单 (StockOrder)
   │   ├─ 自动审核出库单
   │   └─ 扣减库存 (带分布式锁)
   └─ 更新申请单状态和审核信息
```

#### 捐赠管理流程

```
1. 用户提交捐赠 (submitDonation)
   └─ 创建捐赠单，状态设为 "pending"

2. 管理员审核捐赠 (approveDonation)
   ├─ 校验捐赠单存在性和状态
   ├─ 若审核通过且指定了目标物资：
   │   ├─ 创建入库单 (StockOrder)
   │   ├─ 自动审核入库单
   │   └─ 增加库存 (带分布式锁)
   └─ 更新捐赠单状态和审核信息
```

### 4.4 分布式锁使用

#### DistributedLockUtil.java

```java
public class DistributedLockUtil {
    // 获取锁后执行操作
    public <T> T executeWithLock(String lockKey, Supplier<T> action, long waitTime, long leaseTime)

    // 默认等待3秒，持有30秒
    public <T> T executeWithLock(String lockKey, Supplier<T> action)

    // 带重试机制的分布式锁
    public <T> T executeWithLockWithRetry(String lockKey, Supplier<T> action, int maxRetries, long waitTime, long leaseTime)
}
```

**在 StockServiceImpl.executeStockChange() 中的使用**:

```java
String lockKey = "stock:lock:" + item.getMaterialId();
StockOperationResult result = distributedLockUtil.executeWithLockWithRetry(lockKey, () -> {
    // 1. 查询物资
    // 2. 校验库存
    // 3. 计算新库存
    // 4. 更新库存
    // 5. 检查预警状态
    return new StockOperationResult(...);
});
```

### 4.5 Feign客户端

#### PandemicFeignClient.java

```java
@FeignClient(
    name = "epidemic-pandemic-service",
    path = "/pandemic",
    fallbackFactory = PandemicFeignClientFallbackFactory.class
)
public interface PandemicFeignClient {
    @GetMapping("/news")
    Result<PageResult<PandemicNews>> getNewsList(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) String status);

    @GetMapping("/news/{id}")
    Result<PandemicNews> getNewsDetail(@PathVariable("id") String id);
}
```

### 4.6 缓存策略

| Key | 过期时间 | 说明 |
|-----|----------|------|
| material:stats:all | 5分钟 | 物资统计数据 |
| material:warning:list | 5分钟 | 库存预警列表 |
| material:today:stats:{date} | 24小时 | 今日出入库统计 |
| material:config:types | 24小时 | 物资类型列表 |
| material:config:warehouses | 24小时 | 仓库列表 |
| application:status:{id} | 30分钟 | 申请单状态 |
| donation:status:{id} | 30分钟 | 捐赠单状态 |

---

## 5. epidemic-pandemic-service（疫情服务）

**服务端口**: 8083

**数据库**: epidemic_pandemic

**主要功能**: 疫情新闻发布、防控知识库、消息通知推送

### 5.1 Controller层

#### PandemicController (/pandemic)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取疫情新闻列表 | GET | /pandemic/news | 获取疫情新闻列表（分页） |
| 获取新闻详情 | GET | /pandemic/news/{id} | 获取新闻详情 |
| 发布新闻 | POST | /pandemic/news | 发布新闻 |
| 删除新闻 | DELETE | /pandemic/news/{id} | 删除新闻 |
| 获取防疫知识列表 | GET | /pandemic/knowledge | 获取防疫知识列表 |
| 获取实时疫情数据 | GET | /pandemic/data | 获取实时疫情数据 |
| 获取推送统计 | GET | /pandemic/push/stats | 获取推送统计 |
| 获取推送记录列表 | GET | /pandemic/push/list | 获取推送记录列表 |
| 发送推送 | POST | /pandemic/push | 发送推送 |
| 删除推送记录 | DELETE | /pandemic/push/{id} | 删除推送记录 |

#### NotificationController (/notification)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取我的通知列表 | GET | /notification/user | 获取我的通知列表（分页） |
| 获取未读通知数量 | GET | /notification/unread-count | 获取未读通知数量 |
| 标记通知为已读 | PUT | /notification/{id}/read | 标记通知为已读 |
| 标记所有通知为已读 | PUT | /notification/read-all | 标记所有通知为已读 |
| 删除通知 | DELETE | /notification/{id} | 删除通知 |

### 5.2 实体类

#### PandemicNews（疫情新闻）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容 |
| author | String | 作者 |
| status | String | 状态 |
| viewCount | Integer | 浏览次数 |
| publishTime | LocalDateTime | 发布时间 |

#### PushRecord（推送记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键（自增） |
| title | String | 推送标题 |
| content | String | 推送内容 |
| target | String | 推送目标 |
| channels | String | 推送渠道 |
| status | String | 状态 |
| pushTime | LocalDateTime | 推送时间 |

#### UserNotification（用户通知）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键（自增） |
| userId | Long | 用户ID |
| title | String | 通知标题 |
| content | String | 通知内容 |
| type | String | 类型 |
| isRead | Integer | 是否已读（0-否，1-是） |
| pushRecordId | Long | 关联推送记录ID |

#### PushMessage（RabbitMQ消息）

| 字段 | 类型 | 说明 |
|------|------|------|
| pushRecordId | Long | 推送记录ID |
| title | String | 推送标题 |
| content | String | 推送内容 |
| target | String | 推送目标 |
| channels | String | 推送渠道 |
| pushTime | LocalDateTime | 推送时间 |

### 5.3 RabbitMQ配置

| 配置项 | 值 |
|--------|-----|
| PUSH_EXCHANGE | pandemic.push.exchange |
| PUSH_QUEUE | pandemic.push.queue |
| PUSH_ROUTING_KEY | pandemic.push |
| MessageConverter | Jackson2JsonMessageConverter |

### 5.4 消息推送完整流程

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  POST /push     │────▶│ PandemicService  │────▶│ PushRecord表    │
│  (发送推送请求)  │     │ .sendPush()       │     │ (保存推送记录)   │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                                 │ 构建 PushMessage
                                 ▼
                        ┌────────────────────┐
                        │ PushMessageProducer │
                        │ .sendPushMessage()  │
                        └────────┬───────────┘
                                 │
                                 ▼
                        ┌────────────────────┐
                        │   RabbitMQ         │
                        │ pandemic.push.queue│
                        └────────┬───────────┘
                                 │
                                 ▼
                        ┌────────────────────┐
                        │ PushMessageConsumer│
                        │ .handlePushMessage │
                        └────────┬───────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
           ┌─────────────────┐       ┌─────────────────┐
           │ UserFeignClient │       │UserNotification  │
           │getUserIdsByRole │       │createNotifications│
           └─────────────────┘       └─────────────────┘
```

### 5.5 推送目标类型

| target 值 | 说明 |
|-----------|------|
| all | 推送给所有用户 |
| hospital_user | 医院用户 |
| community_staff | 社区人员 |
| material_approver | 物资审核员 |

### 5.6 Feign客户端

#### UserFeignClient.java

```java
@FeignClient(
    name = "epidemic-user-service",
    path = "/user",
    fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient {
    @GetMapping("/stats/role-counts")
    Result<List<Map<String, Object>>> getRoleCounts();

    @GetMapping("/ids/by-role")
    Result<List<Long>> getUserIdsByRole(@RequestParam(required = false) String role);
}
```

### 5.7 Redis缓存策略

| Key | 过期时间 | 说明 |
|-----|----------|------|
| pandemic:news:list:all | 5分钟 | 新闻列表缓存 |
| pandemic:push:stats | 5分钟 | 推送统计缓存 |
| pandemic:push:list | 5分钟 | 推送列表缓存 |
| pandemic:user:role:stats | 30分钟 | 用户角色统计缓存 |

---

## 6. epidemic-log-service（日志服务）

**服务端口**: 8084

**数据库**: epidemic_system

**主要功能**: 统一操作日志记录、日志查询与展示

### 6.1 Controller层

#### OperateLogController (/log)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 保存操作日志 | POST | /log/save | 供其他微服务调用 |
| 获取最近日志 | GET | /log/recent | 获取最近操作日志 |
| 分页查询日志 | GET | /log/list | 分页查询操作日志 |

### 6.2 Service层

```java
public interface OperateLogService extends IService<OperateLog> {
    List<OperateLog> getRecentLogs(int limit);
    Page<OperateLog> getLogList(Integer page, Integer size, String username, String module,
                                 LocalDateTime startTime, LocalDateTime endTime);
}
```

### 6.3 实体类

#### OperateLog.java

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志ID（自增） |
| userId | Long | 用户ID |
| username | String | 用户名 |
| module | String | 操作模块 |
| operation | String | 操作描述 |
| method | String | 请求方法（类.方法） |
| params | String | 请求参数 |
| ip | String | IP地址 |
| status | Integer | 状态：0-失败，1-成功 |
| errorMsg | String | 错误信息 |
| executeTime | Long | 执行时长（毫秒） |
| operateTime | LocalDateTime | 操作时间 |

### 6.4 日志记录完整流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          日志记录完整流程                                     │
└─────────────────────────────────────────────────────────────────────────────┘

  ┌──────────────────┐         ┌─────────────────┐         ┌──────────────────┐
  │   业务微服务      │         │  epidemic-log   │         │     MySQL        │
  │   Controller     │         │    -service     │         │  sys_operate_log │
  └────────┬─────────┘         └────────┬────────┘         └────────▲─────────┘
           │                            │                           │
           │ 1. 业务方法带@OperateLog注解│                           │
           ▼                            │                           │
  ┌──────────────────┐                  │                           │
  │  OperateLogAspect │                  │                           │
  │  (AOP切面)        │                  │                           │
  └────────┬─────────┘                  │                           │
           │                            │                           │
           │ 3. @Async 异步调用          │                           │
           ▼                            │                           │
  ┌──────────────────┐                  │                           │
  │  LogFeignClient  │                  │                           │
  │  saveLog()       │                  │                           │
  └────────┬─────────┘                  │                           │
           │                            │                           │
           │ 4. HTTP POST /log/save     │                           │
           ▼                            ▼                           │
  ┌──────────────────┐         ┌─────────────────┐                  │
  │ OperateLog       │         │ OperateLog      │                  │
  │ Controller       │         │ ServiceImpl     │                  │
  └──────────────────┘         └────────┬────────┘                  │
                                        │                           │
                                        │ 6. MyBatis Plus INSERT    │
                                        ▼                           │
                              ┌─────────────────┴───────────────────┘
```

### 6.5 各微服务日志切面分布

| 微服务 | 切面类包路径 | 注解包路径 |
|--------|-------------|-----------|
| epidemic-material-service | com.epidemic.material.aspect | com.epidemic.material.annotation |
| epidemic-user-service | com.epidemic.user.aspect | com.epidemic.user.annotation |
| epidemic-pandemic-service | com.epidemic.pandemic.aspect | com.epidemic.pandemic.annotation |

每个微服务都有自己独立的 @OperateLog 注解和 OperateLogAspect 实现，但都通过统一的 LogFeignClient 调用 log-service。

---

## 模块依赖关系图

```
┌─────────────────────────────────────────────────────────┐
│                    epidemic-material-management          │
│                       (Vue.js Frontend)                  │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP/API
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   epidemic-gateway                       │
│                    (API Gateway)                         │
│                  Port: 8080                              │
└───────┬───────┬───────┬───────┬─────────┬─────────────┘
        │       │       │       │         │
        ▼       ▼       ▼       ▼         ▼
┌──────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐
│ epidemic- │ │  epidemic-   │ │  epidemic-   │ │ epidemic-  │
│ user-     │ │  material-   │ │  pandemic-   │ │ log-       │
│ service   │ │  service      │ │  service     │ │ service    │
│ Port:8081 │ │  Port:8082    │ │  Port:8083   │ │ Port:8084  │
└────┬─────┘ └───────┬──────┘ └───────┬──────┘ └─────┬──────┘
     │               │                │              │
     └───────────────┴────────────────┴──────────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │  epidemic-common    │
          │  (Shared Module)    │
          │ - Result封装        │
          │ - Exception处理     │
          │ - Feign客户端       │
          └─────────────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    ┌─────────┐ ┌─────────┐ ┌─────────┐
    │  MySQL  │ │  Redis   │ │ RabbitMQ│
    │ (Data)  │ │ (Cache)  │ │  (MQ)   │
    └─────────┘ └─────────┘ └─────────┘
```

---

## 技术栈总结

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.2 | 基础框架 |
| Spring Cloud | 2023.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里云生态组件 |
| MyBatis Plus | 3.5.7 | ORM框架 |
| JWT (jjwt) | 0.12.3 | 身份认证 |
| Redisson | 3.23.5 | 分布式锁 |
| RabbitMQ | - | 消息队列 |
| Nacos | - | 服务发现与配置中心 |
| MySQL | 8.0+ | 数据库 |
| Redis | - | 缓存 |
| Knife4j | 4.5.0 | API文档 |

---

## 配置文件位置

| 模块 | 配置文件路径 |
|------|-------------|
| gateway | backend/epidemic-gateway/src/main/resources/application.yml |
| common | - |
| user-service | backend/epidemic-user-service/src/main/resources/application.yml |
| material-service | backend/epidemic-material-service/src/main/resources/application.yml |
| pandemic-service | backend/epidemic-pandemic-service/src/main/resources/application.yml |
| log-service | backend/epidemic-log-service/src/main/resources/application.yml |

---

*文档生成时间: 2026-03-24*

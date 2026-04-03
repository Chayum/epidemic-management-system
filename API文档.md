# 疫情防控物资调度管理系统 - 前后端接口文档

## 文档信息

- 项目名称：疫情防控物资调度管理系统
- 文档版本：v2.0.0
- 最后更新：2026-03-30
- 后端技术栈：Spring Cloud Alibaba 微服务架构 + MySQL + Redis + RabbitMQ
- 前端技术栈：Vue 3 + Element Plus + Pinia + ECharts
- 服务架构：网关(Gateway) + 用户服务 + 物资服务 + 疫情服务 + 公共模块

---

## 目录

1. [通用说明](#通用说明)
2. [认证模块](#认证模块)
3. [用户管理模块](#用户管理模块)
4. [操作日志模块](#操作日志模块)
5. [物资管理模块](#物资管理模块)
6. [库存管理模块](#库存管理模块)
7. [库存台账模块](#库存台账模块)
8. [申请管理模块](#申请管理模块)
9. [捐赠管理模块](#捐赠管理模块)
10. [疫情信息模块](#疫情信息模块)
11. [数据统计模块](#数据统计模块)
12. [通知模块](#通知模块)
13. [错误码说明](#错误码说明)
14. [数据库设计](#数据库设计)

---

## 通用说明

### 基础路径

- 开发环境：`http://localhost:8080/api`
- 网关端口：8080
- 各微服务端口：
  - 用户服务：8081
  - 物资服务：8082
  - 疫情服务：8083

### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |
| Authorization | String | 否 | Bearer {token}（需认证接口） |
| X-User-Id | String | 否 | 用户ID（网关透传） |
| X-Username | String | 否 | 用户名（网关透传） |

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1706000000000
}
```

### 分页请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

### 分页响应数据

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

## 认证模块

### 1.1 用户登录

**接口地址**：`POST /auth/login`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| role | String | 否 | 用户角色 |

**请求示例**：

```json
{
  "username": "admin",
  "password": "123456",
  "role": "admin"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "password": null,
    "role": "admin",
    "phone": "13800138000",
    "unit": "疫情防控指挥部",
    "status": "active"
  }
}
```

**业务说明**：
- 登录失败超过5次将被锁定10分钟
- 密码匹配支持明文和BCrypt两种格式，自动升级
- 登录成功后密码字段置空返回

---

### 1.2 用户登出

**接口地址**：`POST /auth/logout`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "登出成功"
}
```

---

### 1.3 获取当前用户信息

**接口地址**：`GET /auth/info`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "admin",
    "phone": "13800138000",
    "unit": "疫情防控指挥部",
    "status": "active"
  }
}
```

---

### 1.4 修改密码

**接口地址**：`PUT /auth/password`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPwd | String | 是 | 旧密码 |
| newPwd | String | 是 | 新密码 |

**响应示例**：

```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

---

### 1.5 更新用户信息

**接口地址**：`PUT /auth/profile`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 姓名 |
| phone | String | 否 | 手机号 |
| unit | String | 否 | 所属单位 |

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

## 用户管理模块

### 2.1 获取用户列表

**接口地址**：`GET /user/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| name | String | 否 | 姓名（模糊搜索） |
| username | String | 否 | 用户名（模糊搜索） |
| role | String | 否 | 角色 |
| status | String | 否 | 状态 |

**用户角色枚举**：

| 值 | 说明 |
|----|------|
| admin | 管理员 |
| hospital_user | 医院用户 |
| community_staff | 社区人员 |
| donor | 捐赠方 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "name": "管理员",
        "role": "admin",
        "phone": "13800138000",
        "unit": "疫情防控指挥部",
        "status": "active",
        "createTime": "2026-01-01 00:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

### 2.2 获取用户详情

**接口地址**：`GET /user/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "admin",
    "phone": "13800138000",
    "unit": "疫情防控指挥部",
    "status": "active",
    "createTime": "2026-01-01 00:00:00"
  }
}
```

---

### 2.3 新增用户

**接口地址**：`POST /user`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| name | String | 是 | 姓名 |
| role | String | 是 | 角色 |
| phone | String | 是 | 手机号 |
| unit | String | 是 | 所属单位 |

**响应示例**：

```json
{
  "code": 200,
  "message": "添加成功"
}
```

---

### 2.4 更新用户

**接口地址**：`PUT /user`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：同新增用户，增加 `id` 字段。

**响应示例**：

```json
{
  "code": 200,
  "message": "修改成功"
}
```

---

### 2.5 删除用户

**接口地址**：`DELETE /user/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

### 2.6 更新用户状态

**接口地址**：`PUT /user/status/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 状态（active/disabled） |

**响应示例**：

```json
{
  "code": 200,
  "message": "状态更新成功"
}
```

---

### 2.7 批量更新用户状态

**接口地址**：`PUT /user/batch/status`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 用户ID列表 |
| status | String | 是 | 状态（active/disabled） |

**响应示例**：

```json
{
  "code": 200,
  "message": "批量状态更新成功"
}
```

---

### 2.8 批量删除用户

**接口地址**：`DELETE /user/batch`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 用户ID列表 |

**响应示例**：

```json
{
  "code": 200,
  "message": "批量删除成功"
}
```

---

### 2.9 获取用户角色统计

**接口地址**：`GET /user/stats/role-counts`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    { "role": "admin", "count": 5 },
    { "role": "hospital_user", "count": 20 }
  ]
}
```

---

### 2.10 根据角色获取用户ID列表

**接口地址**：`GET /user/ids/by-role`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 否 | 角色（为空则返回除admin外的所有用户ID） |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [1, 2, 3, 4, 5]
}
```

---

## 操作日志模块

### 3.1 保存操作日志

**接口地址**：`POST /log/save`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| module | String | 否 | 模块名称 |
| operation | String | 否 | 操作类型 |
| username | String | 否 | 用户名 |
| ip | String | 否 | IP地址 |
| method | String | 否 | 请求方法 |
| params | String | 否 | 请求参数 |
| operateTime | LocalDateTime | 否 | 操作时间 |

**响应示例**：

```json
{
  "code": 200,
  "message": "success"
}
```

---

### 3.2 获取最近操作日志

**接口地址**：`GET /log/recent`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 返回条数，默认10 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "module": "用户管理",
      "operation": "创建用户",
      "username": "admin",
      "ip": "127.0.0.1",
      "method": "POST /user",
      "params": "{...}",
      "operateTime": "2026-03-30 10:00:00"
    }
  ]
}
```

---

### 3.3 分页查询操作日志

**接口地址**：`GET /log/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| module | String | 否 | 模块名称 |
| operation | String | 否 | 操作类型 |
| username | String | 否 | 用户名 |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

## 物资管理模块

### 4.1 获取物资列表

**接口地址**：`GET /material/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| name | String | 否 | 物资名称（模糊搜索） |
| type | String | 否 | 物资类型 |
| status | String | 否 | 状态 |

**物资类型枚举**：

| 值 | 说明 |
|----|------|
| protective | 防护物资 |
| disinfection | 消杀物资 |
| testing | 检测物资 |
| equipment | 医疗设备 |
| other | 其他物资 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": "M001",
        "name": "N95医用口罩",
        "type": "protective",
        "specification": "N95",
        "unit": "个",
        "stock": 8500,
        "threshold": 5000,
        "status": "normal",
        "warehouse": "一号仓库",
        "createTime": "2026-01-15 10:00:00",
        "updateTime": "2026-02-24 09:30:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

### 4.2 获取物资详情

**接口地址**：`GET /material/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 物资ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "M001",
    "name": "N95医用口罩",
    "type": "protective",
    "specification": "N95",
    "unit": "个",
    "stock": 8500,
    "threshold": 5000,
    "status": "normal",
    "warehouse": "一号仓库",
    "description": "医用防护口罩，符合GB19083-2010标准",
    "createTime": "2026-01-15 10:00:00",
    "updateTime": "2026-02-24 09:30:00"
  }
}
```

---

### 4.3 获取物资类型列表

**接口地址**：`GET /material/types`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    { "type": "protective", "name": "防护物资" },
    { "type": "disinfection", "name": "消杀物资" },
    { "type": "testing", "name": "检测物资" },
    { "type": "equipment", "name": "医疗设备" },
    { "type": "other", "name": "其他物资" }
  ]
}
```

---

### 4.4 获取仓库列表

**接口地址**：`GET /material/warehouses`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    { "warehouse": "一号仓库", "count": 50 },
    { "warehouse": "二号仓库", "count": 30 }
  ]
}
```

---

### 4.5 新增物资

**接口地址**：`POST /material`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 物资名称 |
| type | String | 是 | 物资类型 |
| specification | String | 否 | 规格型号 |
| unit | String | 是 | 单位 |
| stock | Integer | 是 | 库存数量 |
| threshold | Integer | 是 | 预警阈值 |
| warehouse | String | 是 | 仓库 |
| description | String | 否 | 描述 |

**响应示例**：

```json
{
  "code": 200,
  "message": "新增成功"
}
```

---

### 4.6 更新物资

**接口地址**：`PUT /material`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：同新增物资，增加 `id` 字段。

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

### 4.7 删除物资

**接口地址**：`DELETE /material/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 物资ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 库存管理模块

### 5.1 获取库存列表

**接口地址**：`GET /inventory/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| warehouse | String | 否 | 仓库 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": "INV001",
        "materialId": "M001",
        "materialName": "N95医用口罩",
        "warehouse": "一号仓库",
        "location": "A区-01架-01层",
        "stock": 8500,
        "unit": "个",
        "lastCheckTime": "2026-02-23 15:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 5.2 获取库存预警列表

**接口地址**：`GET /inventory/warning`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "M005",
      "name": "检测试剂",
      "stock": 3200,
      "threshold": 5000,
      "unit": "盒",
      "type": "testing",
      "warningLevel": "high"
    }
  ]
}
```

---

### 5.3 盘点库存

**接口地址**：`POST /inventory/check`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| inventoryId | String | 是 | 库存ID |
| actualStock | Integer | 是 | 实际库存 |
| remark | String | 否 | 备注 |

**响应示例**：

```json
{
  "code": 200,
  "message": "盘点成功"
}
```

---

## 库存台账模块

### 6.1 查询库存台账

**接口地址**：`GET /stock/ledger`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| keyword | String | 否 | 关键词搜索 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "materialId": "M001",
        "materialName": "N95医用口罩",
        "warehouse": "一号仓库",
        "location": "A区-01架-01层",
        "inbound": 5000,
        "outbound": 3000,
        "stock": 8500,
        "unit": "个"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 6.2 创建出入库单

**接口地址**：`POST /stock/order`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialId | String | 是 | 物资ID |
| type | String | 是 | 单据类型（inbound/outbound） |
| quantity | Integer | 是 | 数量 |
| warehouse | String | 是 | 仓库 |
| remark | String | 否 | 备注 |

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": "SO20260330001"
}
```

---

### 6.3 审核单据

**接口地址**：`POST /stock/order/{id}/audit`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 单据ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approved | Boolean | 是 | 审核结果 |
| remark | String | 否 | 审核意见 |

**响应示例**：

```json
{
  "code": 200,
  "message": "审核完成"
}
```

---

### 6.4 作废单据

**接口地址**：`POST /stock/order/{id}/void`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 单据ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "作废成功"
}
```

---

### 6.5 查询单据列表

**接口地址**：`GET /stock/order/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| type | String | 否 | 单据类型 |
| status | String | 否 | 状态 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 6.6 查询库存变动日志

**接口地址**：`GET /stock/log/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| materialId | String | 否 | 物资ID |
| type | String | 否 | 变动类型 |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

---

## 申请管理模块

### 7.1 获取申请列表

**接口地址**：`GET /application/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| status | String | 否 | 状态 |
| applicant | String | 否 | 申请人（模糊搜索） |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

**申请状态枚举**：

| 值 | 说明 |
|----|------|
| pending | 待审核 |
| approved | 已通过 |
| rejected | 已驳回 |
| cancelled | 已取消 |
| delivered | 已发货 |
| received | 已收货 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": "A2026024001",
        "materialId": "M001",
        "materialName": "N95医用口罩",
        "quantity": 1000,
        "unit": "个",
        "applicantId": 2,
        "applicantName": "张三",
        "applicantUnit": "市第一医院",
        "purpose": "用于发热门诊日常防护",
        "urgency": "critical",
        "address": "市第一医院物资科",
        "receiver": "张三",
        "receiverPhone": "13800138000",
        "status": "pending",
        "applyTime": "2026-02-24 10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 7.2 获取申请详情

**接口地址**：`GET /application/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 申请单号 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "A2026024001",
    "materialId": "M001",
    "materialName": "N95医用口罩",
    "quantity": 1000,
    "unit": "个",
    "applicantId": 2,
    "applicantName": "张三",
    "applicantUnit": "市第一医院",
    "purpose": "用于发热门诊日常防护",
    "urgency": "critical",
    "address": "市第一医院物资科",
    "receiver": "张三",
    "receiverPhone": "13800138000",
    "status": "pending",
    "applyTime": "2026-02-24 10:30:00"
  }
}
```

---

### 7.3 提交物资申请

**接口地址**：`POST /application`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialId | String | 是 | 物资ID |
| quantity | Integer | 是 | 申请数量 |
| purpose | String | 是 | 用途说明 |
| urgency | String | 是 | 紧急程度 |
| address | String | 是 | 收货地址 |
| receiver | String | 是 | 收货人 |
| receiverPhone | String | 是 | 收货电话 |

**紧急程度枚举**：

| 值 | 说明 |
|----|------|
| normal | 普通 |
| urgent | 较急 |
| critical | 紧急 |

**响应示例**：

```json
{
  "code": 200,
  "message": "提交成功"
}
```

---

### 7.4 审核申请

**接口地址**：`POST /application/approve`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| applicationId | String | 是 | 申请单号 |
| status | String | 是 | 审核结果（approved/rejected） |
| remark | String | 否 | 审核意见 |

**响应示例**：

```json
{
  "code": 200,
  "message": "审核完成"
}
```

---

### 7.5 取消申请

**接口地址**：`POST /application/{id}/cancel`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 申请单号 |

**响应示例**：

```json
{
  "code": 200,
  "message": "取消成功"
}
```

---

### 7.6 获取我的申请

**接口地址**：`GET /application/my`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：同获取申请列表。

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 7.7 获取物流追踪信息

**接口地址**：`GET /application/{id}/track`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 申请单号 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "applicationId": "A2026024001",
    "status": "delivered",
    "tracks": [
      {
        "time": "2026-02-24 14:00:00",
        "status": "shipped",
        "description": "物资已出库，正在配送中",
        "operator": "仓库管理员"
      },
      {
        "time": "2026-02-24 12:00:00",
        "status": "approved",
        "description": "申请已通过审核",
        "operator": "审核员"
      }
    ]
  }
}
```

---

## 捐赠管理模块

### 8.1 获取捐赠列表

**接口地址**：`GET /donation/list`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| status | String | 否 | 状态 |
| donorUnit | String | 否 | 捐赠单位（模糊搜索） |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": "D2026024001",
        "materialName": "N95医用口罩",
        "type": "protective",
        "quantity": 5000,
        "unit": "个",
        "donorUnit": "市慈善总会",
        "contactPerson": "李四",
        "contactPhone": "13800138001",
        "receiveAddress": "市防疫物资仓库",
        "source": "采购",
        "productionDate": "2026-01-01",
        "expiryDate": "2028-01-01",
        "status": "pending",
        "donateTime": "2026-02-24 10:30:00"
      }
    ],
    "total": 30,
    "page": 1,
    "size": 10
  }
}
```

---

### 8.2 获取捐赠详情

**接口地址**：`GET /donation/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 捐赠单号 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "D2026024001",
    "materialName": "N95医用口罩",
    "type": "protective",
    "quantity": 5000,
    "unit": "个",
    "donorUnit": "市慈善总会",
    "contactPerson": "李四",
    "contactPhone": "13800138001",
    "receiveAddress": "市防疫物资仓库",
    "source": "采购",
    "productionDate": "2026-01-01",
    "expiryDate": "2028-01-01",
    "status": "pending",
    "remark": "",
    "donateTime": "2026-02-24 10:30:00"
  }
}
```

---

### 8.3 提交捐赠申请

**接口地址**：`POST /donation`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialName | String | 是 | 物资名称 |
| type | String | 是 | 物资类型 |
| quantity | Integer | 是 | 捐赠数量 |
| unit | String | 是 | 单位 |
| productionDate | String | 否 | 生产日期 |
| expiryDate | String | 否 | 有效期至 |
| donorUnit | String | 是 | 捐赠单位 |
| contactPerson | String | 是 | 联系人 |
| contactPhone | String | 是 | 联系电话 |
| receiveAddress | String | 是 | 收货地址 |
| source | String | 否 | 物资来源 |
| remark | String | 否 | 物资说明 |

**响应示例**：

```json
{
  "code": 200,
  "message": "提交成功"
}
```

---

### 8.4 获取我的捐赠

**接口地址**：`GET /donation/my`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| status | String | 否 | 状态 |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 8.5 审核捐赠

**接口地址**：`POST /donation/approve`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| donationId | String | 是 | 捐赠单号 |
| status | String | 是 | 审核结果（approved/rejected） |
| remark | String | 否 | 审核意见 |

**响应示例**：

```json
{
  "code": 200,
  "message": "审核完成"
}
```

---

### 8.6 获取捐赠统计

**接口地址**：`GET /donation/stats`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalDonations": 50,
    "totalQuantity": 50000,
    "pendingCount": 8,
    "approvedCount": 42
  }
}
```

---

## 疫情信息模块

### 9.1 获取疫情新闻列表

**接口地址**：`GET /pandemic/news`

**请求头**：`Authorization: Bearer {token}`（管理员需要，公开接口可选）

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| status | String | 否 | 状态（published/draft） |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": "N001",
        "title": "我市新增3例确诊病例",
        "summary": "今日新增3例确诊病例，均在隔离管控中发现",
        "content": "...",
        "author": "疫情防控指挥部",
        "status": "published",
        "viewCount": 1520,
        "publishTime": "2026-02-24 09:00:00",
        "createTime": "2026-02-24 08:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 9.2 获取新闻详情

**接口地址**：`GET /pandemic/news/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 新闻ID |

---

### 9.3 发布新闻

**接口地址**：`POST /pandemic/news`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 标题 |
| summary | String | 是 | 摘要 |
| content | String | 是 | 内容 |
| status | String | 是 | 状态（published/draft） |

**响应示例**：

```json
{
  "code": 200,
  "message": "发布成功"
}
```

---

### 9.4 删除新闻

**接口地址**：`DELETE /pandemic/news/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 新闻ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

### 9.5 获取防疫知识列表

**接口地址**：`GET /pandemic/knowledge`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 9.6 获取实时疫情数据

**接口地址**：`GET /pandemic/data`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "confirmed": 156,
    "cured": 120,
    "dead": 3,
    "suspected": 10,
    "updateTime": "2026-02-24 10:00:00"
  }
}
```

---

### 9.7 获取推送统计

**接口地址**：`GET /pandemic/push/stats`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    { "type": "news", "count": 50 },
    { "type": "notification", "count": 100 }
  ]
}
```

---

### 9.8 获取推送记录

**接口地址**：`GET /pandemic/push/list`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": []
}
```

---

### 9.9 发送推送

**接口地址**：`POST /pandemic/push`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 推送类型 |
| title | String | 是 | 推送标题 |
| content | String | 是 | 推送内容 |
| targetRoles | List | 否 | 目标角色 |

**响应示例**：

```json
{
  "code": 200,
  "message": "推送发送成功"
}
```

---

### 9.10 获取用户角色分布统计

**接口地址**：`GET /pandemic/push/role-stats`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    { "role": "hospital_user", "count": 50 },
    { "role": "community_staff", "count": 30 }
  ]
}
```

---

### 9.11 删除推送记录

**接口地址**：`DELETE /pandemic/push/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 推送记录ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 数据统计模块

### 10.1 获取仪表盘统计数据

**接口地址**：`GET /stats/dashboard`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalMaterials": 128,
    "pendingApplications": 15,
    "pendingDonations": 8,
    "lowStockItems": 23,
    "todayInbound": 1000,
    "todayOutbound": 800,
    "todayInboundTrend": 10,
    "todayInboundTrendType": "up",
    "todayOutboundTrend": 5,
    "todayOutboundTrendType": "down"
  }
}
```

---

### 10.2 获取用户个人统计数据

**接口地址**：`GET /stats/user`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "myApplicationCount": 10,
    "pendingApplicationCount": 2,
    "myDonationCount": 5,
    "myPendingDonationCount": 1,
    "myApprovedDonationCount": 4
  }
}
```

---

### 10.3 获取库存预警列表

**接口地址**：`GET /stats/warning`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": []
}
```

---

### 10.4 获取趋势数据

**接口地址**：`GET /stats/trend`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| period | String | 否 | 统计周期（week/month/year），默认week |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "dates": ["2026-03-23", "2026-03-24", "2026-03-25"],
    "inbound": [100, 150, 200],
    "outbound": [80, 120, 150]
  }
}
```

---

### 10.5 获取物资统计数据

**接口地址**：`GET /stats/material`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalStock": 150000,
    "typeStats": [
      { "type": "protective", "name": "防护物资", "count": 85000 },
      { "type": "disinfection", "name": "消杀物资", "count": 45000 },
      { "type": "testing", "name": "检测物资", "count": 20000 }
    ]
  }
}
```

---

### 10.6 获取申请统计数据

**接口地址**：`GET /stats/application`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalApplications": 200,
    "approvedCount": 150,
    "rejectedCount": 20,
    "pendingCount": 30
  }
}
```

---

### 10.7 获取捐赠统计数据

**接口地址**：`GET /stats/donation`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalDonations": 50,
    "totalQuantity": 50000,
    "pendingCount": 8,
    "approvedCount": 42
  }
}
```

---

### 10.8 获取实时数据大屏数据

**接口地址**：`GET /stats/dashboard/full`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "coreMetrics": {
      "totalMaterials": 128,
      "totalStock": 150000,
      "todayInbound": 1000,
      "todayOutbound": 800,
      "pendingApplications": 15,
      "pendingDonations": 8,
      "lowStockItems": 23
    },
    "materialCategoryStats": [],
    "trendData": {
      "dates": [],
      "inbound": [],
      "outbound": []
    },
    "regionStats": [],
    "warningList": [],
    "realtimeActivities": [],
    "operationLogs": []
  }
}
```

---

## 通知模块

### 11.1 获取我的通知列表

**接口地址**：`GET /notification/user`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| isRead | Integer | 否 | 是否已读（0-未读，1-已读） |

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "type": "application",
        "title": "申请已通过",
        "content": "您的物资申请已通过审核",
        "isRead": 0,
        "createTime": "2026-03-30 10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

### 11.2 获取未读通知数量

**接口地址**：`GET /notification/unread-count`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": 5
}
```

---

### 11.3 标记通知为已读

**接口地址**：`PUT /notification/{id}/read`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 通知ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "标记成功"
}
```

---

### 11.4 标记所有通知为已读

**接口地址**：`PUT /notification/read-all`

**请求头**：`Authorization: Bearer {token}`

**响应示例**：

```json
{
  "code": 200,
  "message": "标记成功"
}
```

---

### 11.5 删除通知

**接口地址**：`DELETE /notification/{id}`

**请求头**：`Authorization: Bearer {token}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 通知ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 token 过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如库存不足） |
| 500 | 服务器内部错误 |

---

## 数据库设计

### 用户表 (sys_user)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 用户ID | 主键 |
| username | VARCHAR | 用户名 | 唯一 |
| password | VARCHAR | 密码 | BCrypt加密存储 |
| name | VARCHAR | 姓名 | |
| role | VARCHAR | 角色 | admin/hospital_user/community_staff/donor |
| phone | VARCHAR | 手机号 | |
| unit | VARCHAR | 所属单位 | |
| status | VARCHAR | 状态 | active/disabled |
| create_time | DATETIME | 创建时间 | |
| update_time | DATETIME | 更新时间 | |

### 物资表 (material)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 物资ID | 主键 |
| name | VARCHAR | 物资名称 | |
| type | VARCHAR | 物资类型 | protective/disinfection/testing/equipment/other |
| specification | VARCHAR | 规格型号 | |
| unit | VARCHAR | 单位 | |
| stock | INT | 库存数量 | |
| threshold | INT | 预警阈值 | |
| warehouse | VARCHAR | 仓库 | |
| description | TEXT | 描述 | |
| status | VARCHAR | 状态 | normal/warning/low |
| create_time | DATETIME | 创建时间 | |
| update_time | DATETIME | 更新时间 | |

### 库存表 (inventory)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 库存ID | 主键 |
| material_id | VARCHAR | 物资ID | 外键 |
| warehouse | VARCHAR | 仓库 | |
| location | VARCHAR | 库位 | |
| stock | INT | 库存数量 | |
| last_check_time | DATETIME | 最后盘点时间 | |

### 出入库单表 (stock_order)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 单据ID | 主键 |
| material_id | VARCHAR | 物资ID | |
| type | VARCHAR | 单据类型 | inbound/outbound |
| quantity | INT | 数量 | |
| status | VARCHAR | 状态 | pending/approved/void |
| warehouse | VARCHAR | 仓库 | |
| remark | VARCHAR | 备注 | |
| applicant_id | BIGINT | 申请人ID | |
| applicant_name | VARCHAR | 申请人姓名 | |
| auditor_id | BIGINT | 审核人ID | |
| auditor_name | VARCHAR | 审核人姓名 | |
| create_time | DATETIME | 创建时间 | |
| audit_time | DATETIME | 审核时间 | |

### 库存变动日志表 (inventory_log)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 日志ID | 主键 |
| material_id | VARCHAR | 物资ID | |
| order_id | VARCHAR | 单据ID | |
| type | VARCHAR | 变动类型 | inbound/outbound |
| quantity | INT | 变动数量 | |
| stock_before | INT | 变动前库存 | |
| stock_after | INT | 变动后库存 | |
| operator_id | BIGINT | 操作人ID | |
| operator_name | VARCHAR | 操作人姓名 | |
| operate_time | DATETIME | 操作时间 | |

### 申请表 (application)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 申请单号 | 主键 |
| material_id | VARCHAR | 物资ID | |
| quantity | INT | 申请数量 | |
| applicant_id | BIGINT | 申请人ID | |
| applicant_name | VARCHAR | 申请人姓名 | |
| applicant_unit | VARCHAR | 申请单位 | |
| purpose | TEXT | 用途说明 | |
| urgency | VARCHAR | 紧急程度 | normal/urgent/critical |
| address | VARCHAR | 收货地址 | |
| receiver | VARCHAR | 收货人 | |
| receiver_phone | VARCHAR | 收货电话 | |
| status | VARCHAR | 状态 | pending/approved/rejected/cancelled/delivered/received |
| apply_time | DATETIME | 申请时间 | |
| approve_time | DATETIME | 审核时间 | |
| approver_id | BIGINT | 审核人ID | |
| approver_name | VARCHAR | 审核人姓名 | |
| approve_remark | TEXT | 审核意见 | |

### 捐赠表 (donation)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 捐赠单号 | 主键 |
| material_name | VARCHAR | 物资名称 | |
| type | VARCHAR | 物资类型 | |
| quantity | INT | 捐赠数量 | |
| unit | VARCHAR | 单位 | |
| donor_id | BIGINT | 捐赠人ID | |
| donor_unit | VARCHAR | 捐赠单位 | |
| contact_person | VARCHAR | 联系人 | |
| contact_phone | VARCHAR | 联系电话 | |
| receive_address | VARCHAR | 收货地址 | |
| source | VARCHAR | 物资来源 | |
| production_date | DATE | 生产日期 | |
| expiry_date | DATE | 有效期至 | |
| status | VARCHAR | 状态 | pending/approved/rejected/cancelled |
| remark | TEXT | 备注 | |
| donate_time | DATETIME | 捐赠时间 | |
| approve_time | DATETIME | 审核时间 | |
| approver_id | BIGINT | 审核人ID | |
| approver_name | VARCHAR | 审核人姓名 | |

### 疫情新闻表 (pandemic_news)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | VARCHAR | 新闻ID | 主键 |
| title | VARCHAR | 标题 | |
| summary | VARCHAR | 摘要 | |
| content | TEXT | 内容 | |
| author | VARCHAR | 作者 | |
| status | VARCHAR | 状态 | published/draft |
| view_count | INT | 浏览次数 | |
| publish_time | DATETIME | 发布时间 | |
| create_time | DATETIME | 创建时间 | |

### 操作日志表 (operate_log)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 日志ID | 主键 |
| module | VARCHAR | 模块名称 | |
| operation | VARCHAR | 操作类型 | |
| username | VARCHAR | 用户名 | |
| ip | VARCHAR | IP地址 | |
| method | VARCHAR | 请求方法 | |
| params | TEXT | 请求参数 | |
| operate_time | DATETIME | 操作时间 | |

### 用户通知表 (user_notification)

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 通知ID | 主键 |
| user_id | BIGINT | 用户ID | |
| type | VARCHAR | 通知类型 | |
| title | VARCHAR | 标题 | |
| content | TEXT | 内容 | |
| is_read | TINYINT | 是否已读 | 0-未读，1-已读 |
| create_time | DATETIME | 创建时间 | |

---

## 说明

- 所有时间格式统一使用 `YYYY-MM-DD HH:mm:ss`
- 所有金额单位统一为元（人民币）
- 分页查询页码从 1 开始
- Token 有效期为 7 天
- 微服务间通过 OpenFeign 进行通信
- 操作日志通过 RabbitMQ 异步发送

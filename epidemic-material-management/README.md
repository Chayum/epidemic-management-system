# 疫情防控物资调度管理系统 - 前端项目

## 项目简介

基于 Vue 3 + Element Plus + Pinia 的疫情防控物资调度管理系统前端项目。

## 技术栈

- **前端框架**：Vue 3
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **路由管理**：Vue Router
- **HTTP 客户端**：Axios
- **构建工具**：Vite
- **图表库**：ECharts
- **Mock 数据**：Mock.js
- **样式**：SCSS

## 项目结构

```
epidemic-material-management/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口模块
│   │   ├── auth.js        # 认证相关接口
│   │   ├── material.js    # 物资管理接口
│   │   ├── application.js # 申请管理接口
│   │   ├── donation.js    # 捐赠管理接口
│   │   ├── pandemic.js  # 疫情信息接口
│   │   └── stats.js      # 数据统计接口
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── layout/            # 布局组件
│   │   ├── index.vue     # 管理端布局
│   │   └── user.vue     # 用户端布局
│   ├── mock/              # Mock 数据
│   │   └── index.js
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # Pinia 状态管理
│   │   ├── user.js       # 用户状态
│   │   └── app.js        # 应用状态
│   ├── styles/            # 样式文件
│   │   └── index.scss
│   ├── utils/             # 工具函数
│   │   └── request.js   # axios 请求封装
│   ├── views/             # 页面组件
│   │   ├── dashboard/   # 管理端页面
│   │   ├── login/      # 登录页面
│   │   ├── material/   # 物资管理
│   │   ├── pandemic/   # 疫情信息
│   │   ├── user/       # 用户管理
│   │   └── user/       # 用户端页面
│   ├── App.vue
│   └── main.js
├── .env.development
├── .env.production
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 功能模块

### 管理端功能

1. **控制台** - 数据概览
2. **用户管理** - 用户列表、个人信息
3. **物资管理** - 物资捐赠、库存管理、物资申领、申领审批、物资追踪
4. **疫情信息** - 疫情动态、消息推送、防控知识库

### 用户端功能

1. **首页** - 数据概览
2. **我要捐赠** - 捐赠物资
3. **物资申领** - 申请物资
4. **我的申请** - 申请记录
5. **我的捐赠** - 捐赠记录
6. **个人中心** - 个人信息、修改密码、消息设置

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 环境变量

### 开发环境 (.env.development)

```env
VITE_API_BASE_URL=/api
```

### 生产环境 (.env.production)

```env
VITE_API_BASE_URL=https://api.example.com
```

## 核心功能说明

### 1. 状态管理 (Pinia)

项目使用 Pinia 进行状态管理，包含两个 Store：

- **user.js** - 用户信息、token、登录/登出
- **app.js** - 应用状态、侧边栏、通知

### 2. 请求封装 (Axios)

- 统一的请求/响应拦截器
- Token 自动添加
- 统一的错误处理
- 超时设置

### 3. 路由守卫

- 登录状态检查
- 角色权限验证
- 页面标题设置

### 4. Mock 数据

开发环境自动加载 Mock 数据，无需后端即可测试。

## 用户角色说明

| 角色 | 说明 |
|------|------|
| admin | 管理员 |
| hospital_user | 医院用户 |
| community_staff | 社区人员 |
| donor | 捐赠方 |

## 接口文档

详细的前后端接口文档请查看项目根目录下的 `API文档.md`。

## 开发规范

### 代码规范

- 使用 Vue 3 Composition API
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case
- 常量命名使用 UPPER_SNAKE_CASE
- 方法命名使用 camelCase

### 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

## 浏览器支持

- Chrome >= 90+
- Edge >= 90+
- Firefox >= 88+
- Safari >= 14+

## 许可证

MIT

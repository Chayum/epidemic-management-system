<template>
  <div class="app-wrapper" :class="{ collapsed: isCollapsed }">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <!-- Logo 区域 -->
      <div class="logo-area">
        <div class="logo-icon-wrap">
          <el-icon class="logo-icon" :size="32"><FirstAidKit /></el-icon>
        </div>
        <transition name="fade">
          <span v-if="!isCollapsed" class="logo-text">防疫物资系统</span>
        </transition>
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff"
        :router="true"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        
        <el-sub-menu index="/user-manage">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/user-manage/list">用户列表</el-menu-item>
          <el-menu-item index="/user-manage/profile">个人信息</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/material">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>物资管理</span>
          </template>
          <el-menu-item index="/material/donation">物资捐赠</el-menu-item>
          <el-menu-item index="/material/inventory">库存管理</el-menu-item>
          <el-menu-item index="/material/apply">物资申领</el-menu-item>
          <el-menu-item index="/material/approval">申领审批</el-menu-item>
          <el-menu-item index="/material/track">物资追踪</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/pandemic">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>疫情信息</span>
          </template>
          <el-menu-item index="/pandemic/news">疫情动态</el-menu-item>
          <el-menu-item index="/pandemic/push">消息推送</el-menu-item>
        </el-sub-menu>
      </el-menu>
      
      <!-- 底部折叠按钮 -->
      <div class="sidebar-footer" @click="toggleCollapse">
        <el-icon :class="isCollapsed ? 'rotate-180' : ''">
          <DArrowLeft />
        </el-icon>
        <span v-if="!isCollapsed" class="footer-text">收起菜单</span>
        <span v-else class="footer-text">展开菜单</span>
      </div>
    </aside>
    
    <!-- 主内容区域 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <div class="breadcrumb-wrap">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute.meta.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <div class="user-avatar">
                <el-avatar :size="38" style="background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)">
                  {{ userInfo.name?.charAt(0) || '管' }}
                </el-avatar>
              </div>
              <div class="user-details">
                <span class="username">{{ userInfo.name || '管理员' }}</span>
                <span class="user-role">系统管理员</span>
              </div>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  FirstAidKit, Monitor, User, Box, DataAnalysis,
  ArrowDown, Setting, SwitchButton, DArrowLeft
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const userInfo = computed(() => userStore.userInfo || {})

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/user-manage/profile')
  }
}
</script>

<style scoped lang="scss">
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-base);
}

/* ==================== 侧边栏样式 ==================== */
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: relative;
  z-index: 100;
  
  /* 侧边栏顶部装饰线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 50%, #10b981 100%);
  }
}

.app-wrapper.collapsed .sidebar {
  width: var(--sidebar-collapsed);
}

/* Logo 区域 */
.logo-area {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(6, 182, 212, 0.2) 100%);
  border-radius: 12px;
  flex-shrink: 0;
}

.logo-icon {
  color: #3b82f6;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 菜单样式 */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 0;
  background: transparent !important;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 2px;
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    margin: 4px 12px;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      background: rgba(59, 130, 246, 0.15) !important;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(6, 182, 212, 0.15) 100%) !important;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 24px;
      background: linear-gradient(180deg, #3b82f6 0%, #06b6d4 100%);
      border-radius: 0 4px 4px 0;
    }
  }
  
  :deep(.el-sub-menu) {
    &.is-active > .el-sub-menu__title {
      color: #fff !important;
    }
  }
  
  :deep(.el-sub-menu__title) {
    color: rgba(255, 255, 255, 0.7);
  }
  
  :deep(.el-menu--inline) {
    background: rgba(0, 0, 0, 0.2) !important;
    
    .el-menu-item {
      margin: 4px 8px;
      height: 40px;
      line-height: 40px;
    }
  }
}

.app-wrapper.collapsed .sidebar-menu {
  padding: 12px 0;

  /* 覆盖 Element Plus 默认折叠宽度 64px，匹配侧边栏 80px */
  &:deep(.el-menu) {
    width: var(--sidebar-collapsed) !important;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 !important;
    margin: 4px auto !important;
    width: 48px;
    height: 48px;
    line-height: 48px;
    border-radius: 12px;
  }
  
  :deep(.el-sub-menu__title) {
    span {
      display: none;
    }
    
    /* 隐藏折叠状态下子菜单箭头 */
    .el-sub-menu__icon-arrow {
      display: none;
    }
    
    .el-icon {
      margin-right: 0;
      font-size: 20px;
    }
  }
  
  :deep(.el-menu-item) {
    .el-icon {
      margin-right: 0;
      font-size: 20px;
    }
    
    /* 隐藏 title slot 内容 */
    .el-menu-tooltip__trigger {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  :deep(.el-sub-menu) {
    &.is-active > .el-sub-menu__title {
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(6, 182, 212, 0.15) 100%) !important;
    }
    
    &.is-opened > .el-sub-menu__title {
      background: rgba(59, 130, 246, 0.2) !important;
    }
  }
  
  /* 折叠时活跃菜单项的左侧指示条 */
  :deep(.el-menu-item.is-active) {
    &::before {
      left: 0;
      width: 3px;
      height: 20px;
    }
  }
}

/* 侧边栏底部 */
.sidebar-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 0 0 0 0;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(6, 182, 212, 0.1) 100%);
    opacity: 0;
    transition: opacity 0.3s;
  }
  
  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.05);
    
    &::before {
      opacity: 1;
    }
  }
  
  &:active {
    transform: scale(0.98);
  }
  
  .el-icon {
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-size: 16px;
  }
  
  .rotate-180 {
    transform: rotate(180deg);
  }
  
  .footer-text {
    font-weight: 500;
    letter-spacing: 0.5px;
    transition: all 0.3s;
  }
}

.app-wrapper.collapsed .sidebar-footer {
  .footer-text {
    display: none;
  }
  
  justify-content: center;
  padding: 16px 8px;
  
  &:hover {
    background: rgba(59, 130, 246, 0.2);
  }
}

/* ==================== 主内容区域样式 ==================== */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-base);
}

/* 顶部导航栏 */
.header {
  height: var(--header-height);
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb-wrap {
  :deep(.el-breadcrumb__inner) {
    font-weight: 500;
    color: var(--text-regular);
    
    &.is-link:hover {
      color: var(--primary-light);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-action {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
  
  &:hover {
    background: var(--bg-base);
    color: var(--primary-light);
  }
}

.notification-badge {
  :deep(.el-badge__content) {
    background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
    border: none;
  }
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px 6px 6px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: var(--bg-base);
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details {
  display: flex;
  flex-direction: column;
  
  .username {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    line-height: 1.3;
  }
  
  .user-role {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.dropdown-arrow {
  color: var(--text-placeholder);
  margin-left: 4px;
  transition: transform 0.3s;
}

.user-info:hover .dropdown-arrow {
  transform: translateY(2px);
}

/* 用户下拉菜单 */
.user-dropdown {
  min-width: 180px;
  padding: 8px;
  
  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 8px;
    margin: 4px 0;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

/* ==================== 页面内容区域 ==================== */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-base);
}

/* ==================== 过渡动画 ==================== */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    
    &.mobile-open {
      transform: translateX(0);
    }
  }
  
  .user-details {
    display: none;
  }
}
</style>

<!-- 非 scoped 样式：用于折叠菜单弹出层（渲染在 body 上，scoped 无法覆盖） -->
<style lang="scss">
/* 折叠侧边栏弹出子菜单 */
.el-menu--popup {
  min-width: 160px !important;
  padding: 6px !important;
  border-radius: 12px !important;
  border: 1px solid var(--border-light, #f1f5f9) !important;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.08) !important;
  background: #fff !important;

  .el-menu-item {
    height: 38px !important;
    line-height: 38px !important;
    padding: 0 16px !important;
    margin: 3px 6px !important;
    border-radius: 8px !important;
    font-size: 14px !important;
    color: #4b5563 !important;
    transition: all 0.2s ease !important;
    position: relative;

    &:hover {
      background: rgba(59, 130, 246, 0.08) !important;
      color: #3b82f6 !important;
    }

    &.is-active {
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(6, 182, 212, 0.08) 100%) !important;
      color: #3b82f6 !important;
      font-weight: 500 !important;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 18px;
        background: linear-gradient(180deg, #3b82f6 0%, #06b6d4 100%);
        border-radius: 0 3px 3px 0;
      }
    }
  }
}
</style>

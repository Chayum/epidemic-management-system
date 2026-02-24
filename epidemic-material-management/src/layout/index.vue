<template>
  <div class="app-wrapper" :class="{ collapsed: isCollapsed }">
    <aside class="sidebar">
      <div class="logo-area">
        <el-icon class="logo-icon" :size="28"><FirstAidKit /></el-icon>
        <span v-if="!isCollapsed" class="logo-text">防疫物资系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
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
          <el-menu-item index="/pandemic/knowledge">防控知识库</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>
    
    <div class="main-container">
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                {{ userInfo.name?.charAt(0) || '管' }}
              </el-avatar>
              <span class="username">{{ userInfo.name || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const isCollapsed = ref(false)
const userInfo = ref({
  name: '管理员',
  role: 'admin'
})

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
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
}

.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--bg-dark);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  flex-shrink: 0;
  z-index: 100;
}

.app-wrapper.collapsed .sidebar {
  width: 64px;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  color: #1890ff;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-base);
}

.header {
  height: var(--header-height);
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
  
  &:hover {
    color: var(--primary-color);
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.3s;
  
  &:hover {
    background: #f5f5f5;
  }
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

:deep(.el-menu--collapse) {
  .el-sub-menu__title {
    justify-content: center;
    padding: 0 12px !important;
  }
  
  .el-menu-item {
    justify-content: center;
    padding: 0 12px !important;
  }
}

:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.2) !important;
}

:deep(.el-menu-item.is-active) {
  background: #1890ff !important;
}
</style>

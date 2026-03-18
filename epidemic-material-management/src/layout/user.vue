<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-container">
        <div class="header-left">
          <el-icon class="logo-icon" :size="32"><FirstAidKit /></el-icon>
          <span class="logo-text">防疫物资调度系统</span>
        </div>
        <div class="header-right">
          <div class="notification-bell" @click="goToNotification">
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
              <el-icon :size="24"><Bell /></el-icon>
            </el-badge>
          </div>
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            class="header-menu"
            :router="true"
            :ellipsis="false"
          >
            <el-menu-item index="/user/home">首页</el-menu-item>
            <el-menu-item v-if="canDonate" index="/user/donation">我要捐赠</el-menu-item>
            <el-menu-item v-if="canApply" index="/user/apply">物资申领</el-menu-item>
            <el-menu-item v-if="canApply" index="/user/my-application">我的申请</el-menu-item>
            <el-menu-item v-if="canDonate" index="/user/my-donation">我的捐赠</el-menu-item>
          </el-menu>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                {{ userInfo.name?.charAt(0) || '用' }}
              </el-avatar>
              <span class="username">{{ userInfo.name || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="notification">
                  <el-icon><Bell /></el-icon>消息通知
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <main class="user-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>
    
    <footer class="user-footer">
      <p>© 2026 疫情防控指挥部 · 防疫物资调度管理系统</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { FirstAidKit, User, Setting, SwitchButton, ArrowDown, More, Bell } from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})
const userRole = computed(() => userStore.userRole)
const canDonate = computed(() => ['donor', 'community_staff', 'hospital_user'].includes(userRole.value))
const canApply = computed(() => ['hospital_user', 'community_staff'].includes(userRole.value))

const activeMenu = computed(() => route.path)

const unreadCount = ref(0)
let refreshTimer = null

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读通知数量失败', error)
  }
}

const goToNotification = () => {
  router.push('/user/notification')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/user/login')
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'notification') {
    router.push('/user/notification')
  }
}

// 定时刷新未读数量
const startRefreshTimer = () => {
  fetchUnreadCount()
  refreshTimer = setInterval(() => {
    if (userStore.isLoggedIn) {
      fetchUnreadCount()
    }
  }, 30000)
}

onMounted(() => {
  startRefreshTimer()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.user-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: #1890ff;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.notification-bell {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;

  &:hover {
    background: #f5f5f5;
  }
}

.header-menu {
  border-bottom: none;
  
  .el-menu-item {
    height: 64px;
    line-height: 64px;
  }
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

.user-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.user-footer {
  background: #fff;
  padding: 24px;
  text-align: center;
  border-top: 1px solid #e8e8e8;
  
  p {
    font-size: 13px;
    color: #8c8c8c;
  }
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}
</style>

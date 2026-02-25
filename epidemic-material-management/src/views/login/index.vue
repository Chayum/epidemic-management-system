<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <!-- 顶部切换按钮 -->
    <div class="switch-mode-btn" @click="toggleMode">
      <el-icon><Switch /></el-icon>
      <span>{{ isUserLogin ? '切换为管理员登录' : '切换为用户登录' }}</span>
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48" color="#1890ff"><FirstAidKit /></el-icon>
        </div>
        <h1 class="title">疫情防控物资调度管理系统</h1>
        <p class="subtitle">Epidemic Prevention Material Management System</p>
      </div>
      
      <!-- 角色选择器 (仅用户登录显示) -->
      <transition name="el-zoom-in-top">
        <div v-if="isUserLogin" class="role-selector">
          <div 
            v-for="role in roles" 
            :key="role.value" 
            class="role-card"
            :class="{ active: selectedRole === role.value }"
            @click="selectedRole = role.value"
          >
            <el-icon :size="28" :color="selectedRole === role.value ? '#1890ff' : '#8c8c8c'">
              <component :is="role.icon" />
            </el-icon>
            <span class="role-name">{{ role.label }}</span>
            <span class="role-desc">{{ role.desc }}</span>
          </div>
        </div>
      </transition>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <div v-if="isUserLogin" class="footer-links">
          <el-link type="primary" :underline="false">帮助中心</el-link>
          <el-divider direction="vertical" />
          <el-link type="primary" :underline="false">隐私政策</el-link>
          <el-divider direction="vertical" />
          <el-link type="primary" :underline="false">服务条款</el-link>
        </div>
        <p class="copyright">© 2026 疫情防控指挥部 · 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
import { Lock, User, InfoFilled, FirstAidKit, OfficeBuilding, Switch, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

// 角色定义
const roles = [
  { 
    value: 'hospital_user', 
    label: '医院/机构', 
    desc: '物资申领',
    icon: FirstAidKit 
  },
  { 
    value: 'community_staff', 
    label: '社区人员', 
    desc: '社区防控',
    icon: OfficeBuilding 
  },
  { 
    value: 'donor', 
    label: '捐赠方', 
    desc: '物资捐赠',
    icon: UserFilled 
  }
]

// 状态
const selectedRole = ref('hospital_user')
const loginForm = reactive({
  username: '',
  password: ''
})

// 根据路由参数判断当前模式
const isUserLogin = computed(() => route.query.type === 'user')

// 切换模式
const toggleMode = () => {
  const targetType = isUserLogin.value ? undefined : 'user'
  router.replace({ 
    path: '/login', 
    query: { 
      ...route.query, 
      type: targetType 
    } 
  })
}

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 确定角色：如果是用户登录模式，使用选中的角色；否则（管理员模式）固定为 admin
        const role = isUserLogin.value ? selectedRole.value : 'admin'
        
        await userStore.login({
          username: loginForm.username,
          password: loginForm.password,
          role: role
        })
        
        ElMessage.success('登录成功')
        
        // 根据角色跳转不同首页
        const targetPath = role === 'admin' ? '/dashboard' : '/user/home'
        router.push(targetPath)
        
      } catch (error) {
        ElMessage.error(error.message || '登录失败，请检查用户名密码')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0a2540 0%, #1a3a5c 50%, #0d2137 100%);
}

.switch-mode-btn {
  position: absolute;
  top: 24px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  font-size: 14px;
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(2px);
  }
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  bottom: -100px;
  left: -50px;
  animation: float 15s ease-in-out infinite reverse;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.login-box {
  position: relative;
  width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-radius: 20px;
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 13px;
  color: #8c8c8c;
  letter-spacing: 1px;
}

/* Role Selector Styles */
.role-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.role-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: #fafafa;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #1890ff;
    background: #e6f7ff;
  }
  
  &.active {
    border-color: #1890ff;
    background: #e6f7ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  }
}

.role-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  margin-top: 8px;
}

.role-desc {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
  transform: scale(0.9);
}

.login-form {
  :deep(.el-input__wrapper) {
    padding: 8px 12px;
    border-radius: 8px;
  }
  
  :deep(.el-input__inner) {
    height: 40px;
  }
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(24, 144, 255, 0.4);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  
  .footer-links {
    margin-bottom: 12px;
    .el-link {
      font-size: 12px;
      color: #8c8c8c;
      &:hover { color: #1890ff; }
    }
  }

  .copyright {
    font-size: 12px;
    color: #bfbfbf;
  }
}
</style>

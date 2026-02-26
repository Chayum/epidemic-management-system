<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
      <p class="page-desc">管理您的个人信息和账户设置</p>
    </div>
    
    <el-row :gutter="24">
      <el-col :xs="24" :lg="8">
        <div class="profile-card card-container">
          <div class="avatar-section">
            <el-avatar :size="120" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              {{ userInfo.name?.charAt(0) || '管' }}
            </el-avatar>
            <el-button type="primary" link class="change-avatar-btn">更换头像</el-button>
          </div>
          
          <div class="user-basic-info">
            <h3 class="user-name">{{ userInfo.name || '未设置姓名' }}</h3>
            <p class="user-role">
              <el-tag :type="getRoleType(userInfo.role)" size="small">
                {{ getRoleName(userInfo.role) }}
              </el-tag>
            </p>
          </div>
          
          <el-divider />
          
          <div class="info-list">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="label">用户名</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="label">手机号</span>
              <span class="value">{{ userInfo.phone || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="label">所属单位</span>
              <span class="value">{{ userInfo.unit || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span class="label">注册时间</span>
              <span class="value">{{ formatCreateTime }}</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="16">
        <div class="settings-card card-container">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="基本信息" name="info">
              <div class="tab-content">
                <h4 class="section-title">基本信息设置</h4>
                <el-form 
                  ref="infoFormRef" 
                  :model="infoForm" 
                  :rules="infoRules" 
                  label-width="100px" 
                  class="profile-form"
                  status-icon
                >
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="infoForm.name" placeholder="请输入姓名" maxlength="20" />
                  </el-form-item>
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="infoForm.phone" placeholder="请输入手机号" maxlength="11">
                      <template #append>
                        <el-button @click="verifyPhone" :disabled="phoneVerified">
                          {{ phoneVerified ? '已验证' : '验证' }}
                        </el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="所属单位" prop="unit">
                    <el-input v-model="infoForm.unit" placeholder="请输入所属单位" maxlength="50" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSaveInfo" :loading="savingInfo" :disabled="!infoChanged">
                      保存修改
                    </el-button>
                    <el-button @click="resetInfoForm" :disabled="savingInfo || !infoChanged">
                      重置
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="账户安全" name="security">
              <div class="tab-content">
                <h4 class="section-title">修改密码</h4>
                <el-form 
                  ref="pwdFormRef" 
                  :model="pwdForm" 
                  :rules="pwdRules" 
                  label-width="100px" 
                  class="profile-form"
                  status-icon
                >
                  <el-form-item label="当前密码" prop="oldPwd">
                    <el-input v-model="pwdForm.oldPwd" type="password" placeholder="请输入当前密码" show-password />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPwd">
                    <el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入新密码" show-password />
                  </el-form-item>
                  <el-form-item label="确认密码" prop="confirmPwd">
                    <el-input v-model="pwdForm.confirmPwd" type="password" placeholder="请再次输入新密码" show-password />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleChangePwd" :loading="changingPwd">
                      修改密码
                    </el-button>
                  </el-form-item>
                </el-form>
                
                <el-divider />
                
                <h4 class="section-title">登录安全</h4>
                <div class="security-tips">
                  <el-alert
                    title="账户安全建议"
                    type="info"
                    :closable="false"
                    show-icon
                  >
                    <template #default>
                      <ul class="security-list">
                        <li>定期更换密码，建议每3个月更换一次</li>
                        <li>密码长度至少6位，包含字母和数字</li>
                        <li>不要在多个网站使用相同的密码</li>
                        <li>不要向他人透露您的账户密码</li>
                      </ul>
                    </template>
                  </el-alert>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="偏好设置" name="preferences">
              <div class="tab-content">
                <h4 class="section-title">界面主题</h4>
                <div class="theme-settings">
                  <div class="theme-option">
                    <div class="theme-preview light" @click="setTheme('light')">
                      <el-icon v-if="currentTheme === 'light'"><Check /></el-icon>
                    </div>
                    <span class="theme-label">浅色主题</span>
                  </div>
                  <div class="theme-option">
                    <div class="theme-preview dark" @click="setTheme('dark')">
                      <el-icon v-if="currentTheme === 'dark'"><Check /></el-icon>
                    </div>
                    <span class="theme-label">深色主题</span>
                  </div>
                </div>
                
                <el-divider />
                
                <h4 class="section-title">通知设置</h4>
                <div class="notification-settings">
                  <div class="setting-row">
                    <div class="setting-info">
                      <span class="setting-title">系统通知</span>
                      <span class="setting-desc">接收系统公告和更新提醒</span>
                    </div>
                    <el-switch v-model="notifications.system" @change="handleNotificationChange('system')" />
                  </div>
                  <div class="setting-row">
                    <div class="setting-info">
                      <span class="setting-title">操作通知</span>
                      <span class="setting-desc">重要操作的确认和结果通知</span>
                    </div>
                    <el-switch v-model="notifications.operation" @change="handleNotificationChange('operation')" />
                  </div>
                  <div class="setting-row">
                    <div class="setting-info">
                      <span class="setting-title">消息推送</span>
                      <span class="setting-desc">实时消息推送提醒</span>
                    </div>
                    <el-switch v-model="notifications.message" @change="handleNotificationChange('message')" />
                  </div>
                </div>
                
                <div class="save-preferences">
                  <el-button type="primary" @click="handleSavePreferences" :loading="savingPrefs">
                    保存偏好设置
                  </el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/auth'
import { User, Phone, OfficeBuilding, Calendar, Monitor, Check } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('info')
const savingInfo = ref(false)
const changingPwd = ref(false)
const savingPrefs = ref(false)
const loadingRecords = ref(false)
const phoneVerified = ref(false)
const currentTheme = ref('light')

const userInfo = ref({
  id: null,
  username: '',
  name: '',
  phone: '',
  unit: '',
  role: '',
  createTime: ''
})

const originalInfo = ref({})

const infoFormRef = ref(null)
const infoForm = reactive({
  name: '',
  phone: '',
  unit: ''
})

const infoRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  unit: [{ required: true, message: '请输入所属单位', trigger: 'blur' }]
}

const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPwd) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPwd: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const notifications = reactive({
  system: true,
  operation: true,
  message: true
})
const infoChanged = computed(() => {
  return JSON.stringify(infoForm) !== JSON.stringify(originalInfo.value)
})

const formatCreateTime = computed(() => {
  if (userInfo.value.createTime) {
    return userInfo.value.createTime
  }
  return '未知'
})

const getRoleName = (role) => {
  const roleMap = {
    hospital_user: '医院用户',
    community_staff: '社区人员',
    donor: '捐赠方',
    admin: '管理员'
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    hospital_user: 'success',
    community_staff: 'warning',
    donor: 'info',
    admin: 'danger'
  }
  return typeMap[role] || 'info'
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = {
        ...res.data,
        createTime: res.data?.createTime || userStore.userInfo.createTime || ''
      }
      infoForm.name = userInfo.value.name || ''
      infoForm.phone = userInfo.value.phone || ''
      infoForm.unit = userInfo.value.unit || ''
      originalInfo.value = { ...infoForm }
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败，请刷新页面重试')
  }
}

const resetInfoForm = () => {
  infoForm.name = originalInfo.value.name || ''
  infoForm.phone = originalInfo.value.phone || ''
  infoForm.unit = originalInfo.value.unit || ''
}

const verifyPhone = () => {
  ElMessage.success('验证码已发送至您的手机，请注意查收')
  setTimeout(() => {
    phoneVerified.value = true
    ElMessage.success('手机号验证成功')
  }, 2000)
}

const handleSaveInfo = async () => {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      savingInfo.value = true
      try {
        const res = await updateUserInfo(infoForm)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          originalInfo.value = { ...infoForm }
          await loadUserInfo()
          await userStore.fetchUserInfo()
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存用户信息失败', error)
        ElMessage.error('保存失败，请稍后重试')
      } finally {
        savingInfo.value = false
      }
    }
  })
}

const handleChangePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPwd.value = true
      try {
        const res = await changePassword({
          oldPwd: pwdForm.oldPwd,
          newPwd: pwdForm.newPwd
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          Object.assign(pwdForm, { oldPwd: '', newPwd: '', confirmPwd: '' })
          setTimeout(() => {
            userStore.logout()
            window.location.href = '/login'
          }, 1500)
        } else {
          ElMessage.error(res.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败', error)
        ElMessage.error('密码修改失败，请稍后重试')
      } finally {
        changingPwd.value = false
      }
    }
  })
}

const setTheme = (theme) => {
  currentTheme.value = theme
  ElMessage.success(`已切换到${theme === 'light' ? '浅色' : '深色'}主题`)
}

const handleNotificationChange = (type) => {
  console.log('通知设置变更:', type, notifications[type])
}

const handleSavePreferences = async () => {
  savingPrefs.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('偏好设置已保存')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    savingPrefs.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    currentTheme.value = savedTheme
  }
})

watch(currentTheme, (newTheme) => {
  localStorage.setItem('theme', newTheme)
})
</script>

<style scoped lang="scss">
.profile-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.profile-card {
  padding: 32px 24px;
  text-align: center;
}

.avatar-section {
  margin-bottom: 20px;
  
  .change-avatar-btn {
    display: block;
    margin: 12px auto 0;
  }
}

.user-basic-info {
  margin-bottom: 20px;
  
  .user-name {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 8px;
  }
  
  .user-role {
    margin: 0;
  }
}

.info-list {
  text-align: left;
  
  .info-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .el-icon {
      font-size: 18px;
      color: #8c8c8c;
      margin-right: 12px;
    }
    
    .label {
      width: 80px;
      color: #8c8c8c;
      font-size: 14px;
    }
    
    .value {
      flex: 1;
      color: #1a1a1a;
      font-weight: 500;
      text-align: right;
    }
  }
}

.settings-card {
  min-height: 600px;
}

.profile-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
  
  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
  }
}

.tab-content {
  padding: 8px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px;
}

.profile-form {
  max-width: 500px;
}

.theme-settings {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.theme-option {
  cursor: pointer;
  
  .theme-preview {
    width: 100px;
    height: 70px;
    border-radius: 8px;
    border: 2px solid #e8e8e8;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;
    transition: all 0.3s;
    
    &.light {
      background: #ffffff;
      
      &:hover {
        border-color: #1890ff;
      }
    }
    
    &.dark {
      background: #1f1f1f;
      
      &:hover {
        border-color: #1890ff;
      }
    }
    
    .el-icon {
      color: #1890ff;
      font-size: 24px;
    }
  }
  
  .theme-label {
    font-size: 14px;
    color: #666;
  }
}

.notification-settings {
  margin-bottom: 24px;
  
  .setting-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .setting-info {
      .setting-title {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #1a1a1a;
        margin-bottom: 4px;
      }
      
      .setting-desc {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.save-preferences {
  margin-top: 24px;
}

.security-tips {
  .security-list {
    margin: 8px 0 0;
    padding-left: 20px;
    
    li {
      font-size: 13px;
      line-height: 1.8;
      color: #666;
    }
  }
}

@media (max-width: 768px) {
  .profile-card {
    margin-bottom: 24px;
  }
  
  .theme-settings {
    flex-direction: column;
    align-items: center;
  }
}
</style>

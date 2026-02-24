import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

// 定义用户状态 store
export const useUserStore = defineStore('user', () => {
  // 用户 token
  const token = ref(localStorage.getItem('token') || '')
  
  // 用户信息
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 用户角色
  const userRole = computed(() => userInfo.value.role || '')

  // 设置 token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    if (info && Object.keys(info).length > 0) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  // 登录
  const login = async (loginData) => {
    // 调用后端登录接口
    const res = await loginApi(loginData)
    
    if (res.code === 200) {
      setToken(res.data.token)
      setUserInfo(res.data.userInfo)
      return res
    } else {
      throw new Error(res.message || '登录失败')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    
    try {
      const res = await getUserInfoApi()
      if (res.code === 200) {
        setUserInfo(res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 清除本地存储数据（开发调试用）
  const clearLocalStorage = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    setToken,
    setUserInfo,
    login,
    fetchUserInfo,
    logout,
    clearLocalStorage
  }
})

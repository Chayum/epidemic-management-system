/**
 * 用户状态管理 Store
 * 基于 Pinia 实现，负责管理用户登录状态、Token、个人信息及权限角色
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // --- State ---

  // 用户 Token，使用 sessionStorage 实现：每个标签页独立登录状态
  const token = ref(sessionStorage.getItem('token') || '')

  // 用户基本信息对象
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))

  // --- Getters ---

  // 计算属性：判断是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 计算属性：获取当前用户角色
  const userRole = computed(() => userInfo.value.role || '')

  // --- Actions ---

  /**
   * 设置 Token 并持久化到 sessionStorage
   * 使用 sessionStorage 实现：每个标签页独立登录状态
   * @param {string} newToken - 新的 Token 字符串
   */
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      sessionStorage.setItem('token', newToken)
    } else {
      sessionStorage.removeItem('token')
    }
  }

  /**
   * 设置用户信息并持久化到 sessionStorage
   * 使用 sessionStorage 实现：每个标签页独立登录状态
   * @param {Object} info - 用户信息对象
   */
  const setUserInfo = (info) => {
    userInfo.value = info || {}
    if (info && Object.keys(info).length > 0) {
      sessionStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      sessionStorage.removeItem('userInfo')
    }
  }

  /**
   * 登录动作
   * 调用后端登录接口，成功后保存 Token 和用户信息
   * @param {Object} loginData - 登录表单数据 {username, password, role}
   * @returns {Promise} 接口响应结果
   */
  const login = async (loginData) => {
    try {
      // 调用后端登录接口
      const res = await loginApi(loginData)
      
      if (res.code === 200) {
        // 登录成功，保存状态
        setToken(res.data.token)
        // 确保用户信息中包含 role 字段
        const userInfo = {
          ...res.data.userInfo,
          role: res.data.userInfo.role || loginData.role
        }
        setUserInfo(userInfo)
        return res
      } else {
        throw new Error(res.message || '登录失败')
      }
    } catch (error) {
      throw error
    }
  }

  /**
   * 异步获取最新用户信息
   * 用于页面刷新或需要更新用户状态时调用
   */
  const fetchUserInfo = async () => {
    if (!token.value) return
    
    try {
      const res = await getUserInfoApi()
      if (res.code === 200) {
        setUserInfo(res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
      // 若获取用户信息失败（如Token失效），可视情况触发登出
    }
  }

  /**
   * 登出动作
   * 清除 Token 和用户信息，重置状态
   */
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }

  /**
   * 辅助方法：清除所有本地存储数据
   * 通常用于开发调试或强制重置环境
   */
  const clearLocalStorage = () => {
    logout()
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

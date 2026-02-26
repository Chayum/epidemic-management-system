/**
 * HTTP请求封装工具
 * 基于Axios封装，提供请求/响应拦截、Token注入、统一错误处理等功能
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  // API 请求的基础路径，优先从环境变量读取
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // 请求超时时间（毫秒）
  timeout: 15000
})

/**
 * 请求拦截器
 * 在请求发送前统一注入Token，实现身份认证
 */
request.interceptors.request.use(
  config => {
    // 从 Pinia Store 获取用户 token
    const userStore = useUserStore()
    const token = userStore.token

    // 如果有 token，添加到请求头 Authorization 字段
    // 格式通常为 Bearer <token>
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    return config
  },
  error => {
    // 请求发送失败的处理
    console.error('请求发送错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理后端返回的状态码和网络错误
 */
request.interceptors.response.use(
  response => {
    const res = response.data

    // 业务状态码判断，非200视为业务异常
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: 未登录或 token 过期
      // 触发登出操作并跳转至登录页
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '业务处理失败'))
    }

    // 请求成功，直接返回业务数据
    return res
  },
  error => {
    // HTTP 状态码错误处理（如 4xx, 5xx）
    console.error('响应错误:', error)

    if (error.response) {
      // 根据 HTTP 状态码提示不同错误信息
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的接口不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请联系管理员')
          break
        default:
          ElMessage.error(error.response.data.message || '未知错误')
      }
    } else if (error.request) {
      // 请求已发出但无响应
      ElMessage.error('网络连接超时，请检查网络')
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default request

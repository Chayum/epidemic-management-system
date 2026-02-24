// 通用工具函数

import dayjs from 'dayjs'

// 格式化日期
export const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化日期 - 仅日期
export const formatDateOnly = (date) => {
  return formatDate(date, 'YYYY-MM-DD')
}

// 格式化日期 - 仅时间
export const formatTimeOnly = (date) => {
  return formatDate(date, 'HH:mm:ss')
}

// 相对时间
export const formatRelativeTime = (date) => {
  if (!date) return ''
  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'second')
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)} 天前`
  
  return formatDateOnly(date)
}

// 获取物资类型名称
export const getMaterialTypeName = (type) => {
  const typeMap = {
    protective: '防护物资',
    disinfection: '消杀物资',
    testing: '检测物资',
    equipment: '医疗设备',
    other: '其他物资'
  }
  return typeMap[type] || type
}

// 获取申请状态名称
export const getApplicationStatusName = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消',
    delivered: '已发货',
    received: '已收货'
  }
  return statusMap[status] || status
}

// 获取申请状态类型（用于 Element Plus Tag）
export const getApplicationStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info',
    delivered: 'primary',
    received: ''
  }
  return typeMap[status] || ''
}

// 获取捐赠状态名称
export const getDonationStatusName = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

// 获取捐赠状态类型
export const getDonationStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || ''
}

// 获取紧急程度名称
export const getUrgencyName = (urgency) => {
  const urgencyMap = {
    normal: '普通',
    urgent: '较急',
    critical: '紧急'
  }
  return urgencyMap[urgency] || urgency
}

// 获取紧急程度类型
export const getUrgencyType = (urgency) => {
  const typeMap = {
    normal: '',
    urgent: 'warning',
    critical: 'danger'
  }
  return typeMap[urgency] || ''
}

// 获取用户角色名称
export const getRoleName = (role) => {
  const roleMap = {
    admin: '管理员',
    hospital_user: '医院用户',
    community_staff: '社区人员',
    donor: '捐赠方'
  }
  return roleMap[role] || role
}

// 获取用户角色类型
export const getRoleType = (role) => {
  const typeMap = {
    admin: 'danger',
    hospital_user: 'success',
    community_staff: 'warning',
    donor: 'info'
  }
  return typeMap[role] || ''
}

// 格式化数字（千分位）
export const formatNumber = (num) => {
  if (num === null || num === undefined) return ''
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化文件大小
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 防抖
export const debounce = (fn, delay = 300) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 节流
export const throttle = (fn, delay = 300) => {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

// 深拷贝
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj)
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (obj instanceof Object) {
    const copyObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        copyObj[key] = deepClone(obj[key])
      }
    }
    return copyObj
  }
}

// 生成随机 ID
export const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 下载文件
export const downloadFile = (url, filename) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename || ''
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

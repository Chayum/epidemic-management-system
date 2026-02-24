import request from '@/utils/request'

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

// 获取物资统计数据
export const getMaterialStats = () => {
  return request({
    url: '/stats/material',
    method: 'get'
  })
}

// 获取申请统计数据
export const getApplicationStats = () => {
  return request({
    url: '/stats/application',
    method: 'get'
  })
}

// 获取捐赠统计数据
export const getDonationStats = () => {
  return request({
    url: '/stats/donation',
    method: 'get'
  })
}

// 获取趋势数据
export const getTrendData = (params) => {
  return request({
    url: '/stats/trend',
    method: 'get',
    params
  })
}

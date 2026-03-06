import request from '@/utils/request'

/**
 * 获取实时数据大屏完整数据
 */
export function getDashboardData() {
  return request({
    url: '/stats/dashboard/full',
    method: 'get'
  })
}

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

/**
 * 获取趋势数据
 */
export function getTrendData(period = 'week') {
  return request({
    url: '/stats/trend',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取物资统计数据
 */
export function getMaterialStats() {
  return request({
    url: '/stats/material',
    method: 'get'
  })
}

/**
 * 获取预警列表
 */
export function getWarningList() {
  return request({
    url: '/stats/warning',
    method: 'get'
  })
}

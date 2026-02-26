import request from '@/utils/request'

/**
 * 获取仪表盘统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getDashboardStats = () => {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

/**
 * 获取物资统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getMaterialStats = () => {
  return request({
    url: '/stats/material',
    method: 'get'
  })
}

/**
 * 获取申请统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getApplicationStats = () => {
  return request({
    url: '/stats/application',
    method: 'get'
  })
}

/**
 * 获取捐赠统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getDonationStats = () => {
  return request({
    url: '/stats/donation',
    method: 'get'
  })
}

/**
 * 获取趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.type - 趋势类型（inbound/outbound/application/donation）
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 * @returns {Promise} Axios Promise对象
 */
export const getTrendData = (params) => {
  return request({
    url: '/stats/trend',
    method: 'get',
    params
  })
}

/**
 * 获取用户个人统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getUserStats = () => {
  return request({
    url: '/stats/user',
    method: 'get'
  })
}

/**
 * 获取库存预警列表
 * @returns {Promise} Axios Promise对象
 */
export const getWarningList = () => {
  return request({
    url: '/stats/warning',
    method: 'get'
  })
}

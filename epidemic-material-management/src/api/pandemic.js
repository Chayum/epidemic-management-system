import request from '@/utils/request'

/**
 * 获取疫情新闻列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.status] - 新闻状态
 * @returns {Promise} Axios Promise对象
 */
export const getNewsList = (params) => {
  return request({
    url: '/pandemic/news',
    method: 'get',
    params
  })
}

/**
 * 获取新闻详情
 * @param {string} id - 新闻ID
 * @returns {Promise} Axios Promise对象
 */
export const getNewsDetail = (id) => {
  return request({
    url: `/pandemic/news/${id}`,
    method: 'get'
  })
}

/**
 * 发布新闻
 * @param {Object} data - 新闻数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} data.source - 来源
 * @returns {Promise} Axios Promise对象
 */
export const publishNews = (data) => {
  return request({
    url: '/pandemic/news',
    method: 'post',
    data
  })
}

/**
 * 删除新闻
 * @param {string} id - 新闻ID
 * @returns {Promise} Axios Promise对象
 */
export const deleteNews = (id) => {
  return request({
    url: `/pandemic/news/${id}`,
    method: 'delete'
  })
}

/**
 * 获取实时疫情数据
 * @returns {Promise} Axios Promise对象
 */
export const getPandemicData = () => {
  return request({
    url: '/pandemic/data',
    method: 'get'
  })
}

/**
 * 获取推送统计
 * @returns {Promise} Axios Promise对象
 */
export const getPushStats = () => {
  return request({
    url: '/pandemic/push/stats',
    method: 'get'
  })
}

/**
 * 获取推送记录
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} params.status - 推送状态（可选）
 * @returns {Promise} Axios Promise对象
 */
export const getPushList = (params) => {
  return request({
    url: '/pandemic/push/list',
    method: 'get',
    params
  })
}

/**
 * 发送推送
 * @param {Object} data - 推送数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {Array} data.targets - 目标用户列表
 * @returns {Promise} Axios Promise对象
 */
export const sendPush = (data) => {
  return request({
    url: '/pandemic/push',
    method: 'post',
    data
  })
}

/**
 * 获取用户角色分布统计
 * @returns {Promise} Axios Promise对象
 */
export const getUserRoleStats = () => {
  return request({
    url: '/pandemic/push/role-stats',
    method: 'get'
  })
}

/**
 * 删除推送记录
 * @ {number} id - 推送记录ID
 * @returns {Promise} Axios Promise对象
 */
export const deletePushRecord = (id) => {
  return request({
    url: `/pandemic/push/${id}`,
    method: 'delete'
  })
}

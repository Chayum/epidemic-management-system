import request from '@/utils/request'

/**
 * 获取疫情新闻列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @returns {Promise} Axios Promise对象
 */
export const getNewsList = (params) => {
  return request({
    url: '/stats/news/list',
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
    url: `/stats/news/${id}`,
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
 * 获取防疫知识列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @returns {Promise} Axios Promise对象
 */
export const getKnowledgeList = (params) => {
  return request({
    url: '/pandemic/knowledge',
    method: 'get',
    params
  })
}

/**
 * 获取知识详情
 * @param {string} id - 知识ID
 * @returns {Promise} Axios Promise对象
 */
export const getKnowledgeDetail = (id) => {
  return request({
    url: `/pandemic/knowledge/${id}`,
    method: 'get'
  })
}

/**
 * 发布知识
 * @param {Object} data - 知识数据
 * @param {string} data.title - 标题
 * @param {string} data.category - 分类
 * @param {string} data.content - 内容
 * @returns {Promise} Axios Promise对象
 */
export const publishKnowledge = (data) => {
  return request({
    url: '/pandemic/knowledge',
    method: 'post',
    data
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
 * @returns {Promise} Axios Promise对象
 */
export const getPushList = () => {
  return request({
    url: '/pandemic/push/list',
    method: 'get'
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

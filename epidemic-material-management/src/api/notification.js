import request from '@/utils/request'

/**
 * 获取我的通知列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {number} [params.isRead] - 是否已读（0-未读，1-已读）
 * @returns {Promise} Axios Promise对象
 */
export const getMyNotifications = (params) => {
  return request({
    url: '/notification/user',
    method: 'get',
    params
  })
}

/**
 * 获取未读通知数量
 * @returns {Promise} Axios Promise对象
 */
export const getUnreadCount = () => {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

/**
 * 标记通知为已读
 * @param {number} id - 通知ID
 * @returns {Promise} Axios Promise对象
 */
export const markAsRead = (id) => {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记所有通知为已读
 * @returns {Promise} Axios Promise对象
 */
export const markAllAsRead = () => {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

/**
 * 删除通知
 * @ {number} id - 通知ID
 * @returns {Promise} Axios Promise对象
 */
export const deleteNotification = (id) => {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}
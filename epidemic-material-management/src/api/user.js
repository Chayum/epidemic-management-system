import request from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.username] - 用户名
 * @param {string} [params.name] - 姓名
 * @param {string} [params.phone] - 电话
 * @param {string} [params.role] - 角色
 * @param {string} [params.status] - 状态
 * @returns {Promise} Axios Promise对象
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取用户信息
 * @param {string|number} id - 用户ID
 * @returns {Promise} Axios Promise对象
 */
export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

/**
 * 新增用户
 * @param {Object} data - 用户数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.name - 姓名
 * @param {string} data.role - 角色
 * @returns {Promise} Axios Promise对象
 */
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户数据
 * @param {string|number} data.id - 用户ID
 * @param {string} [data.name] - 姓名
 * @param {string} [data.phone] - 电话
 * @param {string} [data.email] - 邮箱
 * @returns {Promise} Axios Promise对象
 */
export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {string|number} id - 用户ID
 * @returns {Promise} Axios Promise对象
 */
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

/**
 * 更新用户状态
 * @param {string|number} id - 用户ID
 * @param {string} status - 新状态
 * @returns {Promise} Axios Promise对象
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}

/**
 * 批量更新用户状态
 * @param {Array} ids - 用户ID列表
 * @param {string} status - 新状态
 * @returns {Promise} Axios Promise对象
 */
export function batchUpdateStatus(ids, status) {
  return request({
    url: '/user/batch/status',
    method: 'put',
    data: { ids, status }
  })
}

/**
 * 批量删除用户
 * @param {Array} ids - 用户ID列表
 * @returns {Promise} Axios Promise对象
 */
export function batchDelete(ids) {
  return request({
    url: '/user/batch',
    method: 'delete',
    data: { ids }
  })
}

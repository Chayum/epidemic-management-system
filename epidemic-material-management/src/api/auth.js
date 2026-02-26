import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} [data.role] - 登录角色
 * @returns {Promise} Axios Promise对象
 */
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise} Axios Promise对象
 */
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} Axios Promise对象
 */
export const getUserInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码修改数据
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} Axios Promise对象
 */
export const changePassword = (data) => {
  return request({
    url: '/auth/password',
    method: 'put',
    data
  })
}

/**
 * 更新个人信息
 * @param {Object} data - 个人信息数据
 * @param {string} [data.name] - 姓名
 * @param {string} [data.phone] - 电话
 * @param {string} [data.email] - 邮箱
 * @returns {Promise} Axios Promise对象
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/auth/profile',
    method: 'put',
    data
  })
}

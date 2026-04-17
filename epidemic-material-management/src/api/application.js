import request from '@/utils/request'

/**
 * 获取申请列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.status] - 申请状态
 * @param {string} [params.applicantName] - 申请人姓名
 * @returns {Promise} Axios Promise对象
 */
export const getApplicationList = (params) => {
  return request({
    url: '/application/list',
    method: 'get',
    params
  })
}

/**
 * 获取申请详情
 * @param {string} id - 申请ID
 * @returns {Promise} Axios Promise对象
 */
export const getApplicationDetail = (id) => {
  return request({
    url: `/application/${id}`,
    method: 'get'
  })
}

/**
 * 提交物资申请
 * @param {Object} data - 申请数据
 * @param {Array} data.items - 申请物资项列表
 * @param {string} data.reason - 申请理由
 * @param {string} data.urgency - 紧急程度
 * @returns {Promise} Axios Promise对象
 */
export const submitApplication = (data) => {
  return request({
    url: '/application',
    method: 'post',
    data
  })
}

/**
 * 审核申请
 * @param {Object} data - 审核数据
 * @param {string} data.applicationId - 申请ID
 * @param {string} data.status - 审批状态（"approved" 或 "rejected"）
 * @param {string} [data.remark] - 审核意见
 * @returns {Promise} Axios Promise对象
 */
export const approveApplication = (data) => {
  return request({
    url: '/application/approve',
    method: 'post',
    data
  })
}

/**
 * 取消申请
 * @param {string} id - 申请ID
 * @returns {Promise} Axios Promise对象
 */
export const cancelApplication = (id) => {
  return request({
    url: `/application/${id}/cancel`,
    method: 'post'
  })
}

/**
 * 获取我的申请列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.status] - 申请状态
 * @returns {Promise} Axios Promise对象
 */
export const getMyApplications = (params) => {
  return request({
    url: '/application/my',
    method: 'get',
    params
  })
}

/**
 * 获取物流追踪信息
 * @param {string} id - 申请ID
 * @returns {Promise} Axios Promise对象
 */
export const getTrackInfo = (id) => {
  return request({
    url: `/application/${id}/track`,
    method: 'get'
  })
}

/**
 * 确认收货
 * @param {string} id - 申请ID
 * @returns {Promise} Axios Promise对象
 */
export const confirmReceive = (id) => {
  return request({
    url: `/application/${id}/confirm`,
    method: 'post'
  })
}

/**
 * 更新申请状态（管理员）
 * 用于管理员手动更新物流状态：approved -> delivered -> received
 * @param {string} id - 申请ID
 * @param {string} status - 新状态（delivered/received）
 * @returns {Promise} Axios Promise对象
 */
export const updateApplicationStatus = (id, status) => {
  return request({
    url: `/application/${id}/status`,
    method: 'put',
    params: { status }
  })
}

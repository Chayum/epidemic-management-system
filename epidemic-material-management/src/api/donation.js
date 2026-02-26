import request from '@/utils/request'

/**
 * 获取捐赠列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.status] - 状态
 * @param {string} [params.donorUnit] - 捐赠单位
 * @param {number} [params.donorId] - 捐赠人ID
 * @param {string} [params.type] - 捐赠类型
 * @param {string} [params.id] - 捐赠ID
 * @returns {Promise} Axios Promise对象
 */
export const getDonationList = (params) => {
  return request({
    url: '/donation/list',
    method: 'get',
    params
  })
}

/**
 * 获取捐赠详情
 * @param {string} id - 捐赠ID
 * @returns {Promise} Axios Promise对象
 */
export const getDonationDetail = (id) => {
  return request({
    url: `/donation/${id}`,
    method: 'get'
  })
}

/**
 * 提交捐赠申请
 * @param {Object} data - 捐赠数据
 * @param {Array} data.items - 捐赠物资列表
 * @param {string} [data.donorUnit] - 捐赠单位
 * @param {string} [data.contactName] - 联系人
 * @param {string} [data.contactPhone] - 联系电话
 * @param {string} [data.remark] - 备注
 * @returns {Promise} Axios Promise对象
 */
export const submitDonation = (data) => {
  return request({
    url: '/donation',
    method: 'post',
    data
  })
}

/**
 * 审核捐赠
 * @param {Object} data - 审核数据
 * @param {string} data.donationId - 捐赠ID
 * @param {boolean} data.approved - 是否通过
 * @param {string} [data.comment] - 审核意见
 * @returns {Promise} Axios Promise对象
 */
export const approveDonation = (data) => {
  return request({
    url: '/donation/approve',
    method: 'post',
    data
  })
}

/**
 * 获取我的捐赠记录
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.status] - 状态
 * @returns {Promise} Axios Promise对象
 */
export const getMyDonations = (params) => {
  return request({
    url: '/donation/my',
    method: 'get',
    params
  })
}

/**
 * 获取捐赠统计数据
 * @returns {Promise} Axios Promise对象
 */
export const getDonationStats = () => {
  return request({
    url: '/donation/stats',
    method: 'get'
  })
}

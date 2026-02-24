import request from '@/utils/request'

// 获取捐赠列表
export const getDonationList = (params) => {
  return request({
    url: '/donation/list',
    method: 'get',
    params
  })
}

// 获取捐赠详情
export const getDonationDetail = (id) => {
  return request({
    url: `/donation/${id}`,
    method: 'get'
  })
}

// 提交捐赠申请
export const submitDonation = (data) => {
  return request({
    url: '/donation',
    method: 'post',
    data
  })
}

// 审核捐赠
export const approveDonation = (data) => {
  return request({
    url: '/donation/approve',
    method: 'post',
    data
  })
}

// 获取我的捐赠
export const getMyDonations = (params) => {
  return request({
    url: '/donation/my',
    method: 'get',
    params
  })
}

// 获取捐赠统计
export const getDonationStats = () => {
  return request({
    url: '/donation/stats',
    method: 'get'
  })
}

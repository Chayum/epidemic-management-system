import request from '@/utils/request'

// 获取申请列表
export const getApplicationList = (params) => {
  return request({
    url: '/application/list',
    method: 'get',
    params
  })
}

// 获取申请详情
export const getApplicationDetail = (id) => {
  return request({
    url: `/application/${id}`,
    method: 'get'
  })
}

// 提交物资申请
export const submitApplication = (data) => {
  return request({
    url: '/application',
    method: 'post',
    data
  })
}

// 审核申请
export const approveApplication = (data) => {
  return request({
    url: '/application/approve',
    method: 'post',
    data
  })
}

// 取消申请
export const cancelApplication = (id) => {
  return request({
    url: `/application/${id}/cancel`,
    method: 'post'
  })
}

// 获取我的申请
export const getMyApplications = (params) => {
  return request({
    url: '/application/my',
    method: 'get',
    params
  })
}

// 获取物流追踪信息
export const getTrackInfo = (id) => {
  return request({
    url: `/application/${id}/track`,
    method: 'get'
  })
}

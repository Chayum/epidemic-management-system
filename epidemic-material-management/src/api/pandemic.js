import request from '@/utils/request'

// 获取疫情新闻列表
export const getNewsList = (params) => {
  return request({
    url: '/stats/news/list',
    method: 'get',
    params
  })
}

// 获取新闻详情
export const getNewsDetail = (id) => {
  return request({
    url: `/stats/news/${id}`,
    method: 'get'
  })
}

// 发布新闻
export const publishNews = (data) => {
  return request({
    url: '/pandemic/news',
    method: 'post',
    data
  })
}

// 删除新闻
export const deleteNews = (id) => {
  return request({
    url: `/pandemic/news/${id}`,
    method: 'delete'
  })
}

// 获取防疫知识列表
export const getKnowledgeList = (params) => {
  return request({
    url: '/pandemic/knowledge',
    method: 'get',
    params
  })
}

// 获取知识详情
export const getKnowledgeDetail = (id) => {
  return request({
    url: `/pandemic/knowledge/${id}`,
    method: 'get'
  })
}

// 发布知识
export const publishKnowledge = (data) => {
  return request({
    url: '/pandemic/knowledge',
    method: 'post',
    data
  })
}

// 获取实时疫情数据
export const getPandemicData = () => {
  return request({
    url: '/pandemic/data',
    method: 'get'
  })
}

// 获取推送统计
export const getPushStats = () => {
  return request({
    url: '/pandemic/push/stats',
    method: 'get'
  })
}

// 获取推送记录
export const getPushList = () => {
  return request({
    url: '/pandemic/push/list',
    method: 'get'
  })
}

// 发送推送
export const sendPush = (data) => {
  return request({
    url: '/pandemic/push',
    method: 'post',
    data
  })
}

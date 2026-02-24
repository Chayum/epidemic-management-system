import request from '@/utils/request'

// 获取疫情新闻列表
export const getNewsList = (params) => {
  return request({
    url: '/pandemic/news',
    method: 'get',
    params
  })
}

// 获取新闻详情
export const getNewsDetail = (id) => {
  return request({
    url: `/pandemic/news/${id}`,
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

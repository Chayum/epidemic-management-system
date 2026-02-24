import request from '@/utils/request'

// 获取物资列表
export const getMaterialList = (params) => {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

// 获取物资详情
export const getMaterialDetail = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

// 新增物资
export const addMaterial = (data) => {
  return request({
    url: '/material',
    method: 'post',
    data
  })
}

// 更新物资
export const updateMaterial = (data) => {
  return request({
    url: '/material',
    method: 'put',
    data
  })
}

// 删除物资
export const deleteMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'delete'
  })
}

// 获取库存列表
export const getInventoryList = (params) => {
  return request({
    url: '/inventory/list',
    method: 'get',
    params
  })
}

// 获取库存预警列表
export const getWarningList = () => {
  return request({
    url: '/inventory/warning',
    method: 'get'
  })
}

// 盘点库存
export const checkInventory = (data) => {
  return request({
    url: '/inventory/check',
    method: 'post',
    data
  })
}

import request from '@/utils/request'

/**
 * 获取物资列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.name] - 物资名称
 * @param {string} [params.type] - 物资类型
 * @param {string} [params.status] - 物资状态
 * @returns {Promise} Axios Promise对象
 */
export const getMaterialList = (params) => {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

/**
 * 获取物资详情
 * @param {string} id - 物资ID
 * @returns {Promise} Axios Promise对象
 */
export const getMaterialDetail = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

/**
 * 新增物资
 * @param {Object} data - 物资数据
 * @param {string} data.name - 物资名称
 * @param {string} data.type - 物资类型
 * @param {string} data.unit - 单位
 * @param {number} data.stock - 库存数量
 * @returns {Promise} Axios Promise对象
 */
export const addMaterial = (data) => {
  return request({
    url: '/material',
    method: 'post',
    data
  })
}

/**
 * 更新物资
 * @param {Object} data - 物资数据
 * @param {string} data.id - 物资ID
 * @param {string} [data.name] - 物资名称
 * @param {string} [data.type] - 物资类型
 * @param {number} [data.stock] - 库存数量
 * @returns {Promise} Axios Promise对象
 */
export const updateMaterial = (data) => {
  return request({
    url: '/material',
    method: 'put',
    data
  })
}

/**
 * 删除物资
 * @param {string} id - 物资ID
 * @returns {Promise} Axios Promise对象
 */
export const deleteMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'delete'
  })
}

/**
 * 获取库存列表
 * @param {Object} params - 查询参数
 * @returns {Promise} Axios Promise对象
 */
export const getInventoryList = (params) => {
  return request({
    url: '/inventory/list',
    method: 'get',
    params
  })
}

/**
 * 获取库存预警列表
 * @returns {Promise} Axios Promise对象
 */
export const getWarningList = () => {
  return request({
    url: '/inventory/warning',
    method: 'get'
  })
}

/**
 * 盘点库存
 * @param {Object} data - 盘点数据
 * @param {string} data.materialId - 物资ID
 * @param {number} data.actualStock - 实际库存
 * @returns {Promise} Axios Promise对象
 */
export const checkInventory = (data) => {
  return request({
    url: '/inventory/check',
    method: 'post',
    data
  })
}

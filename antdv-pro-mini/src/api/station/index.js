import { useGet, usePost } from '~/utils/request'

/**
 * 站点列表
 * @param {*} data 
 * @returns 
 */
export const list = (data) => {
  return useGet('/station/page', data)
}

/**
 * 根据列车ID获取站点
 * @param {*} trainId 
 * @returns 
 */
export const getByTrainId = (trainId) => {
  return useGet('/station/getByTrainId', { trainId })
}

/**
 * 获取站点详情
 * @param {*} id 
 * @returns 
 */
export const getStationById = (id) => {
  return useGet('/station/getById', { id })
}

/**
 * 新增编辑站点
 * @param {*} data 
 * @returns 
 */
export const save = (data) => {
  return usePost('/station/save', data)
}

/**
 * 批量保存站点
 * @param {*} data 
 * @returns 
 */
export const saveBatch = (data) => {
  return usePost('/station/saveBatch', data)
}

/**
 * 删除站点
 * @param {*} data 
 * @returns 
 */
export const del = (data) => {
  return usePost('/station/delete', data)
} 
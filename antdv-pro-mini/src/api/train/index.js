import { useGet, usePost } from '~/utils/request'

/**
 * 列车列表
 * @param {*} data 
 * @returns 
 */
export const list = (data) => {
  return useGet('/train/page', data)
}

/**
 * 获取所有列车
 * @returns 
 */
export const getAllTrains = () => {
  return useGet('/train/list')
}

/**
 * 获取列车详情
 * @param {*} id 
 * @returns 
 */
export const getTrainById = (id) => {
  return useGet('/train/getById', { id })
}

/**
 * 获取列车及其站点
 * @param {*} id 
 * @returns 
 */
export const getTrainWithStations = (id) => {
  return useGet('/train/getWithStations', { id })
}

/**
 * 新增编辑列车
 * @param {*} data 
 * @returns 
 */
export const save = (data) => {
  return usePost('/train/save', data)
}

/**
 * 删除列车
 * @param {*} data 
 * @returns 
 */
export const del = (data) => {
  return usePost('/train/delete', data)
}

/**
 * 使用AI生成列车路线
 * @returns 
 */
export const generateTrainRoute = () => {
  return usePost('/train/generate')
}

/**
 * 使用AI生成指定起点和终点的列车路线
 * @param {*} start 起始站点
 * @param {*} end 终点站点
 * @returns 
 */
export const generateTrainRouteWithLocations = (start, end) => {
  const data = {
    start: start || '',
    end: end || ''
  };
  console.log('发送参数:', data);
  return usePost('/train/generateWithLocations', data);
} 
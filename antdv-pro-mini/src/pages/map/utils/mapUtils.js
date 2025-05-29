/**
 * 地图工具类
 */

/**
 * 计算两点之间的距离（公里）
 * @param {Array} point1 点1坐标 [lng, lat]
 * @param {Array} point2 点2坐标 [lng, lat]
 * @returns {Number} 距离，单位公里
 */
export function calculateDistance(point1, point2) {
  const rad = Math.PI / 180;
  const lat1 = point1[1] * rad;
  const lat2 = point2[1] * rad;
  const lng1 = point1[0] * rad;
  const lng2 = point2[0] * rad;
  
  const a = Math.sin(lat1) * Math.sin(lat2) +
            Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);
  const earth_radius = 6378.137; // 地球半径，单位km
  const distance = earth_radius * Math.acos(Math.min(a, 1));
  
  return distance;
}

/**
 * 计算线段上的点
 * @param {Array} start 起点坐标 [lng, lat]
 * @param {Array} end 终点坐标 [lng, lat]
 * @param {Number} ratio 比例 0-1
 * @returns {Array} 计算出的点坐标 [lng, lat]
 */
export function calculatePointOnLine(start, end, ratio) {
  const lng = start[0] + (end[0] - start[0]) * ratio;
  const lat = start[1] + (end[1] - start[1]) * ratio;
  return [lng, lat];
}

/**
 * 计算两点间的角度（用于图标旋转）
 * @param {Array} start 起点坐标 [lng, lat]
 * @param {Array} end 终点坐标 [lng, lat]
 * @returns {Number} 角度（度）
 */
export function calculateAngle(start, end) {
  return Math.atan2(end[1] - start[1], end[0] - start[0]) * 180 / Math.PI;
}

/**
 * 解析时间字符串（HH:MM）转为分钟数
 * @param {String} timeStr 时间字符串，如 "08:30"
 * @returns {Number} 分钟数
 */
export function parseTimeToMinutes(timeStr) {
  if (!timeStr) return 0;
  const [hours, minutes] = timeStr.split(':').map(Number);
  return hours * 60 + minutes;
}

/**
 * 格式化分钟数为时间字符串
 * @param {Number} minutes 分钟数
 * @returns {String} 时间字符串，如 "08:30"
 */
export function formatMinutesToTime(minutes) {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
}

/**
 * 创建列车路径
 * @param {Array} stations 站点数组
 * @returns {Array} 路径点数组 [[lng, lat], [lng, lat], ...]
 */
export function createTrainPath(stations) {
  // 基本路径只连接站点
  const basePath = stations.map(station => station.coordinate);
  
  // 如果需要平滑的路径，可以在站点之间插值更多点
  const smoothPath = [];
  for (let i = 0; i < basePath.length - 1; i++) {
    smoothPath.push(basePath[i]);
    
    // 在两站之间插入额外的点使路径更平滑
    const start = basePath[i];
    const end = basePath[i + 1];
    
    // 根据距离决定插入点的数量
    const distance = calculateDistance(start, end);
    const pointsToInsert = Math.floor(distance / 30); // 每30公里插入一个点
    
    for (let j = 1; j <= pointsToInsert; j++) {
      const ratio = j / (pointsToInsert + 1);
      smoothPath.push(calculatePointOnLine(start, end, ratio));
    }
  }
  
  // 添加终点
  if (basePath.length > 0) {
    smoothPath.push(basePath[basePath.length - 1]);
  }
  
  return smoothPath;
}

/**
 * 生成随机列车图标颜色
 * @returns {String} 颜色代码
 */
export function generateRandomTrainColor() {
  const colors = [
    '#1890ff', // 蓝色
    '#52c41a', // 绿色
    '#fa8c16', // 橙色
    '#722ed1', // 紫色
    '#eb2f96', // 粉色
    '#faad14'  // 黄色
  ];
  
  return colors[Math.floor(Math.random() * colors.length)];
} 
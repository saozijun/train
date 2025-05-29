<template>
  <div class="train-tracking-container">
    <a-card class="train-info-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <h2>列车监控</h2>
          <a-space>
            <a-select v-model:value="selectedTrain" style="width: 200px" placeholder="选择车次" @change="handleTrainChange">
              <a-select-option v-for="train in trains" :key="train.id" :value="train.id">
                {{ train.code }} ({{ train.fromStation }} - {{ train.toStation }})
              </a-select-option>
            </a-select>
            <a-select v-model:value="timeMultiplier" style="width: 120px" @change="handleSpeedChange">
              <a-select-option :value="50">50x</a-select-option>
              <a-select-option :value="100">100x</a-select-option>
              <a-select-option :value="200">200x</a-select-option>
              <a-select-option :value="500">500x</a-select-option>
            </a-select>
            <a-button type="primary" @click="startSimulation" :disabled="isSimulating">
              <template #icon><play-circle-outlined /></template>
              开始发车监控
            </a-button>
            <a-button @click="stopSimulation" :disabled="!isSimulating">
              <template #icon><pause-circle-outlined /></template>
              停止发车监控
            </a-button>
            <a-tooltip title="重置视图">
              <a-button @click="resetMapView">
                <template #icon><eye-outlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip :title="isTrainLocked ? '解锁列车视图' : '锁定列车视图'">
              <a-button @click="toggleTrainLock" type="dashed" :class="{ 'active-lock': isTrainLocked }">
                <template #icon>
                  <lock-outlined v-if="isTrainLocked" />
                  <unlock-outlined v-else />
                </template>
              </a-button>
            </a-tooltip>
          </a-space>
        </div>
      </template>

      <a-row :gutter="16">
        <a-col :span="18">
          <div class="map-container" ref="mapContainer"></div>
        </a-col>
        <a-col :span="6">
          <div class="info-panel">
            <a-tabs class="custom-tabs">
              <a-tab-pane key="schedule" tab="运行时刻表">
                <div class="schedule-table">
                  <a-table 
                    :columns="scheduleColumns" 
                    :data-source="currentStations" 
                    :pagination="false" 
                    size="small"
                    :scroll="{ y: 300 }"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'status'">
                        <a-tag :color="getStationStatusColor(record)">{{ getStationStatusText(record) }}</a-tag>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>
              <a-tab-pane key="info" tab="列车信息">
                <div class="train-detail-info">
                  <a-descriptions :column="1" bordered size="small">
                    <a-descriptions-item label="车次编号">{{ currentTrain?.code || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="始发站">{{ currentTrain?.fromStation || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="终点站">{{ currentTrain?.toStation || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="列车类型">{{ currentTrain?.type || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="总里程">{{ currentTrain?.distance || '-' }} 公里</a-descriptions-item>
                    <a-descriptions-item label="运行时间">{{ formatDuration(currentTrain?.duration) || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="最高速度">{{ currentTrain?.maxSpeed || '-' }} km/h</a-descriptions-item>
                  </a-descriptions>
                </div>
              </a-tab-pane>
            </a-tabs>
            
            <a-card 
              v-if="isSimulating" 
              title="实时状态" 
              class="status-card" 
              :bordered="false" 
              size="small"
            >
              <a-row :gutter="[16, 16]">
                <a-col :span="12">
                  <a-statistic 
                    title="当前站点" 
                    :value="currentStatus.station || '-'"
                    :value-style="{ fontSize: '16px' }"
                  />
                </a-col>
                <a-col :span="12">
                  <a-statistic 
                    title="当前速度" 
                    :value="`${currentStatus.speed || 0} km/h`"
                    :value-style="{ fontSize: '16px' }"
                  />
                </a-col>
                <a-col :span="12">
                  <a-statistic 
                    title="下一站" 
                    :value="currentStatus.nextStation || '-'"
                    :value-style="{ fontSize: '16px' }"
                  />
                </a-col>
                <a-col :span="12">
                  <a-statistic 
                    title="预计到达" 
                    :value="currentStatus.eta || '-'"
                    :value-style="{ fontSize: '16px' }"
                  />
                </a-col>
              </a-row>
              <a-progress
                :percent="currentStatus.progress"
                :status="isSimulating ? 'active' : 'normal'"
                :stroke-color="{
                  '0%': '#108ee9',
                  '100%': '#87d068',
                }"
                class="progress-bar"
              />
            </a-card>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, h } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
import { message } from 'ant-design-vue';
import { 
  EyeOutlined, 
  PlayCircleOutlined, 
  PauseCircleOutlined,
  LockOutlined,
  UnlockOutlined
} from '@ant-design/icons-vue';

import trainPng from '~/assets/images/t1.png';

// 导入自定义组件和工具
import StationMarker from './components/StationMarker.vue';
import TrainMarker from './components/TrainMarker.vue';
import { 
  calculateDistance, 
  calculatePointOnLine, 
  calculateAngle, 
  parseTimeToMinutes, 
  formatMinutesToTime,
  createTrainPath,
  generateRandomTrainColor
} from './utils/mapUtils';

// 导入API
import { getAllTrains } from '~/api/train';
import { getByTrainId } from '~/api/station';

// 地图实例
const map = ref(null);
const mapContainer = ref(null);
const AMap = ref(null);
const trainMarker = ref(null);
const polyLine = ref(null);
const stationMarkers = ref([]);
const isTrainLocked = ref(false); // 是否锁定列车视图

// 监控状态
const isSimulating = ref(false);
const simulationTimer = ref(null);
const simulationStartTime = ref(0);
const simulationElapsedTime = ref(0);
const selectedTrain = ref(null);
const currentStationIndex = ref(-1);
const lastPosition = ref(null); // 上一次的位置
const moveAnimation = ref(null); // 移动动画

// 当前状态
const currentStatus = ref({
  station: '-',
  speed: 0,
  nextStation: '-',
  eta: '-',
  progress: 0
});

// 列车数据
const trains = ref([]);

// 计算当前选中的列车
const currentTrain = computed(() => {
  return trains.value.find(train => train.id === selectedTrain.value) || null;
});

// 计算当前的站点列表
const currentStations = computed(() => {
  if (!currentTrain.value) return [];
  return currentTrain.value.stations.map((station, index) => {
    return {
      ...station,
      key: index,
      status: getStationStatus(index)
    };
  });
});

// 时刻表列
const scheduleColumns = [
  {
    title: '站点',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '到达',
    dataIndex: 'arriveTime',
    key: 'arriveTime',
    customRender: ({ text }) => text || '-'
  },
  {
    title: '发车',
    dataIndex: 'departTime',
    key: 'departTime',
    customRender: ({ text }) => text || '-'
  },
  {
    title: '状态',
    key: 'status',
  }
];

// 在 script setup 顶部添加新的 ref
const passedPolyLine = ref(null);
const currentSpeed = ref(0);
const targetSpeed = ref(0);
const speedUpdateInterval = ref(null);

// 在 script setup 顶部添加时间倍率控制
const timeMultiplier = ref(100); // 100倍速

// 初始化地图
onMounted(async () => {
  try {
    AMap.value = await AMapLoader.load({
      key: '4a3d90ff3f6b025a96940716e5c29eb6',
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.MapType', 'AMap.HawkEye', 'AMap.MoveAnimation']
    });
    
    // 创建地图实例
    map.value = new AMap.value.Map(mapContainer.value, {
      viewMode: '3D',
      zoom: 5,
      center: [116.397428, 39.90923],
      mapStyle: 'amap://styles/whitesmoke',
      features: ['bg', 'road', 'building', 'point']
    });
    
    // 添加地图控件
    map.value.addControl(new AMap.value.Scale());
    map.value.addControl(new AMap.value.ToolBar({
      position: 'RB',
      liteStyle: true
    }));
    map.value.addControl(new AMap.value.MapType({
      defaultType: 0,
      position: 'RT'
    }));
    
    // 加载列车数据
    await loadTrains();
    
    // 默认选择第一个列车
    if (trains.value.length > 0) {
      selectedTrain.value = trains.value[0].id;
      handleTrainChange(selectedTrain.value);
    }
    
  } catch (e) {
    message.error('地图加载失败，请检查网络');
    console.error('地图初始化失败:', e);
  }
});

// 加载列车数据
async function loadTrains() {
  try {
    const { data } = await getAllTrains();
    if (data && data.length > 0) {
      // 为每个列车加载站点数据
      for (const train of data) {
        const stationsResult = await getByTrainId(train.id);
        const stations = stationsResult.data || [];
        
        // 将站点数据转换为地图需要的格式
        const formattedStations = stations.map(station => ({
          name: station.name,
          time: station.departTime || station.arriveTime,
          arriveTime: station.arriveTime,
          departTime: station.departTime,
          stop: station.stop || 0,
          distance: station.distance || 0,
          coordinate: [station.longitude, station.latitude]
        }));
        
        // 按照序号排序
        formattedStations.sort((a, b) => a.sequence - b.sequence);
        
        // 添加站点数据到列车对象
        train.stations = formattedStations;
        
        // 确保有颜色属性
        if (!train.color) {
          train.color = generateRandomTrainColor();
        }
      }
      
      trains.value = data;
    }
  } catch (error) {
    console.error('加载列车数据失败:', error);
    message.error('加载列车数据失败');
  }
}

onUnmounted(() => {
  stopSimulation();
  if (map.value) {
    map.value.destroy();
  }
});

// 创建列车图标
function createTrainMarker(train) {
  // 创建列车标记的HTML内容
  const trainContent = `
    <div class="train-marker" style="
      position: relative;
      width: 32px;
      height: 32px;
      transform: rotate(0deg);
      transition: transform 0.3s ease;
    ">
      <img src="${trainPng}" style="
        width: 100%;
        height: 100%;
        object-fit: contain;
      " />
      <div class="train-label" style="
        position: absolute;
        top: -25px;
        left: 50%;
        transform: translateX(-50%);
        background: ${train.color};
        color: white;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
        white-space: nowrap;
      ">${train.code}</div>
    </div>
  `;

  // 创建标记
  const marker = new AMap.value.Marker({
    position: train.stations[0].coordinate,
    content: trainContent,
    offset: new AMap.value.Pixel(-16, -16),
    zIndex: 200,
    autoRotation: true
  });

  // 添加点击事件
  let infoWindow = null;
  marker.on('click', () => {
    if (infoWindow) {
      infoWindow.close();
      infoWindow = null;
      return;
    }

    infoWindow = new AMap.value.InfoWindow({
      isCustom: true,
      content: `
        <div class="train-info-window" style="
          background: white;
          padding: 12px;
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          min-width: 200px;
        ">
          <div class="train-info-header" style="
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            padding-bottom: 8px;
            border-bottom: 1px solid #eee;
          ">
            <h4 style="margin: 0; font-weight: bold; color: #1890ff;">${train.code}</h4>
            <span class="train-type" style="
              color: white;
              padding: 2px 6px;
              border-radius: 10px;
              font-size: 12px;
              background: ${train.color};
            ">${train.type}</span>
          </div>
          <div class="train-info-body" style="
            p {
              margin: 6px 0;
              font-size: 13px;
            }
            span {
              font-weight: 500;
              margin-right: 5px;
            }
          ">
            <p><span>始发站:</span> ${train.fromStation}</p>
            <p><span>终点站:</span> ${train.toStation}</p>
            <p><span>当前速度:</span> ${currentStatus.value.speed} km/h</p>
            <p><span>下一站:</span> ${currentStatus.value.nextStation}</p>
            <p><span>预计到达:</span> ${currentStatus.value.eta}</p>
          </div>
        </div>
      `,
      offset: new AMap.value.Pixel(0, -15),
      closeWhenClickMap: true
    });
    
    infoWindow.open(map.value, marker.getPosition());
  });

  return marker;
}

// 切换列车锁定状态
function toggleTrainLock() {
  isTrainLocked.value = !isTrainLocked.value;
  
  if (isTrainLocked.value) {
    message.success('已锁定列车视图，地图将跟随列车移动');
    
    // 如果当前正在模拟中，立即将地图中心设为列车位置并放大地图
    if (isSimulating.value && trainMarker.value) {
      map.value.setCenter(trainMarker.value.getPosition());
      map.value.setZoom(14); // 放大到14级，提供更详细的视图
    }
  } else {
    message.info('已解锁列车视图');
    // 恢复到较远的视图，可以看到更多站点
    // resetMapView();
  }
}

// 更新列车位置和方向
function updateTrainPosition() {
  if (!currentTrain.value || !trainMarker.value || !map.value) return;
  
  const train = currentTrain.value;
  const stations = train.stations;
  const totalDuration = train.duration;
  
  // 获取所有站点坐标
  const path = stations.map(station => station.coordinate);
  
  // 如果动画未开始，启动动画
  if (!trainMarker.value.moveAnimation) {
    // 创建已经走过的路线
    if (!passedPolyLine.value) {
      passedPolyLine.value = new AMap.value.Polyline({
        map: map.value,
        strokeColor: "#4CAF50",  // 绿色
        strokeWeight: 6,
        zIndex: 101
      });
    }

    // 监听移动事件
    trainMarker.value.on('moving', (e) => {
      // 更新已经走过的路线
      passedPolyLine.value.setPath(e.passedPath);
      
      // 如果锁定列车视图，则跟随列车移动
      if (isTrainLocked.value) {
        map.value.setCenter(e.target.getPosition(), true);
      }
      
      // 计算当前速度和更新状态
      updateCurrentStatus();
    });

    // 启动移动动画
    trainMarker.value.moveAlong(path, {
      duration: totalDuration * 1000 / timeMultiplier.value,
      autoRotation: true,
      // 添加动画完成回调
      onComplete: () => {
        // 确保在动画完成时更新到最后一个站点
        currentStationIndex.value = stations.length - 1;
        updateCurrentStatus();
      }
    });
  }
}

// 处理列车变更
function handleTrainChange(trainId) {
  stopSimulation();
  
  if (!map.value || !AMap.value) return;
  
  // 清除之前的标记
  clearMapMarkers();
  
  const train = trains.value.find(t => t.id === trainId);
  if (!train) return;
  
  // 创建路线
  const path = createTrainPath(train.stations);
  polyLine.value = new AMap.value.Polyline({
    path: path,
    strokeColor: train.color || "#0091ff",
    strokeWeight: 6,
    strokeOpacity: 0.8,
    strokeStyle: "solid",
    showDir: true,
    dirColor: '#fff'
  });
  
  // 添加路线到地图
  map.value.add(polyLine.value);
  
  // 添加站点标记
  train.stations.forEach((station, index) => {
    const marker = new AMap.value.Marker({
      position: station.coordinate,
      title: station.name,
      icon: new AMap.value.Icon({
        size: new AMap.value.Size(25, 34),
        image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
        imageSize: new AMap.value.Size(25, 34),
      }),
      offset: new AMap.value.Pixel(-13, -30),
      zIndex: 100
    });
    
    marker.setMap(map.value);
    
    // 为站点标记添加点击事件
    let stationInfoWindows = [];
    marker.on('click', () => {
      // 关闭已打开的站点信息窗口
      if (stationInfoWindows[index]) {
        stationInfoWindows[index].close();
        stationInfoWindows[index] = null;
        return;
      }

      // 关闭其他可能打开的站点信息窗口
      stationInfoWindows.forEach((window, idx) => {
        if (window && idx !== index) {
          window.close();
          stationInfoWindows[idx] = null;
        }
      });
      
      const infoWindow = new AMap.value.InfoWindow({
        isCustom: true,
        content: `
          <div class="station-info">
            <h4>${station.name}</h4>
            <p>到达: ${station.arriveTime || '始发站'}</p>
            <p>发车: ${station.departTime || '终点站'}</p>
            <p>停留: ${station.stop}分钟</p>
          </div>
        `,
        offset: new AMap.value.Pixel(0, -30)
      });
      
      infoWindow.open(map.value, station.coordinate);
      stationInfoWindows[index] = infoWindow;
    });
    
    stationMarkers.value.push(marker);
  });
  
  // 创建列车标记
  trainMarker.value = createTrainMarker(train);
  trainMarker.value.setMap(map.value);
  
  // 设置地图可见区域
  resetMapView();
}

// 重置地图视图
function resetMapView() {
  if (!map.value || stationMarkers.value.length === 0) return;
  map.value.setFitView([...stationMarkers.value, trainMarker.value]);
}

// 清除地图标记
function clearMapMarkers() {
  if (!map.value) return;
  
  if (polyLine.value) {
    map.value.remove(polyLine.value);
    polyLine.value = null;
  }
  
  if (trainMarker.value) {
    map.value.remove(trainMarker.value);
    trainMarker.value = null;
  }
  
  if (stationMarkers.value.length > 0) {
    map.value.remove(stationMarkers.value);
    stationMarkers.value = [];
  }
}

// 更新当前状态
function updateCurrentStatus() {
  if (!currentTrain.value || !trainMarker.value) return;
  
  const train = currentTrain.value;
  const stations = train.stations;
  const totalDuration = train.duration;
  const elapsed = Math.min(simulationElapsedTime.value, totalDuration);
  
  // 获取当前列车位置
  const currentPos = trainMarker.value.getPosition();
  
  // 更新当前站点索引
  let nearestStationIndex = 0;
  let minDistance = Infinity;
  
  stations.forEach((station, index) => {
    const distance = calculateDistance(
      [currentPos.getLng(), currentPos.getLat()],
      station.coordinate
    );
    if (distance < minDistance) {
      minDistance = distance;
      nearestStationIndex = index;
    }
  });
  
  // 如果距离小于100米，认为到达该站点
  if (minDistance < 0.1 && nearestStationIndex !== currentStationIndex.value) {
    currentStationIndex.value = nearestStationIndex;
    if (nearestStationIndex > 0) {
      message.info(`列车已到达${stations[nearestStationIndex].name}站`);
    }
  }
  
  // 获取当前站和下一站
  const currentStation = stations[currentStationIndex.value];
  const nextStation = currentStationIndex.value < stations.length - 1 ? stations[currentStationIndex.value + 1] : null;
  
  // 计算总行程进度 - 使用更精确的方式
  let progress = 0;
  
  if (currentStationIndex.value === stations.length - 1) {
    // 到达终点
    progress = 100;
  } else {
    // 计算路线总长度
    const totalPathLength = polyLine.value.getLength();
    
    // 获取当前位置到起点的路径长度
    const passedPath = passedPolyLine.value ? passedPolyLine.value.getPath() : [];
    const passedLength = passedPath.length > 0 ? passedPolyLine.value.getLength() : 0;
    
    // 计算进度
    progress = Math.floor((passedLength / totalPathLength) * 100);
  }
  
  // 确保进度在0-100之间
  progress = Math.max(0, Math.min(100, progress));
  
  // 计算目标速度
  if (nextStation) {
    const segmentDistance = nextStation.distance - currentStation.distance;
    const startTimeStr = currentStation.departTime || currentStation.time;
    const endTimeStr = nextStation.arriveTime || nextStation.time;
    
    const startMinutes = parseTimeToMinutes(startTimeStr);
    const endMinutes = parseTimeToMinutes(endTimeStr);
    
    const segmentDuration = (endMinutes - startMinutes) * 60; // 秒
    targetSpeed.value = segmentDistance / (segmentDuration / 3600); // km/h
    
    // 添加小幅度随机波动（±2km/h）
    targetSpeed.value += (Math.random() * 4 - 2);
    targetSpeed.value = Math.max(0, Math.min(train.maxSpeed, targetSpeed.value));
  } else {
    targetSpeed.value = 0;
  }
  
  // 预计到达时间
  let eta = '-';
  if (nextStation) {
    eta = nextStation.arriveTime || nextStation.time;
  }
  
  // 更新状态
  currentStatus.value = {
    station: currentStation.name,
    speed: Math.round(currentSpeed.value),
    nextStation: nextStation ? nextStation.name : '终点站',
    eta: eta,
    progress: progress
  };
  
  // 只有在真正到达终点站时才停止模拟
  if (currentStationIndex.value === stations.length - 1 && minDistance < 0.1) {
    message.success(`${train.code}列车已到达终点站${train.toStation}`);
    stopSimulation();
  }
}

// 平滑更新速度
function updateSpeed() {
  if (!isSimulating.value) return;
  
  // 计算速度差
  const speedDiff = targetSpeed.value - currentSpeed.value;
  
  // 使用缓动函数平滑更新速度
  if (Math.abs(speedDiff) > 0.1) {
    currentSpeed.value += speedDiff * 0.1; // 每次更新只改变差值的10%
  } else {
    currentSpeed.value = targetSpeed.value;
  }
}

// 开始监控
function startSimulation() {
  if (isSimulating.value || !currentTrain.value) return;
  
  isSimulating.value = true;
  simulationStartTime.value = Date.now();
  simulationElapsedTime.value = 0;
  currentStationIndex.value = 0;
  lastPosition.value = null;
  currentSpeed.value = 0;
  targetSpeed.value = 0;
  
  // 确保列车从始发站出发
  if (trainMarker.value && currentTrain.value) {
    const startStation = currentTrain.value.stations[0];
    trainMarker.value.setPosition(startStation.coordinate);
    message.success(`${currentTrain.value.code}列车从${startStation.name}出发`);
  }
  
  // 更新当前状态
  updateCurrentStatus();
  
  // 启动动画
  updateTrainPosition();
  
  // 设置定时器，每秒更新一次状态
  simulationTimer.value = setInterval(() => {
    simulationElapsedTime.value = (Date.now() - simulationStartTime.value) / 1000 * timeMultiplier.value;
    updateCurrentStatus();
  }, 1000);
  
  // 设置速度更新定时器，每100ms更新一次
  speedUpdateInterval.value = setInterval(updateSpeed, 100);
}

// 停止监控
function stopSimulation() {
  if (simulationTimer.value) {
    clearInterval(simulationTimer.value);
    simulationTimer.value = null;
  }
  
  if (speedUpdateInterval.value) {
    clearInterval(speedUpdateInterval.value);
    speedUpdateInterval.value = null;
  }
  
  // 停止列车动画
  if (trainMarker.value && trainMarker.value.stopMove) {
    trainMarker.value.stopMove();
  }
  
  // 清除已经走过的路线
  if (passedPolyLine.value) {
    map.value.remove(passedPolyLine.value);
    passedPolyLine.value = null;
  }
  
  currentSpeed.value = 0;
  targetSpeed.value = 0;
  isSimulating.value = false;
}

// 获取站点状态
function getStationStatus(index) {
  if (!isSimulating.value) return 'pending';
  
  if (index < currentStationIndex.value) {
    return 'passed';
  } else if (index === currentStationIndex.value) {
    return 'current';
  } else {
    return 'pending';
  }
}

// 获取站点状态颜色
function getStationStatusColor(record) {
  switch (record.status) {
    case 'passed': return 'green';
    case 'current': return 'blue';
    default: return 'default';
  }
}

// 获取站点状态文本
function getStationStatusText(record) {
  switch (record.status) {
    case 'passed': return '已过站';
    case 'current': return '当前站';
    default: return '未到达';
  }
}

// 格式化时长
function formatDuration(seconds) {
  if (!seconds) return '-';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  
  return `${hours}小时${minutes}分钟`;
}

// 在 script setup 中添加处理函数
function handleSpeedChange(value) {
  if (isSimulating.value) {
    // 如果正在模拟中，需要重启模拟以应用新的速度
    stopSimulation();
    startSimulation();
  }
}
</script>

<style lang="less" scoped>
.train-tracking-container {
  padding: 16px;
  background-color: #f0f2f5;
    height: calc(100vh - 180px);
  .train-info-card {
    margin-bottom: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    .card-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1f1f1f;
      }
    }
    
    .active-lock {
      background-color: #e6f7ff;
      color: #1890ff;
      border-color: #1890ff;
    }
  }
  
  .map-container {
    height: 100%;
    min-height: 650px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .info-panel {
    height: 100%;
    min-height: 650px;
    display: flex;
    flex-direction: column;
    
    .custom-tabs {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      :deep(.ant-tabs-content) {
        flex: 1;
        overflow: hidden;
      }
      
      :deep(.ant-tabs-tabpane) {
        height: 100%;
        overflow: hidden;
      }
    }
    
    .schedule-table {
      height: 100%;
      overflow: hidden;
    }
    
    .train-detail-info {
      margin: 16px 0;
    }
    
    .status-card {
      margin-top: 16px;
      
      .ant-statistic {
        margin-bottom: 8px;
        
        .ant-statistic-title {
          font-size: 13px;
          color: #666;
        }
      }
      
      .progress-bar {
        margin-top: 16px;
      }
    }
  }
}

// 站点信息窗口样式
:deep(.station-info) {
  padding: 12px;
  background: #fff;
  h4 {
    margin: 0 0 8px 0;
    color: #1890ff;
    font-size: 15px;
  }
  
  p {
    margin: 4px 0;
    color: #666;
  }
}

// 列车标签样式
:deep(.train-label) {
  background-color: rgba(24, 144, 255, 0.8);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

// 列车信息窗口样式
:deep(.train-info-window) {
  padding: 16px;
  min-width: 240px;
  
  .train-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
    
    h4 {
      margin: 0;
      font-weight: bold;
      color: #1890ff;
      font-size: 16px;
    }
    
    .train-type {
      color: white;
      padding: 2px 8px;
      border-radius: 10px;
      font-size: 12px;
      background: linear-gradient(45deg, #1890ff, #52c41a);
    }
  }
  
  .train-info-body {
    p {
      margin: 8px 0;
      font-size: 13px;
      color: #666;
      
      span {
        font-weight: 500;
        margin-right: 5px;
        color: #333;
      }
    }
  }
}
</style>
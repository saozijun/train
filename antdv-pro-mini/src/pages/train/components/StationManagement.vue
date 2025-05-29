<template>
  <div>
    <a-modal
      v-model:open="visible"
      :title="'站点管理 - ' + (currentTrain?.code || '')"
      width="1200px"
      :footer="null"
      :afterClose="afterClose"
      :bodyStyle="{ padding: '16px', backgroundColor: '#f5f7fa' }"
    >
      <div class="station-management">
        <a-row :gutter="[16, 16]">
          <a-col :span="24">
            <a-card :bordered="false" :bodyStyle="{ padding: '16px' }" class="action-card">
              <div class="action-header">
                <a-space>
                  <a-button type="primary" @click="addStation">
                    <template #icon>
                      <PlusOutlined />
                    </template>
                    添加站点
                  </a-button>
                  <a-button type="primary" @click="saveStations" :loading="saving">
                    <template #icon>
                      <SaveOutlined />
                    </template>
                    保存路线
                  </a-button>
                </a-space>
                <a-tooltip title="提示：可以在地图上点击添加站点，拖动已有站点修改位置">
                  <a-button type="link" class="help-btn">
                    <template #icon>
                      <InfoCircleOutlined />
                    </template>
                    操作指南
                  </a-button>
                </a-tooltip>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-card :bordered="false" :bodyStyle="{ padding: '0px' }" class="station-card">
              <template #title>
                <div class="card-title">
                  <div class="title-text">站点列表</div>
                  <a-tag v-if="stations.length > 0" color="processing">{{ stations.length }}个站点</a-tag>
                </div>
              </template>
              <a-table
                :columns="columns"
                :data-source="stations"
                :pagination="false"
                :rowKey="record => record.id || record.tempId"
                :rowClassName="(record, index) => (selectedStationIndex === index ? 'selected-row' : '')"
                @row-click="(record, index) => selectStation(index)"
                size="small"
              >
                <template #bodyCell="{ column, index }">
                  <template v-if="column.key === 'operation'">
                    <div class="operation-buttons">
                      <a-button type="text" size="small" @click="moveUp(index)" :disabled="index === 0">
                        <template #icon><ArrowUpOutlined /></template>
                      </a-button>
                      <a-button type="text" size="small" @click="moveDown(index)" :disabled="index === stations.length - 1">
                        <template #icon><ArrowDownOutlined /></template>
                      </a-button>
                      <a-button type="text" size="small" @click="editStation(index)">
                        <template #icon><EditOutlined /></template>
                      </a-button>
                      <a-popconfirm title="确定删除吗?" @confirm="removeStation(index)">
                        <a-button type="text" danger size="small">
                          <template #icon><DeleteOutlined /></template>
                        </a-button>
                      </a-popconfirm>
                    </div>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>
          <a-col :span="16">
            <a-card :bordered="false" :bodyStyle="{ padding: '0px' }" class="map-card">
              <template #title>
                <div class="card-title">
                  <div class="title-text">路线地图</div>
                  <a-tag v-if="currentTrain" :color="currentTrain.color || '#1890ff'">
                    {{ currentTrain.fromStation }} - {{ currentTrain.toStation }}
                  </a-tag>
                </div>
              </template>
              <div class="map-container" ref="mapContainer"></div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-modal>

    <a-modal
      v-model:open="stationModalVisible"
      :title="currentStation.id ? '编辑站点' : '添加站点'"
      @ok="handleStationOk"
      :confirm-loading="confirmLoading"
      :afterClose="afterStationClose"
      :width="520"
    >
      <a-form
        ref="stationFormRef"
        :model="currentStation"
        :rules="stationRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="站点名称" name="name">
          <a-input v-model:value="currentStation.name" placeholder="请输入站点名称" />
        </a-form-item>
        <a-row :gutter="8">
          <a-col :span="12">
            <a-form-item label="到达时间" name="arriveTime" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-time-picker v-model:value="currentStation.arriveTime" format="HH:mm" placeholder="如: 08:30" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="发车时间" name="departTime" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-time-picker v-model:value="currentStation.departTime" format="HH:mm" placeholder="如: 08:35" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="8">
          <a-col :span="12">
            <a-form-item label="停留时间" name="stop" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-input-number v-model:value="currentStation.stop" placeholder="分钟" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="距离起点" name="distance" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-input-number 
                v-model:value="currentStation.distance" 
                placeholder="公里" 
                style="width: 100%" 
                :disabled="editingIndex.value > 0"
              />
              <template v-if="editingIndex.value > 0">
                <div style="color: #999; font-size: 12px; margin-top: 4px;">根据站点顺序自动计算</div>
              </template>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="8">
          <a-col :span="12">
            <a-form-item label="经度" name="longitude" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-input-number v-model:value="currentStation.longitude" style="width: 100%" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="纬度" name="latitude" :label-col="{ span: 12 }" :wrapper-col="{ span: 12 }">
              <a-input-number v-model:value="currentStation.latitude" style="width: 100%" :precision="6" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { 
  PlusOutlined, 
  SaveOutlined, 
  InfoCircleOutlined, 
  ArrowUpOutlined, 
  ArrowDownOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue';
import { getByTrainId, saveBatch } from '~/api/station';
import AMapLoader from '@amap/amap-jsapi-loader';

const visible = ref(false);
const stationModalVisible = ref(false);
const confirmLoading = ref(false);
const saving = ref(false);
const currentTrain = ref(null);
const stations = ref([]);
const stationFormRef = ref();
const editingIndex = ref(-1);
const selectedStationIndex = ref(-1);
const mapContainer = ref(null);
const map = ref(null);
const AMap = ref(null);
const stationMarkers = ref([]);
const polyLine = ref(null);

const currentStation = ref({
  name: '',
  arriveTime: '',
  departTime: '',
  stop: 0,
  distance: 0,
  longitude: null,
  latitude: null,
  trainId: null
});

const stationRules = {
  name: [{ required: true, message: '请输入站点名称', trigger: 'change' }],
  distance: [{ required: true, message: '请输入距离', trigger: 'change' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'change' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'change' }]
};

const columns = [
  {
    title: '序号',
    key: 'sequence',
    width: 50,
    customRender: ({ index }) => index + 1
  },
  {
    title: '站点名称',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true
  },
  {
    title: '到达/发车',
    dataIndex: 'arriveTime',
    key: 'arriveTime',
    width: 120,
    customRender: ({ record }) => {
      return `${record.arriveTime || '-'} / ${record.departTime || '-'}`;
    }
  },
  {
    title: '操作',
    key: 'operation',
    width: 200
  }
];

const open = async (train) => {
  currentTrain.value = train;
  visible.value = true;
  
  await nextTick();
  // 加载站点数据
  await loadStations(train.id);
  // 初始化地图
  await initMap();
};

const afterClose = () => {
  stations.value = [];
  currentTrain.value = null;
  destroyMap();
};

const loadStations = async (trainId) => {
  try {
    const { data } = await getByTrainId(trainId);
    stations.value = data || [];
    
    // 地图已加载后，更新地图上的站点标记
    if (map.value) {
      updateMapMarkers();
      drawRouteLine();
    }
  } catch (error) {
    console.log(error);
    message.error('加载站点失败');
  }
};

// 初始化地图
const initMap = async () => {
  if (!mapContainer.value) return;
  
  try {
    if (!AMap.value) {
      AMap.value = await AMapLoader.load({
        key: '4a3d90ff3f6b025a96940716e5c29eb6', 
        version: '2.0',
        plugins: ['AMap.Scale', 'AMap.ToolBar']
      });
    }
    
    // 创建地图实例
    map.value = new AMap.value.Map(mapContainer.value, {
      viewMode: '2D',
      zoom: 5,
      center: [116.397428, 39.90923], // 默认中心点
      mapStyle: 'amap://styles/whitesmoke'
    });
    
    // 添加地图控件
    map.value.addControl(new AMap.value.Scale());
    map.value.addControl(new AMap.value.ToolBar({
      position: 'RB',
      liteStyle: true
    }));
    
    // 添加地图点击事件，用于添加新站点
    map.value.on('click', handleMapClick);
    
    // 更新地图上的站点标记
    updateMapMarkers();
    drawRouteLine();
    
    // 如果有站点，则将地图中心设置为站点中心
    if (stations.value.length > 0) {
      fitMapToBounds();
    }
  } catch (e) {
    console.error('地图初始化失败:', e);
    message.error('地图加载失败，请检查网络');
  }
};

// 销毁地图
const destroyMap = () => {
  if (map.value) {
    map.value.destroy();
    map.value = null;
  }
};

// 更新地图上的站点标记
const updateMapMarkers = () => {
  if (!map.value || !AMap.value) return;
  
  // 清除已有标记
  if (stationMarkers.value.length > 0) {
    map.value.remove(stationMarkers.value);
    stationMarkers.value = [];
  }
  
  // 添加站点标记
  stations.value.forEach((station, index) => {
    if (!station.longitude || !station.latitude) return;
    
    const marker = new AMap.value.Marker({
      position: [station.longitude, station.latitude],
      title: station.name,
      draggable: true, // 可拖动
      cursor: 'move',
      label: {
        content: `<div style="padding: 3px 5px; background-color: #fff; border-radius: 2px; border: 1px solid #ccc;">${index + 1}. ${station.name}</div>`,
        direction: 'top'
      }
    });
    
    // 为标记添加点击事件
    marker.on('click', () => {
      selectStation(index);
    });
    
    // 拖动结束后更新站点坐标
    marker.on('dragend', (e) => {
      const position = marker.getPosition();
      stations.value[index].longitude = position.getLng();
      stations.value[index].latitude = position.getLat();
      
      // 站点位置变更后重新计算所有距离
      recalculateDistances();
      
      drawRouteLine(); // 重新绘制路线
    });
    
    marker.setMap(map.value);
    stationMarkers.value.push(marker);
  });
};

// 绘制路线
const drawRouteLine = () => {
  if (!map.value || !AMap.value || stations.value.length < 2) return;
  
  // 清除已有路线
  if (polyLine.value) {
    map.value.remove(polyLine.value);
    polyLine.value = null;
  }
  
  // 创建路径点
  const path = stations.value
    .filter(station => station.longitude && station.latitude)
    .map(station => [station.longitude, station.latitude]);
  
  if (path.length < 2) return;
  
  // 创建折线
  polyLine.value = new AMap.value.Polyline({
    path: path,
    strokeColor: currentTrain.value?.color || "#1890ff",
    strokeWeight: 6,
    strokeOpacity: 0.8,
    strokeStyle: "solid",
    showDir: true
  });
  
  // 添加路线到地图
  map.value.add(polyLine.value);
};

// 地图适应所有站点
const fitMapToBounds = () => {
  if (!map.value || stationMarkers.value.length === 0) return;
  
  map.value.setFitView(stationMarkers.value);
};

// 处理地图点击，添加新站点
const handleMapClick = (e) => {
  // 不在表单编辑时才能添加新站点
  if (stationModalVisible.value) return;
  
  // 获取点击位置的经纬度
  const lng = e.lnglat.getLng();
  const lat = e.lnglat.getLat();
  
  // 准备添加新站点
  currentStation.value = {
    name: `新站点${stations.value.length + 1}`,
    arriveTime: '',
    departTime: '',
    stop: 0,
    distance: calculateDistance(stations.value),
    longitude: lng,
    latitude: lat,
    trainId: currentTrain.value.id,
    tempId: Date.now() // 临时ID，用于前端标识
  };
  
  editingIndex.value = -1;
  stationModalVisible.value = true;
};

// 计算新站点的距离
const calculateDistance = (stationList) => {
  if (stationList.length === 0) return 0;
  
  // 如果有上一个站点，则使用其距离加上估算值
  const lastStation = stationList[stationList.length - 1];
  return lastStation.distance + 50; // 默认加50公里
};

// 选择站点
const selectStation = (index) => {
  selectedStationIndex.value = index;
  
  // 高亮对应的标记
  if (map.value && stationMarkers.value[index]) {
    stationMarkers.value[index].setAnimation('AMAP_ANIMATION_BOUNCE');
    setTimeout(() => {
      stationMarkers.value[index].setAnimation(null);
    }, 2000);
    
    // 将地图中心移动到该站点
    map.value.setCenter(stationMarkers.value[index].getPosition());
  }
};

// 编辑站点
const editStation = (index) => {
  const station = { ...stations.value[index] };
  
  // 将时间字符串转换为 dayjs 对象
  if (station.arriveTime) {
    station.arriveTime = dayjs(station.arriveTime, 'HH:mm');
  }
  
  if (station.departTime) {
    station.departTime = dayjs(station.departTime, 'HH:mm');
  }
  
  // 如果不是第一个站点，自动计算距离
  if (index > 0) {
    station.distance = calculateDistanceFromPrevious(index);
  }
  
  currentStation.value = station;
  editingIndex.value = index;
  stationModalVisible.value = true;
};

// 计算与前一站的距离
const calculateDistanceFromPrevious = (index) => {
  if (index <= 0 || index >= stations.value.length) return 0;
  
  const prevStation = stations.value[index - 1];
  const currentStation = stations.value[index];
  
  // 如果有经纬度，使用地理距离计算
  if (prevStation.longitude && prevStation.latitude && 
      currentStation.longitude && currentStation.latitude) {
    return calculateGeoDistance(
      prevStation.latitude, prevStation.longitude,
      currentStation.latitude, currentStation.longitude
    );
  } else if (prevStation.distance) {
    // 否则基于上一站距离加上估算值
    return prevStation.distance + 50; // 默认加50公里
  }
  
  return 0;
};

// 根据经纬度计算距离（公里）
const calculateGeoDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // 地球半径，单位公里
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  // 四舍五入到整数公里
  return Math.round(R * c);
};

// 处理站点顺序变更后的距离重新计算
const recalculateDistances = () => {
  if (stations.value.length <= 1) return;
  
  // 重新计算所有站点距离（除了第一个站点保持为0）
  stations.value[0].distance = 0; // 确保第一个站点距离为0
  
  for (let i = 1; i < stations.value.length; i++) {
    stations.value[i].distance = calculateDistanceFromPrevious(i);
  }
};

// 上移站点
const moveUp = (index) => {
  if (index > 0) {
    const temp = stations.value[index];
    stations.value[index] = stations.value[index - 1];
    stations.value[index - 1] = temp;
    
    // 重新计算距离
    recalculateDistances();
    
    // 更新地图标记
    updateMapMarkers();
    drawRouteLine();
  }
};

// 下移站点
const moveDown = (index) => {
  if (index < stations.value.length - 1) {
    const temp = stations.value[index];
    stations.value[index] = stations.value[index + 1];
    stations.value[index + 1] = temp;
    
    // 重新计算距离
    recalculateDistances();
    
    // 更新地图标记
    updateMapMarkers();
    drawRouteLine();
  }
};

// 删除站点
const removeStation = (index) => {
  stations.value.splice(index, 1);
  
  // 更新地图标记
  updateMapMarkers();
  drawRouteLine();
};

// 保存所有站点
const saveStations = async () => {
  if (stations.value.length < 2) {
    message.warning('至少需要两个站点');
    return;
  }
  
  saving.value = true;
  try {
    // 确保所有站点都有trainId，并处理时间格式
    const stationsToSave = stations.value.map((station, index) => {
      const stationData = { ...station };
      
      // 确保时间是字符串格式
      if (stationData.arriveTime && typeof stationData.arriveTime !== 'string') {
        stationData.arriveTime = stationData.arriveTime.format('HH:mm');
      }
      
      if (stationData.departTime && typeof stationData.departTime !== 'string') {
        stationData.departTime = stationData.departTime.format('HH:mm');
      }
      
      return {
        ...stationData,
        trainId: currentTrain.value.id,
        sequence: index
      };
    });
    
    await saveBatch(stationsToSave);
    message.success('保存成功');
    await loadStations(currentTrain.value.id);
  } catch (error) {
    console.log(error);
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
};

// 处理站点表单提交
const handleStationOk = async () => {
  try {
    await stationFormRef.value.validate();
    confirmLoading.value = true;
    
    // 处理时间格式
    const stationData = { ...currentStation.value };
    
    // 将 dayjs 对象转换为字符串
    if (stationData.arriveTime && typeof stationData.arriveTime !== 'string') {
      stationData.arriveTime = stationData.arriveTime.format('HH:mm');
    }
    
    if (stationData.departTime && typeof stationData.departTime !== 'string') {
      stationData.departTime = stationData.departTime.format('HH:mm');
    }
    
    if (editingIndex.value >= 0) {
      // 编辑现有站点
      stations.value[editingIndex.value] = stationData;
    } else {
      // 添加新站点
      stations.value.push(stationData);
    }
    
    // 重新计算所有距离
    recalculateDistances();
    
    stationModalVisible.value = false;
    message.success('站点已添加到列表，请点击"保存路线"进行保存');
    
    // 更新地图标记
    updateMapMarkers();
    drawRouteLine();
  } catch (error) {
    console.log(error);
  } finally {
    confirmLoading.value = false;
  }
};

// 关闭表单
const afterStationClose = () => {
  stationFormRef.value?.resetFields();
};

// 添加站点
const addStation = () => {
  // 计算默认位置
  let defaultLng = 116.397428; // 北京
  let defaultLat = 39.90923;
  
  // 如果已有站点，则使用最后一个站点位置
  if (stations.value.length > 0) {
    const lastStation = stations.value[stations.value.length - 1];
    if (lastStation.longitude && lastStation.latitude) {
      defaultLng = lastStation.longitude;
      defaultLat = lastStation.latitude;
    }
  }
  
  currentStation.value = {
    name: '',
    arriveTime: '',
    departTime: '',
    stop: 0,
    distance: calculateDistance(stations.value),
    longitude: defaultLng,
    latitude: defaultLat,
    trainId: currentTrain.value.id,
    tempId: Date.now() // 临时ID，用于前端标识
  };
  
  editingIndex.value = -1;
  stationModalVisible.value = true;
};

defineExpose({
  open
});
</script>

<style lang="less" scoped>
.station-management {
  .action-card {
    margin-bottom: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    
    .action-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .help-btn {
      color: #8c8c8c;
    }
  }
  
  .card-title {
    display: flex;
    align-items: center;
    
    .title-text {
      font-weight: 500;
      margin-right: 8px;
    }
  }
  
  .station-card, .map-card {
    height: 550px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    
    :deep(.ant-card-body) {
      height: calc(100% - 57px); // 减去卡片头部高度
      display: flex;
      flex-direction: column;
    }
    
    :deep(.ant-table-wrapper) {
      flex: 1;
      overflow: hidden;
    }
    
    :deep(.ant-table) {
      height: 100%;
    }
    
    :deep(.ant-table-body) {
      height: calc(100% - 55px); // 减去表头高度
      overflow-y: auto;
    }
  }
  
  .map-container {
    height: 100%;
    border-radius: 4px;
    overflow: hidden;
  }
  
  .selected-row {
    background-color: #e6f7ff;
  }
  
  .operation-buttons {
    display: flex;
    justify-content: space-between;
    
    button {
      padding: 0 4px;
    }
  }
}
</style> 
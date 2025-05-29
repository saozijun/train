<template>
  <div class="station-marker">
    <!-- 注意：实际标记将由AMap创建，这里主要用于组织信息窗口内容 -->
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  station: {
    type: Object,
    required: true
  },
  isCurrentStation: {
    type: Boolean,
    default: false
  },
  isPassed: {
    type: Boolean,
    default: false
  }
});

// 获取信息窗口内容
const infoWindowContent = computed(() => {
  return `
    <div class="station-info">
      <h4>${props.station.name}</h4>
      <p>到达时间: ${props.station.arriveTime || '始发站'}</p>
      <p>发车时间: ${props.station.departTime || '终点站'}</p>
      <p>停留时间: ${props.station.stop}分钟</p>
      <p>里程: ${props.station.distance}公里</p>
      ${props.isCurrentStation ? '<p class="current-station">当前站</p>' : ''}
    </div>
  `;
});

// 导出给父组件使用的方法和属性
defineExpose({
  infoWindowContent
});
</script>

<style lang="less" scoped>
.station-marker {
  display: none; // 实际不会显示，由AMap控制
}

:deep(.station-info) {
  padding: 10px;
  min-width: 180px;
  background: #fff;
  h4 {
    margin: 0 0 10px;
    font-weight: bold;
    color: #1890ff;
    border-bottom: 1px solid #eee;
    padding-bottom: 5px;
  }
  
  p {
    margin: 5px 0;
    font-size: 12px;
  }
  
  .current-station {
    color: #52c41a;
    font-weight: bold;
  }
}
</style> 
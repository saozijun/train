<template>
  <div class="train-marker">
    <!-- 注意：实际标记将由AMap创建，这里主要用于组织信息窗口内容 -->
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  train: {
    type: Object,
    required: true
  },
  currentSpeed: {
    type: Number,
    default: 0
  },
  currentPosition: {
    type: Array,
    default: () => [0, 0]
  },
  nextStation: {
    type: String,
    default: ''
  },
  eta: {
    type: String,
    default: ''
  },
  progress: {
    type: Number,
    default: 0
  }
});

// 获取信息窗口内容
const infoWindowContent = computed(() => {
  return `
    <div class="train-info-window">
      <div class="train-info-header">
        <h4>${props.train.code}</h4>
        <span class="train-type">${props.train.type}</span>
      </div>
      <div class="train-info-body">
        <p><i class="info-icon origin-icon"></i><span>始发站:</span> ${props.train.from}</p>
        <p><i class="info-icon dest-icon"></i><span>终点站:</span> ${props.train.to}</p>
        <p><i class="info-icon speed-icon"></i><span>当前速度:</span> ${props.currentSpeed} km/h</p>
        <p><i class="info-icon next-icon"></i><span>下一站:</span> ${props.nextStation}</p>
        <p><i class="info-icon time-icon"></i><span>预计到达:</span> ${props.eta}</p>
      </div>
      <div class="train-progress">
        <div class="progress-bar">
          <div class="progress-inner" style="width: ${props.progress}%"></div>
        </div>
        <div class="progress-text">${props.progress}%</div>
      </div>
    </div>
  `;
});

// 导出给父组件使用的方法和属性
defineExpose({
  infoWindowContent
});
</script>

<style lang="less" scoped>
.train-marker {
  display: none; // 实际不会显示，由AMap控制
}

:deep(.train-info-window) {
  padding: 12px;
  min-width: 220px;
  
  .train-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
    
    h4 {
      margin: 0;
      font-weight: bold;
      color: #1890ff;
    }
    
    .train-type {
      background: #1890ff;
      color: white;
      padding: 2px 6px;
      border-radius: 10px;
      font-size: 12px;
    }
  }
  
  .train-info-body {
    p {
      margin: 6px 0;
      font-size: 13px;
      display: flex;
      align-items: center;
      
      span {
        font-weight: 500;
        margin-right: 5px;
      }
      
      .info-icon {
        display: inline-block;
        width: 14px;
        height: 14px;
        margin-right: 6px;
        background-color: #ccc;
        border-radius: 50%;
      }
    }
  }
  
  .train-progress {
    margin-top: 10px;
    
    .progress-bar {
      height: 6px;
      background: #f0f0f0;
      border-radius: 3px;
      overflow: hidden;
      
      .progress-inner {
        height: 100%;
        background: linear-gradient(to right, #108ee9, #87d068);
        border-radius: 3px;
        transition: width 0.3s ease;
      }
    }
    
    .progress-text {
      margin-top: 3px;
      text-align: right;
      font-size: 12px;
      color: #888;
    }
  }
}
</style> 
<template>
  <a-modal
    v-model:open="visible"
    :title="modelRef.id ? '编辑列车' : '新增列车'"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    :afterClose="afterClose"
  >
    <a-form
      ref="formRef"
      :model="modelRef"
      :rules="rules"
      :label-col="{ style: { width: '80px' } }"
    >
      <a-form-item label="车次编号" name="code">
        <a-input v-model:value="modelRef.code" placeholder="请输入车次编号" />
      </a-form-item>
      <a-form-item label="始发站" name="fromStation">
        <a-input v-model:value="modelRef.fromStation" placeholder="请输入始发站" />
      </a-form-item>
      <a-form-item label="终点站" name="toStation">
        <a-input v-model:value="modelRef.toStation" placeholder="请输入终点站" />
      </a-form-item>
      <a-form-item label="列车类型" name="type">
        <a-select v-model:value="modelRef.type" placeholder="请选择列车类型">
          <a-select-option value="复兴号">复兴号</a-select-option>
          <a-select-option value="和谐号">和谐号</a-select-option>
          <a-select-option value="动车组">动车组</a-select-option>
          <a-select-option value="普通列车">普通列车</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="总里程" name="distance">
        <a-input-number v-model:value="modelRef.distance" placeholder="请输入总里程" addon-after="km" style="width: 100%" />
      </a-form-item>
      <a-form-item label="运行时间" name="duration">
        <a-input-number v-model:value="modelRef.duration" placeholder="请输入运行时间" addon-after="秒" style="width: 100%" />
      </a-form-item>
      <a-form-item label="最高速度" name="maxSpeed">
        <a-input-number v-model:value="modelRef.maxSpeed" placeholder="请输入最高速度" addon-after="km/h" style="width: 100%" />
      </a-form-item>
      <a-form-item label="列车颜色" name="color">
        <div class="color-picker-container">
          <div class="input-with-color">
            <div class="color-block" :style="{ background: modelRef.color || '#1890ff' }"></div>
            <a-input v-model:value="modelRef.color" placeholder="请输入颜色代码，如: #1890ff" disabled />
          </div>
          <a-popover
            trigger="click"
            placement="bottomLeft"
            v-model:visible="colorPickerVisible"
            destroyTooltipOnHide
            overlayClassName="color-picker-popover"
          >
            <template #content>
              <div class="color-palette" @click.stop>
                <div class="custom-color">
                  <a-input
                    type="color"
                    v-model:value="tempColor"
                    class="color-input"
                  />
                  <a-button type="primary" size="small" @click.stop="applyColor">应用</a-button>
                </div>
              </div>
            </template>
            <template #title>
              <div class="color-picker-title">选择列车颜色</div>
            </template>
            <a-button
              class="color-preview"
              :style="{ backgroundColor: modelRef.color || '#1890ff', borderColor: modelRef.color || '#1890ff' }"
            >
              <span class="color-preview-text" :style="{ color: getContrastColor(modelRef.color || '#1890ff') }">
                选色
              </span>
            </a-button>
          </a-popover>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, h } from "vue";
import { message } from 'ant-design-vue';
import { save } from '~/api/train';

const formRef = ref();
const visible = ref(false);
const confirmLoading = ref(false);
const emits = defineEmits(["saveOk"]);
const colorPickerVisible = ref(false);
const tempColor = ref('#1890ff'); // 临时颜色，用于预览

const modelRef = ref({
  code: "",
  fromStation: "",
  toStation: "",
  type: "",
  distance: 0,
  duration: 0,
  maxSpeed: 0,
  color: "#1890ff"
});

// 应用颜色到表单
const applyColor = () => {
  modelRef.value.color = tempColor.value;
  colorPickerVisible.value = false;
  message.success('颜色已应用');
};

// 获取与背景颜色对比度高的文字颜色
const getContrastColor = (hexColor) => {
  // 将十六进制颜色转换为RGB
  const r = parseInt(hexColor.slice(1, 3), 16);
  const g = parseInt(hexColor.slice(3, 5), 16);
  const b = parseInt(hexColor.slice(5, 7), 16);
  
  // 计算亮度
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  
  // 根据亮度返回黑色或白色
  return brightness > 128 ? '#000000' : '#ffffff';
};

const rules = {
  code: [{ required: true, message: '请输入车次编号', trigger: 'change' }],
  fromStation: [{ required: true, message: '请输入始发站', trigger: 'change' }],
  toStation: [{ required: true, message: '请输入终点站', trigger: 'change' }],
  type: [{ required: true, message: '请选择列车类型', trigger: 'change' }],
  distance: [{ required: true, message: '请输入总里程', trigger: 'change' }],
  duration: [{ required: true, message: '请输入运行时间', trigger: 'change' }],
  maxSpeed: [{ required: true, message: '请输入最高速度', trigger: 'change' }],
  color: [{ required: true, message: '请输入列车颜色', trigger: 'change' }],
};

const afterClose = () => {
  formRef.value?.resetFields();
  colorPickerVisible.value = false;
};

const handleOk = async () => {
  try {
    await formRef.value.validate();
    confirmLoading.value = true;
    await save(modelRef.value);
    message.success('操作成功');
    emits('saveOk');
    visible.value = false;
  } catch (error) {
    console.log(error);
  } finally {
    confirmLoading.value = false;
  }
};

const open = async (row) => {
  modelRef.value = JSON.parse(JSON.stringify(row));
  if (!modelRef.value.color) {
    modelRef.value.color = '#1890ff';
  }
  tempColor.value = modelRef.value.color;
  visible.value = true;
};

defineExpose({
  open,
});
</script>

<style lang="less" scoped>
.ant-form {
  margin-top: 20px;
}

.color-picker-container {
  display: flex;
  align-items: center;
  
  .input-with-color {
    position: relative;
    flex: 1;
    display: flex;
    
    .color-block {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      width: 16px;
      height: 16px;
      border-radius: 2px;
      z-index: 1;
    }
    
    .ant-input {
      flex: 1;
      padding-left: 36px;
    }
  }
  
  .color-preview {
    margin-left: 8px;
    width: 60px;
    height: 32px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 2px;
    
    .color-preview-text {
      font-size: 12px;
    }
  }
}

.color-palette {
  width: 250px;
  
  .custom-color {
    display: flex;
    align-items: center;
    
    .color-input {
      flex: 1;
      height: 40px;
      border: none;
      background: none;
      margin-right: 8px;
      cursor: pointer;
      
      :deep(input[type="color"]) {
        width: 100%;
        height: 40px;
        border: 1px solid #f0f0f0;
        padding: 0;
        background: none;
        border-radius: 4px;
      }
    }
  }
}

.color-picker-title {
  font-size: 14px;
  margin-bottom: 8px;
}

:global(.color-picker-popover) {
  .ant-popover-inner-content {
    padding: 12px;
  }
}
</style> 
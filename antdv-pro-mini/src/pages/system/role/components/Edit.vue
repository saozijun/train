<template>
  <a-modal
    v-model:open="visible"
    :title="modelRef.id ? '编辑' : '新增'"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    :afterClose="afterClose"
  >
    <a-form name="form">
      <a-form-item label="名称" v-bind="validateInfos.name" >
        <a-input v-model:value="modelRef.name" placeholder="请输入"/>
      </a-form-item>
      <a-form-item label="描述" v-bind="validateInfos.description" >
        <a-input v-model:value="modelRef.description" placeholder="请输入"/>
      </a-form-item>
      <a-form-item label="标识" v-bind="validateInfos.flag" >
        <a-input v-model:value="modelRef.flag" placeholder="请输入" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
import { ref } from "vue";
import { Form, message } from 'ant-design-vue';
import { save } from '~/api/role'
const useForm = Form.useForm;
const visible = ref(false);
const confirmLoading = ref(false);
const emits = defineEmits(["saveOk"]);
const modelRef = ref({
  name: "",
  description: "",
  flag: ""
})

const afterClose = () => {
  resetFields(); 
};

const { resetFields, validate, validateInfos } = useForm(
  modelRef,
  reactive({
    name: [{ required: true, message: '请输入名称'}],
    description: [{ required: true, message: '请输入描述'}],
    flag: [ { required: true, message: '请输入标识'}]
  }),
);
const handleOk = async () => {
  await validate();
  confirmLoading.value = true;
  await save(modelRef.value);
  message.success('操作成功');
  emits('saveOk');
  visible.value = false;
  confirmLoading.value = false;
};
const open = (row) => {
  modelRef.value = JSON.parse(JSON.stringify(row));
  visible.value = true;
};
defineExpose({
  open,
});
</script>
<style lang="less" scoped>
.ant-form{
  margin-top: 20px;
}
</style>
<template>
  <a-modal
    v-model:open="visible"
    :title="modelRef.id ? '编辑' : '新增'"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    :afterClose="afterClose"
  >
    <a-form
      ref="formRef"
      :model="modelRef"
      :rules="rules"
      :label-col="{style:{width: '60px'}}"
    >
      <a-form-item label="昵称" name="nickname">
        <a-input v-model:value="modelRef.nickname" placeholder="请输入"/>
      </a-form-item>
      <a-form-item label="账号" name="username">
        <a-input v-model:value="modelRef.username" placeholder="请输入"/>
      </a-form-item>
      <a-form-item label="密码" name="password">
        <a-input-password v-model:value="modelRef.password" placeholder="请输入"/>
      </a-form-item>
      <a-form-item label="角色" name="roleId">
        <a-select v-model:value="modelRef.roleId" placeholder="请选择">
          <a-select-option v-for="(item) in roleList" :value="item.id" :key="item.id">{{item.name}}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="modelRef.email" type="email" placeholder="请输入"/>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref } from "vue";
import { message } from 'ant-design-vue';
import { save } from '~/api/user'
import { zdList } from '~/api/role'

const formRef = ref();
const visible = ref(false);
const confirmLoading = ref(false);
const emits = defineEmits(["saveOk"]);
const roleList = ref([])

const modelRef = ref({
  nickname: "",
  username: "",
  password: "",
  email: "",
  role: "",
  roleId: ""
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'change' }],
  username: [{ required: true, message: '请输入账号', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'change' }],
};

const afterClose = () => {
  formRef.value?.resetFields();
};

const handleOk = async () => {
  try {
    await formRef.value.validate();
    confirmLoading.value = true;
    let role = roleList.value.find(item => item.id == modelRef.value.roleId)
    modelRef.value.role = role.flag
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
  roleList.value = (await zdList()).data
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
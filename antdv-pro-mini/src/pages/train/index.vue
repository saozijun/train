<template>
  <div class="box">
    <a-card mb-4>
      <a-form :model="formModel">
        <a-row :gutter="[15, 0]">
          <a-col>
            <a-form-item name="code" label="车次">
              <a-input v-model:value="formModel.code" placeholder="请输入车次" />
            </a-form-item>
          </a-col>
          <a-col>
            <a-form-item name="type" label="类型">
              <a-input v-model:value="formModel.type" placeholder="请输入类型" />
            </a-form-item>
          </a-col>
          <a-col>
            <a-space flex justify-end w-full>
              <a-button :loading="loading" type="primary" @click="onSearch">
                查询
              </a-button>
              <a-button :loading="loading" @click="onReset">
                重置
              </a-button>
              <a-button type="primary" @click="open">
                <template #icon>
                  <PlusOutlined />
                </template>
                新增
              </a-button>
              <a-button type="primary" @click="showAIRouteModal" :loading="aiLoading">
                <template #icon>
                  <ThunderboltOutlined />
                </template>
                AI生成路线
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
    <a-table :columns="columns" :data-source="tableData" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          <span>
            <a-tag color="blue">{{ record.type }}</a-tag>
          </span>
        </template>
        <template v-else-if="column.key === 'operation'">
          <a-button style="padding: 0;" type="link" @click="open(record)">编辑</a-button>
          <a-button type="link" @click="openStationManagement(record)">管理站点</a-button>
          <a-popconfirm title="确定删除吗?" @confirm="onDelete(record.id)">
            <a-button style="padding: 0;"  type="link" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
    <div class="pagination">
      <a-pagination v-model:current="formModel.pageNum" :total="total" @change="onPageChange" />
    </div>
    <Edit ref="editRef" @saveOk="getList"></Edit>
    <StationManagement ref="stationManagementRef"></StationManagement>
    
    <!-- AI生成路线模态框 -->
    <a-modal v-model:visible="aiModalVisible" title="AI生成路线" @ok="generateAIRoute">
      <a-form :model="aiFormModel">
        <a-form-item label="起始站点">
          <a-input v-model:value="aiFormModel.start" placeholder="选填，不填则随机生成" />
        </a-form-item>
        <a-form-item label="终点站点" style="margin-top: 10px;">
          <a-input v-model:value="aiFormModel.end" placeholder="选填，不填则随机生成" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script setup>
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue';
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { list, del, generateTrainRoute, generateTrainRouteWithLocations } from '~/api/train';
import Edit from './components/Edit.vue';
import StationManagement from './components/StationManagement.vue';

const editRef = ref(null);
const stationManagementRef = ref(null);
const loading = ref(false);
const aiLoading = ref(false);
const aiModalVisible = ref(false);
const aiFormModel = ref({
  start: '',
  end: ''
});
const tableData = ref([]);
const total = ref(0);
const formModel = ref({
  pageNum: 1,
  pageSize: 10,
  code: "",
  type: "",
});

onMounted(() => {
  getList();
});

const onPageChange = (page) => {
  formModel.value.pageNum = page;
  getList();
};

const onSearch = () => {
  formModel.value.pageNum = 1;
  getList();
};

const onReset = () => {
  formModel.value = {
    pageNum: 1,
    pageSize: 10,
    code: "",
    type: "",
  };
  getList();
};

const getList = async () => {
  loading.value = true;
  try {
    const { data } = await list(formModel.value);
    total.value = data.total;
    data.records.map((item, i) => { item.index = i + 1 });
    tableData.value = data.records;
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
  }
};

const onDelete = async (id) => {
  try {
    await del({ id });
    getList();
    message.success('删除成功');
  } catch (error) {
    console.log(error);
  }
};

const showAIRouteModal = () => {
  aiFormModel.value = {
    start: '',
    end: ''
  };
  aiModalVisible.value = true;
};

const generateAIRoute = async () => {
  aiLoading.value = true;
  aiModalVisible.value = false;
  
  try {
    const start = aiFormModel.value.start ? aiFormModel.value.start.trim() : '';
    const end = aiFormModel.value.end ? aiFormModel.value.end.trim() : '';
    
    console.log('前端发送参数 - 起点:', start, '终点:', end);
    
    // 根据是否有起始点和终点选择调用不同的API
    if (start || end) {
      await generateTrainRouteWithLocations(start, end);
      message.success(`AI路线生成成功 (${start ? start : '随机起点'} → ${end ? end : '随机终点'})`);
    } else {
      await generateTrainRoute();
      message.success('AI随机路线生成成功');
    }
    
    getList();
  } catch (error) {
    console.error('生成路线错误:', error);
    message.error('AI路线生成失败: ' + (error.message || '未知错误'));
  } finally {
    aiLoading.value = false;
  }
};

const open = (record = {}) => {
  editRef.value.open(record);
};

const openStationManagement = (record) => {
  stationManagementRef.value.open(record);
};

const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 80,
  },
  {
    title: '车次编号',
    dataIndex: 'code',
    key: 'code',
  },
  {
    title: '始发站',
    dataIndex: 'fromStation',
    key: 'fromStation',
  },
  {
    title: '终点站',
    dataIndex: 'toStation',
    key: 'toStation',
  },
  {
    title: '列车类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '总里程(km)',
    dataIndex: 'distance',
    key: 'distance',
  },
  {
    title: '最高速度(km/h)',
    dataIndex: 'maxSpeed',
    key: 'maxSpeed',
  },
  {
    title: '操作',
    key: 'operation',
    fixed: 'right',
    width: 250,
  },
];
</script>

<style lang="less" scoped>
.box {
  height: calc(100vh - 170px);
}

:deep(.ant-form-item) {
  margin-bottom: 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 
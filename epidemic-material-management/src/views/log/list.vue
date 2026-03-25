<template>
  <div class="log-list">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="filterForm.module" placeholder="请输入模块" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="operateTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.operateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="140" />
        <el-table-column prop="operation" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="100" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="executeTime" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.executeTime ? row.executeTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOperateLogList } from '@/api/stats'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const dateRange = ref([])

const filterForm = reactive({
  username: '',
  module: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (filterForm.username) params.username = filterForm.username
    if (filterForm.module) params.module = filterForm.module
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await getOperateLogList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

const handleReset = () => {
  filterForm.username = ''
  filterForm.module = ''
  dateRange.value = []
  pagination.page = 1
  pagination.size = 10
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.log-list {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary, #1f2937);
  }
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
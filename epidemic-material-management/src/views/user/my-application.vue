<template>
  <div class="my-application">
    <div class="page-header">
      <h2 class="page-title">我的申请</h2>
    </div>
    
    <div class="search-bar card-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="申请单号">
          <el-input v-model="searchForm.id" placeholder="请输入申请单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="申请单号" width="140" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="quantity" label="申请数量" width="100" />
        <el-table-column prop="urgency" label="紧急程度" width="90">
          <template #default="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)" size="small">{{ row.urgencyText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途说明" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看详情</el-button>
            <el-button type="primary" link @click="handleTrack(row)">物流追踪</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </div>
    
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRow.id">
        <el-descriptions-item label="申请单号">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ currentRow.materialName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ currentRow.quantity }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="getUrgencyType(currentRow.urgency)">{{ currentRow.urgencyText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用途说明">{{ currentRow.purpose }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ currentRow.address }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentRow.receiver }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.approveTime || '待审核' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow.rejectReason" label="驳回原因">{{ currentRow.rejectReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref({})

const searchForm = reactive({
  id: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 15
})

const tableData = ref([
  { id: 'A2026024', materialName: 'N95医用口罩', quantity: 1000, urgency: 'critical', urgencyText: '紧急', purpose: 'ICU急需N95口罩', address: '市第一医院', receiver: '张三', applyTime: '2026-02-24 10:30:00', status: 'pending', approveTime: '' },
  { id: 'A2026023', materialName: '医用防护服', quantity: 500, urgency: 'urgent', urgencyText: '较急', purpose: '一线医护人员防护', address: '市第一医院', receiver: '张三', applyTime: '2026-02-24 09:45:00', status: 'approved', approveTime: '2026-02-24 11:00:00' },
  { id: 'A2026022', materialName: '84消毒液', quantity: 200, urgency: 'normal', urgencyText: '普通', purpose: '日常消杀使用', address: '市第一医院', receiver: '张三', applyTime: '2026-02-24 09:15:00', status: 'approved', approveTime: '2026-02-24 10:30:00' },
  { id: 'A2026021', materialName: '检测试剂', quantity: 1000, urgency: 'urgent', urgencyText: '较急', purpose: '核酸检测使用', address: '市第一医院', receiver: '张三', applyTime: '2026-02-23 16:20:00', status: 'rejected', approveTime: '2026-02-23 18:00:00', rejectReason: '库存不足' },
  { id: 'A2026020', materialName: '一次性手套', quantity: 500, urgency: 'normal', urgencyText: '普通', purpose: '日常防护', address: '市第一医院', receiver: '张三', applyTime: '2026-02-23 14:00:00', status: 'approved', approveTime: '2026-02-23 16:30:00' }
])

const getUrgencyType = (urgency) => {
  const typeMap = { critical: 'danger', urgent: 'warning', normal: '' }
  return typeMap[urgency] || ''
}

const getStatusType = (status) => {
  const typeMap = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return textMap[status] || ''
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  Object.assign(searchForm, { id: '', status: '' })
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleTrack = (row) => {
  if (row.status === 'approved') {
    ElMessage.info('查看物流追踪信息')
  } else {
    ElMessage.warning('申请未通过审核，无法追踪')
  }
}
</script>

<style scoped lang="scss">
.my-application {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.search-bar {
  margin-bottom: 20px;
}

.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

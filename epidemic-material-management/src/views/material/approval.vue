<template>
  <div class="material-approval">
    <div class="page-header">
      <h2 class="page-title">申领审批</h2>
      <div class="header-actions">
        <el-radio-group v-model="statusFilter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待审核</el-radio-button>
          <el-radio-button label="approved">已通过</el-radio-button>
          <el-radio-button label="rejected">已驳回</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="search-bar card-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="申请单号">
          <el-input v-model="searchForm.id" placeholder="请输入申请单号" clearable />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="searchForm.applicant" placeholder="请输入申请人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="申请单号" width="120" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="unit" label="所属单位" min-width="150" />
        <el-table-column prop="materialName" label="物资名称" min-width="120" />
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="primary" link @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link @click="handleReject(row)">驳回</el-button>
            </template>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const statusFilter = ref('all')
const rejectDialogVisible = ref(false)
const currentRow = ref({})

const searchForm = reactive({
  id: '',
  applicant: ''
})

const rejectForm = reactive({
  reason: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 28
})

const tableData = ref([
  { id: 'A2026024', applicant: '张三', unit: '市第一医院', materialName: 'N95医用口罩', quantity: 1000, urgency: 'critical', urgencyText: '紧急', purpose: 'ICU急需N95口罩', applyTime: '2026-02-24 10:30:00', status: 'pending' },
  { id: 'A2026023', applicant: '李四', unit: '区疾控中心', materialName: '防护服', quantity: 500, urgency: 'urgent', urgencyText: '较急', purpose: '一线医护人员防护', applyTime: '2026-02-24 09:45:00', status: 'pending' },
  { id: 'A2026022', applicant: '王五', unit: '社区服务中心', materialName: '消毒液', quantity: 200, urgency: 'normal', urgencyText: '普通', purpose: '日常消杀使用', applyTime: '2026-02-24 09:15:00', status: 'pending' },
  { id: 'A2026021', applicant: '赵六', unit: '市第二医院', materialName: '检测试剂', quantity: 1000, urgency: 'urgent', urgencyText: '较急', purpose: '核酸检测使用', applyTime: '2026-02-24 08:50:00', status: 'approved' },
  { id: 'A2026020', applicant: '钱七', unit: '市第三医院', materialName: '一次性手套', quantity: 500, urgency: 'normal', urgencyText: '普通', purpose: '日常防护', applyTime: '2026-02-23 16:20:00', status: 'rejected' }
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

const handleApprove = (row) => {
  ElMessageBox.confirm(`确定要通过申请单 "${row.id}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    row.status = 'approved'
    ElMessage.success('审批通过')
  }).catch(() => {})
}

const handleReject = (row) => {
  currentRow.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  currentRow.value.status = 'rejected'
  rejectDialogVisible.value = false
  ElMessage.success('已驳回')
}

const handleView = (row) => {
  ElMessage.info(`查看申请单: ${row.id}`)
}

const handleSizeChange = (size) => {
  pagination.size = size
  handleSearch()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  handleSearch()
}
</script>

<style scoped lang="scss">
.material-approval {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

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
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已发货" value="delivered" />
            <el-option label="已收货" value="received" />
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
            <el-tag :type="getUrgencyType(row.urgency)" size="small">{{ getUrgencyText(row.urgency) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途说明" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看详情</el-button>
            <!-- 已通过/已发货/已收货状态都可以查看物流追踪 -->
            <el-button
              v-if="['approved', 'delivered', 'received'].includes(row.status)"
              type="primary"
              link
              @click="handleTrack(row)"
            >
              物流追踪
            </el-button>
            <!-- 已发货状态显示确认收货按钮 -->
            <el-button
              v-if="row.status === 'delivered'"
              type="success"
              link
              @click="handleConfirmReceive(row)"
            >
              确认收货
            </el-button>
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
          <el-tag :type="getUrgencyType(currentRow.urgency)">{{ getUrgencyText(currentRow.urgency) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用途说明">{{ currentRow.purpose }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ currentRow.address }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentRow.receiver }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(currentRow.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.approveTime ? formatTime(currentRow.approveTime) : '待审核' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow.rejectReason" label="驳回原因">{{ currentRow.rejectReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, confirmReceive } from '@/api/application'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref({})

// 搜索表单状态
const searchForm = reactive({
  id: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

/**
 * 获取紧急程度标签类型
 */
const getUrgencyType = (urgency) => {
  const typeMap = { critical: 'danger', urgent: 'warning', normal: 'info' }
  return typeMap[urgency] || 'info'
}

/**
 * 获取紧急程度显示文本
 */
const getUrgencyText = (urgency) => {
  const textMap = { critical: '紧急', urgent: '较急', normal: '普通' }
  return textMap[urgency] || '普通'
}

/**
 * 获取状态标签类型
 */
const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info',
    delivered: 'primary',
    received: ''
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态显示文本
 */
const getStatusText = (status) => {
  const textMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消',
    delivered: '已发货',
    received: '已收货'
  }
  return textMap[status] || '未知状态'
}

/**
 * 格式化日期时间
 */
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 获取申请记录列表
 * 支持分页和条件筛选
 */
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      id: searchForm.id || undefined,
      status: searchForm.status || undefined
    }
    const res = await getMyApplications(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || '获取申请列表失败')
    }
  } catch (error) {
    console.error('获取申请列表出错', error)
    ElMessage.error('获取申请列表出错')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 * 重置页码并重新加载数据
 */
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  Object.assign(searchForm, { id: '', status: '' })
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchData()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchData()
}

/**
 * 查看详情
 */
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

/**
 * 物流追踪
 * 跳转到追踪页面查看物流详情
 */
const handleTrack = (row) => {
  // 已通过/已发货/已收货状态都可以查看物流追踪
  router.push(`/user/track?id=${row.id}`)
}

/**
 * 确认收货
 * 将申请状态从 delivered 更新为 received
 */
const handleConfirmReceive = (row) => {
  ElMessageBox.confirm('确认已收到物资吗？', '确认收货', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const res = await confirmReceive(row.id)
      if (res.code === 200) {
        ElMessage.success('确认收货成功')
        fetchData() // 刷新列表
      } else {
        ElMessage.error(res.message || '确认收货失败')
      }
    } catch (error) {
      console.error('确认收货出错', error)
      ElMessage.error('确认收货失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
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

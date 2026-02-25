<template>
  <div class="my-donation">
    <div class="page-header">
      <h2 class="page-title">我的捐赠</h2>
    </div>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="8" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.bgColor }">
          <div class="stat-icon-wrap" :style="{ background: stat.iconBg }">
            <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <div class="search-bar card-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="捐赠单号">
          <el-input v-model="searchForm.id" placeholder="请输入捐赠单号" clearable />
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
        <el-table-column prop="id" label="捐赠单号" width="140" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="quantity" label="捐赠数量" width="100" />
        <el-table-column prop="donorUnit" label="捐赠单位" min-width="150" />
        <el-table-column prop="donateTime" label="捐赠时间" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看详情</el-button>
            <el-button type="primary" link v-if="row.status === 'approved'" @click="handleCertificate(row)">证书</el-button>
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
    
    <el-dialog v-model="detailVisible" title="捐赠详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRow.id">
        <el-descriptions-item label="捐赠单号">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ currentRow.materialName }}</el-descriptions-item>
        <el-descriptions-item label="捐赠数量">{{ currentRow.quantity }} {{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="捐赠单位">{{ currentRow.donorUnit }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ currentRow.receiveAddress }}</el-descriptions-item>
        <el-descriptions-item label="捐赠时间">{{ currentRow.donateTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.approveTime || '待审核' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <el-dialog v-model="certificateVisible" title="捐赠证书" width="500px">
      <div class="certificate">
        <div class="cert-header">
          <el-icon :size="48" color="#faad14"><Medal /></el-icon>
          <h2>捐赠荣誉证书</h2>
        </div>
        <div class="cert-content">
          <p>感谢您对疫情防控工作的支持</p>
          <p class="cert-unit">{{ currentRow.donorUnit }}</p>
          <p>于 {{ currentRow.donateTime }} 捐赠</p>
          <p class="cert-material">{{ currentRow.materialName }} {{ currentRow.quantity }}{{ currentRow.unit }}</p>
          <p>特发此证，以表感谢</p>
        </div>
        <div class="cert-footer">
          <p>疫情防控指挥部</p>
          <p>{{ new Date().toLocaleDateString() }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Present, Clock, CircleCheck } from '@element-plus/icons-vue'
import { getMyDonations } from '@/api/donation'
import dayjs from 'dayjs'

const loading = ref(false)
const detailVisible = ref(false)
const certificateVisible = ref(false)
const currentRow = ref({})

const searchForm = reactive({
  id: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const stats = ref([
  { label: '总捐赠次数', value: '0', icon: 'Present', color: '#1890ff', bgColor: '#e6f7ff', iconBg: '#bae7ff' },
  { label: '待审核', value: '0', icon: 'Clock', color: '#faad14', bgColor: '#fffbe6', iconBg: '#ffe7ba' },
  { label: '已通过', value: '0', icon: 'CircleCheck', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' }
])

const tableData = ref([])

const getStatusType = (status) => {
  const typeMap = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return textMap[status] || status
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: searchForm.status || undefined,
      id: searchForm.id || undefined
      // 后端/donation/my接口不需要传donorId，后端会自动从token获取
    }
    // 如果有单号查询，虽然DTO没有直接支持id查询，但可以尝试传给后端看看是否需要支持
    // 目前后端QueryDTO只有 status, donorUnit, donorId, type
    // 如果需要支持单号查询，需要修改后端。这里暂时忽略单号查询或仅在前端过滤(不推荐)
    // 假设后端暂不支持单号精确查询，或者我们可以用 type 字段暂代? 不行。
    // 暂时忽略 searchForm.id，或者修改后端支持。
    // 考虑到用户需求是“修复假数据”，先确保能查出来。
    
    const res = await getMyDonations(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
      
      // 简单更新一下总数统计
      stats.value[0].value = pagination.total.toString()
      // 待审核和已通过无法从分页接口直接获取准确总数，除非后端返回
      // 暂时保持0或不显示，以免误导。或者仅统计当前页（也不对）。
      // 这里为了界面不空，可以显示总数，其他两个暂时显示 '-'
      stats.value[1].value = '-'
      stats.value[2].value = '-'
    }
  } catch (error) {
    console.error('获取捐赠记录失败', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { id: '', status: '' })
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleCertificate = (row) => {
  currentRow.value = row
  certificateVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.my-donation {
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

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
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

.certificate {
  text-align: center;
  padding: 20px;
  border: 2px solid #faad14;
  border-radius: 8px;
  background: linear-gradient(to bottom, #fff, #fffbe6);
}

.cert-header {
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    color: #faad14;
    margin-top: 12px;
  }
}

.cert-content {
  font-size: 16px;
  color: #595959;
  line-height: 2;
  
  .cert-unit {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 16px 0;
  }
  
  .cert-material {
    font-size: 18px;
    font-weight: 500;
    color: #1890ff;
  }
}

.cert-footer {
  margin-top: 32px;
  font-size: 14px;
  color: #8c8c8c;
}
</style>

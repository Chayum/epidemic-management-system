<template>
  <div class="material-inventory">
    <div class="page-header">
      <h2 class="page-title">库存管理</h2>
      <div class="header-actions">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增物资
        </el-button>
      </div>
    </div>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="stat in inventoryStats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.bgColor }">
          <el-icon :size="32" :color="stat.color"><component :is="stat.icon" /></el-icon>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <div class="search-bar card-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="物资名称">
          <el-input v-model="searchForm.name" placeholder="请输入物资名称" clearable />
        </el-form-item>
        <el-form-item label="物资类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="防护物资" value="protective" />
            <el-option label="消杀物资" value="disinfection" />
            <el-option label="检测物资" value="testing" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="库存充足" value="sufficient" />
            <el-option label="库存预警" value="warning" />
            <el-option label="库存不足" value="insufficient" />
          </el-select>
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
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="materialId" label="物资ID" width="120" />
        <el-table-column prop="name" label="物资名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="库存数量" width="100" sortable>
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.quantity < row.threshold }">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="threshold" label="预警阈值" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="有效期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    </div>
    
    <el-dialog v-model="dialogVisible" title="物资详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物资ID">{{ currentItem.materialId }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ currentItem.name }}</el-descriptions-item>
        <el-descriptions-item label="物资类型">{{ getTypeName(currentItem.type) }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentItem.unit }}</el-descriptions-item>
        <el-descriptions-item label="库存数量">{{ currentItem.quantity }}</el-descriptions-item>
        <el-descriptions-item label="预警阈值">{{ currentItem.threshold }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ currentItem.expiryDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem)">{{ getStatusText(currentItem) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Upload, Plus, Search, Refresh, Box, Warning, Check, Close } from '@element-plus/icons-vue'
import { getMaterialList } from '@/api/material'

const loading = ref(false)
const dialogVisible = ref(false)
const currentItem = ref({})

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const inventoryStats = ref([
  { label: '物资种类', value: '0', icon: 'Box', color: '#1890ff', bgColor: '#e6f7ff' },
  { label: '总库存', value: '0', icon: 'Collection', color: '#52c41a', bgColor: '#f6ffed' },
  { label: '库存预警', value: '0', icon: 'Warning', color: '#faad14', bgColor: '#fffbe6' },
  { label: '库存不足', value: '0', icon: 'Close', color: '#f5222d', bgColor: '#fff1f0' }
])

const tableData = ref([])

// 从后端获取物资列表
const fetchMaterialList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await getMaterialList(params)
    if (res.code === 200) {
      tableData.value = res.data.list?.map(item => ({
        materialId: item.id,
        name: item.name,
        type: item.type,
        quantity: item.stock,
        unit: item.unit,
        threshold: item.threshold,
        status: item.status,
        expiryDate: item.expiryDate
      })) || []
      pagination.total = res.data.total || 0
      
      // 更新统计数据
      const warnings = tableData.value.filter(item => item.quantity < item.threshold)
      const insufficient = warnings.filter(item => item.quantity < item.threshold * 0.5)
      inventoryStats.value[0].value = pagination.total.toString()
      inventoryStats.value[1].value = tableData.value.reduce((sum, item) => sum + item.quantity, 0).toLocaleString()
      inventoryStats.value[2].value = warnings.length.toString()
      inventoryStats.value[3].value = insufficient.length.toString()
    }
  } catch (error) {
    console.error('获取物资列表失败:', error)
    ElMessage.error('获取物资列表失败')
  } finally {
    loading.value = false
  }
}

const getTypeName = (type) => {
  const typeMap = { protective: '防护物资', disinfection: '消杀物资', testing: '检测物资' }
  return typeMap[type] || type
}

const getTypeTag = (type) => {
  const tagMap = { protective: '', disinfection: 'success', testing: 'warning' }
  return tagMap[type] || ''
}

const getStatusText = (row) => {
  if (row.quantity < row.threshold) {
    return row.quantity < row.threshold * 0.5 ? '库存不足' : '库存预警'
  }
  return '库存充足'
}

const getStatusType = (row) => {
  if (row.quantity < row.threshold) {
    return row.quantity < row.threshold * 0.5 ? 'danger' : 'warning'
  }
  return 'success'
}

const handleSearch = () => {
  pagination.page = 1
  fetchMaterialList()
}

const handleReset = () => {
  Object.assign(searchForm, { name: '', type: '', status: '' })
  handleSearch()
}

const handleAdd = () => {
  ElMessage.info('新增物资功能')
}

const handleEdit = (row) => {
  ElMessage.info(`编辑物资: ${row.name}`)
}

const handleView = (row) => {
  currentItem.value = row
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除物资 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleImport = () => {
  ElMessage.info('批量导入功能')
}

const handleSizeChange = (size) => {
  pagination.size = size
  fetchMaterialList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchMaterialList()
}

onMounted(() => {
  fetchMaterialList()
})
</script>

<style scoped lang="scss">
.material-inventory {
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

.header-actions {
  display: flex;
  gap: 12px;
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

.text-danger {
  color: #f5222d;
  font-weight: 600;
}
</style>

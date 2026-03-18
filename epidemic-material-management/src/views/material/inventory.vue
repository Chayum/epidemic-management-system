<!--
  库存管理页面组件
  提供物资的列表查询、新增、编辑、删除、导入导出及统计展示功能
-->
<template>
  <div class="material-inventory">
    <!-- 顶部标题与操作栏 -->
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
    
    <!-- 库存统计卡片 -->
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
    
    <!-- 搜索过滤栏 -->
    <div class="search-bar card-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="物资名称">
          <el-input v-model="searchForm.name" placeholder="请输入物资名称" clearable />
        </el-form-item>
        <el-form-item label="物资类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="防护物资" value="protective" />
            <el-option label="消杀物资" value="disinfection" />
            <el-option label="检测物资" value="testing" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
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
    
    <!-- 物资列表表格 -->
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAddStock(row)">添加</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
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
    
    <!-- 详情弹窗 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="editMode === 'add' ? '新增物资' : '编辑物资'" width="620px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="物资名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入物资名称" clearable />
        </el-form-item>
        <el-form-item label="物资类型" prop="type">
          <el-select v-model="editForm.type" placeholder="请选择类型" style="width: 200px">
            <el-option label="防护物资" value="protective" />
            <el-option label="消杀物资" value="disinfection" />
            <el-option label="检测物资" value="testing" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="editForm.specification" placeholder="请输入规格型号" clearable />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="editForm.unit" placeholder="如：个/箱/套" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="editForm.stock" :min="0" :controls="true" style="width: 200px" />
        </el-form-item>
        <el-form-item label="预警阈值" prop="threshold">
          <el-input-number v-model="editForm.threshold" :min="0" :controls="true" style="width: 200px" />
        </el-form-item>
        <el-form-item label="存放仓库">
          <el-input v-model="editForm.warehouse" placeholder="请输入仓库/库区" clearable />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加库存弹窗 -->
    <el-dialog v-model="addStockDialogVisible" title="添加库存" width="500px">
      <el-form ref="addStockFormRef" :model="addStockForm" :rules="addStockRules" label-width="100px">
        <el-form-item label="物资名称">
          <span>{{ addStockForm.materialName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ addStockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="addStockForm.quantity" :min="1" :controls="true" style="width: 200px" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="addStockForm.supplier" placeholder="请输入供应商" clearable />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addStockForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addStockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addStockLoading" @click="handleSubmitAddStock">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Upload, Plus, Search, Refresh, Box, Warning, Check, Close } from '@element-plus/icons-vue'
import { getMaterialList, addMaterial, updateMaterial, deleteMaterial, createStockOrder } from '@/api/material'

const loading = ref(false)
const dialogVisible = ref(false)
const currentItem = ref({})
const editDialogVisible = ref(false)
const editMode = ref('add') // 'add' 或 'edit'
const saving = ref(false)
const editFormRef = ref(null)
const addStockDialogVisible = ref(false)
const addStockFormRef = ref(null)
const addStockLoading = ref(false)

const addStockForm = reactive({
  materialId: '',
  materialName: '',
  currentStock: 0,
  quantity: 1,
  supplier: '',
  remark: ''
})

const addStockRules = {
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'change' }]
}

// 搜索表单状态
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

// 分页状态
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 顶部统计数据（Mock初始值）
const inventoryStats = ref([
  { label: '物资种类', value: '0', icon: 'Box', color: '#1890ff', bgColor: '#e6f7ff' },
  { label: '总库存', value: '0', icon: 'Collection', color: '#52c41a', bgColor: '#f6ffed' },
  { label: '库存预警', value: '0', icon: 'Warning', color: '#faad14', bgColor: '#fffbe6' },
  { label: '库存不足', value: '0', icon: 'Close', color: '#f5222d', bgColor: '#fff1f0' }
])

const tableData = ref([])

// 编辑表单状态
const editForm = reactive({
  id: '',
  name: '',
  type: '',
  specification: '',
  unit: '',
  stock: 0,
  threshold: 0,
  warehouse: '',
  description: ''
})

// 表单校验规则
const editRules = {
  name: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入预警阈值', trigger: 'change' }]
}

/**
 * 获取物资列表
 * 根据搜索条件和分页参数查询，并更新统计数据
 */
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
      // 转换后端数据格式以适配前端显示
      tableData.value = res.data.list?.map(item => ({
        materialId: item.id,
        id: item.id,
        name: item.name,
        type: item.type,
        quantity: item.stock,
        unit: item.unit,
        threshold: item.threshold,
        status: item.status,
        specification: item.specification,
        warehouse: item.warehouse,
        description: item.description,
        expiryDate: item.expiryDate
      })) || []
      pagination.total = res.data.total || 0
      
      // 前端简易统计（仅针对当前页，实际生产环境建议由后端返回统计数据）
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

// 辅助函数：获取类型中文名称
const getTypeName = (type) => {
  const typeMap = { protective: '防护物资', disinfection: '消杀物资', testing: '检测物资' }
  return typeMap[type] || type
}

// 辅助函数：获取类型标签样式
const getTypeTag = (type) => {
  const tagMap = { protective: '', disinfection: 'success', testing: 'warning' }
  return tagMap[type] || ''
}

// 辅助函数：根据库存与阈值计算状态文本
const getStatusText = (row) => {
  if (row.quantity < row.threshold) {
    return row.quantity < row.threshold * 0.5 ? '库存不足' : '库存预警'
  }
  return '库存充足'
}

// 辅助函数：根据库存与阈值计算状态标签颜色
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

// 打开新增弹窗
const handleAdd = () => {
  editMode.value = 'add'
  // 重置表单
  Object.assign(editForm, {
    id: '',
    name: '',
    type: '',
    specification: '',
    unit: '',
    stock: 0,
    threshold: 0,
    warehouse: '',
    description: ''
  })
  editDialogVisible.value = true
}

// 打开编辑弹窗（回填数据）
const handleEdit = (row) => {
  editMode.value = 'edit'
  Object.assign(editForm, {
    id: row.materialId,
    name: row.name,
    type: row.type,
    specification: row.specification || '',
    unit: row.unit || '',
    stock: row.quantity ?? 0,
    threshold: row.threshold ?? 0,
    warehouse: row.warehouse || '',
    description: row.description || ''
  })
  editDialogVisible.value = true
}

const handleView = (row) => {
  currentItem.value = row
  dialogVisible.value = true
}

// 删除操作
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除物资 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMaterial(row.materialId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchMaterialList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 打开添加库存弹窗
const handleAddStock = (row) => {
  Object.assign(addStockForm, {
    materialId: row.materialId,
    materialName: row.name,
    currentStock: row.quantity,
    quantity: 1,
    supplier: '',
    remark: ''
  })
  addStockDialogVisible.value = true
}

// 提交添加库存
const handleSubmitAddStock = async () => {
  if (!addStockFormRef.value) return
  await addStockFormRef.value.validate(async (valid) => {
    if (!valid) return
    addStockLoading.value = true
    try {
      const payload = {
        type: 'inbound',
        sourceType: 'manual',
        supplier: addStockForm.supplier || undefined,
        remark: addStockForm.remark || undefined,
        items: [{
          materialId: addStockForm.materialId,
          quantity: addStockForm.quantity
        }]
      }
      const res = await createStockOrder(payload)
      if (res.code === 200) {
        ElMessage.success('添加库存成功')
        addStockDialogVisible.value = false
        fetchMaterialList()
      } else {
        ElMessage.error(res.message || '添加库存失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('添加库存失败')
    } finally {
      addStockLoading.value = false
    }
  })
}

// 保存操作（新增或更新）
const handleSave = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        id: editForm.id || undefined,
        name: editForm.name,
        type: editForm.type,
        specification: editForm.specification || undefined,
        unit: editForm.unit,
        stock: editForm.stock,
        threshold: editForm.threshold,
        warehouse: editForm.warehouse || undefined,
        description: editForm.description || undefined
      }
      const res = editMode.value === 'add' ? await addMaterial(payload) : await updateMaterial(payload)
      if (res.code === 200) {
        ElMessage.success(editMode.value === 'add' ? '新增成功' : '更新成功')
        editDialogVisible.value = false
        fetchMaterialList()
      } else {
        ElMessage.error(res.message || (editMode.value === 'add' ? '新增失败' : '更新失败'))
      }
    } catch (e) {
      console.error(e)
      ElMessage.error(editMode.value === 'add' ? '新增失败' : '更新失败')
    } finally {
      saving.value = false
    }
  })
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

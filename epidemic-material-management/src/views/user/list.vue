<template>
  <div class="user-list-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <span class="total-count">共 {{ pagination.total }} 条记录</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择" clearable style="width: 140px">
            <el-option label="管理员" value="admin" />
            <el-option label="申请方" value="applicant" />
            <el-option label="捐赠方" value="donor" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
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

    <!-- 批量操作工具栏 -->
    <div class="toolbar" v-if="selectedRows.length > 0">
      <div class="selection-info">
        <el-checkbox v-model="isAllSelected" :indeterminate="isIndeterminate" @change="handleSelectAll">
          {{ selectedRows.length }} 项已选择
        </el-checkbox>
      </div>
      <div class="batch-actions">
        <el-button size="small" @click="handleBatchEnable">
          <el-icon><CircleCheck /></el-icon>批量启用
        </el-button>
        <el-button size="small" @click="handleBatchDisable">
          <el-icon><CircleClose /></el-icon>批量禁用
        </el-button>
        <el-button size="small" type="danger" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="selection" width="50" fixed="left" />
        <el-table-column prop="id" label="ID" width="70" fixed="left" />
        <el-table-column prop="username" label="用户名" width="120" fixed="left" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="unit" label="所属单位" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="active"
              inactive-value="inactive"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空数据提示 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无用户数据" />
    </div>

    <!-- 分页控件 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="用户ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRow.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="所属单位" :span="2">{{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleType(currentRow.role)">{{ getRoleName(currentRow.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 'active' ? 'success' : 'info'">
            {{ currentRow.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatTime(currentRow.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentRow); detailVisible = false">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="formVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="dialogTitle === '编辑用户'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入所属单位" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="申请方" value="applicant" />
            <el-option label="捐赠方" value="donor" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="dialogTitle === '编辑用户'">
          <el-switch
            v-model="form.status"
            active-value="active"
            inactive-value="inactive"
            active-text="启用"
            inactive-text="禁用"
            inline-prompt
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogTitle === '新增用户'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Delete, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, updateUserStatus, batchUpdateStatus, batchDelete } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  phone: '',
  role: '',
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
const loading = ref(false)
const tableRef = ref(null)

// 选中行
const selectedRows = ref([])

// 表单对话框
const formVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const submitLoading = ref(false)
const currentRow = ref(null)

// 表单数据
const form = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  unit: '',
  role: '',
  password: '',
  status: 'active'
})

// 表单验证规则
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  unit: [{ required: true, message: '请输入所属单位', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 全选状态
const isAllSelected = computed(() => tableData.value.length > 0 && selectedRows.value.length === tableData.value.length)
const isIndeterminate = computed(() => selectedRows.value.length > 0 && selectedRows.value.length < tableData.value.length)

// 角色映射
const getRoleName = (role) => {
  const roleMap = {
    admin: '管理员',
    applicant: '申请方',
    donor: '捐赠方'
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    admin: 'danger',
    applicant: 'success',
    donor: 'info'
  }
  return typeMap[role] || 'info'
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await getUserList(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, { username: '', name: '', phone: '', role: '', status: '' })
  handleSearch()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 全选
const handleSelectAll = (checked) => {
  if (checked) {
    tableRef.value.toggleAllSelection()
  } else {
    tableRef.value.clearSelection()
  }
}

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null,
    username: '',
    name: '',
    phone: '',
    unit: '',
    role: '',
    password: '',
    status: 'active'
  })
  formVisible.value = true
}

// 查看用户
const handleView = (row) => {
  currentRow.value = { ...row }
  detailVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...row, password: '' })
  formVisible.value = true
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.name || row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success(`${row.status === 'active' ? '启用' : '禁用'}成功`)
  } catch (error) {
    row.status = row.status === 'active' ? 'inactive' : 'active'
    ElMessage.error('状态更新失败')
  }
}

// 批量启用
const handleBatchEnable = async () => {
  const ids = selectedRows.value.map(row => row.id)
  try {
    await batchUpdateStatus(ids, 'active')
    ElMessage.success('批量启用成功')
    fetchUserList()
  } catch (error) {
    ElMessage.error('批量启用失败')
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  const ids = selectedRows.value.map(row => row.id)
  try {
    await batchUpdateStatus(ids, 'inactive')
    ElMessage.success('批量禁用成功')
    fetchUserList()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

// 批量删除
const handleBatchDelete = () => {
  const count = selectedRows.value.length
  ElMessageBox.confirm(`确定要删除选中的 ${count} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ids = selectedRows.value.map(row => row.id)
    try {
      await batchDelete(ids)
      ElMessage.success('批量删除成功')
      fetchUserList()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogTitle.value === '新增用户') {
          await addUser(form)
          ElMessage.success('新增成功')
        } else {
          await updateUser(form)
          ElMessage.success('修改成功')
        }
        formVisible.value = false
        fetchUserList()
      } catch (error) {
        ElMessage.error(dialogTitle.value === '新增用户' ? '新增失败' : '修改失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  fetchUserList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchUserList()
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="scss">
.user-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;

  .header-left {
    display: flex;
    align-items: baseline;
    gap: 12px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .total-count {
    font-size: 14px;
    color: #909399;
  }
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 15px;

  .search-form {
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ecf5ff;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 15px;

  .selection-info {
    font-size: 14px;
    color: #409eff;
  }

  .batch-actions {
    display: flex;
    gap: 10px;
  }
}

.table-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 300px;

  :deep(.el-table) {
    font-size: 14px;
  }

  :deep(.el-switch) {
    --el-switch-on-color: #67c23a;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;

  :deep(.el-pagination) {
    --el-pagination-bg-color: #409eff;
  }
}

// 响应式布局
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .search-section {
    :deep(.el-form) {
      display: block;
      
      .el-form-item {
        display: block;
        margin-bottom: 10px;
        
        .el-form-item__content {
          display: block;
        }
      }
    }
  }

  .toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .batch-actions {
    flex-wrap: wrap;
  }
}
</style>

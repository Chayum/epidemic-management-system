<template>
  <div class="material-track">
    <div class="page-header">
      <h2 class="page-title">物资追踪</h2>
    </div>

    <!-- 列表区域 -->
    <div class="card-container">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="queryParams.status"
          placeholder="按状态筛选"
          clearable
          style="width: 150px"
          @change="handleFilter"
        >
          <el-option label="已通过" value="approved" />
          <el-option label="已发货" value="delivered" />
          <el-option label="已收货" value="received" />
        </el-select>
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索申请单号/物资名称"
          clearable
          style="width: 250px"
          @keyup.enter="handleFilter"
          @clear="handleFilter"
        >
          <template #append>
            <el-button @click="handleFilter">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 列表 -->
      <el-table
        v-loading="loading"
        :data="applicationList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="申请单号" width="180" />
        <!-- 管理员端显示申请人信息 -->
        <el-table-column v-if="isAdmin" prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="materialName" label="物资名称" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="80">
          <template #default="{ row }">
            {{ row.quantity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isAdmin ? '160' : '100'" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewTrack(row)">
              查看详情
            </el-button>
            <!-- 管理员更新状态按钮 -->
            <el-button
              v-if="isAdmin && (row.status === 'approved' || row.status === 'delivered')"
              type="success"
              link
              @click="handleUpdateStatus(row)"
            >
              更新状态
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 物流详情弹窗 -->
    <el-dialog
      v-model="trackDialogVisible"
      title="物流详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="trackInfo" class="track-dialog-content">
        <!-- 申请信息 -->
        <div class="info-section">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="申请单号">{{ trackInfo.applicationId }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(trackInfo.status)">{{ getStatusText(trackInfo.status) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 物流进度 -->
        <div class="timeline-section">
          <h4 class="section-title">物流进度</h4>
          <el-timeline v-if="trackInfo.tracks && trackInfo.tracks.length > 0">
            <el-timeline-item
              v-for="(activity, index) in trackInfo.tracks"
              :key="index"
              :timestamp="formatDate(activity.time)"
              :type="index === 0 ? 'primary' : 'info'"
              :hollow="index !== 0"
            >
              <h4>{{ getStatusText(activity.status) }}</h4>
              <p>{{ activity.description }}</p>
              <p v-if="activity.operator" class="operator">操作人: {{ activity.operator }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无物流记录" :image-size="80" />
        </div>
      </div>
      <template #footer>
        <el-button @click="trackDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 更新状态弹窗（管理员） -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新物流状态"
      width="500px"
      destroy-on-close
    >
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="申请单号">
          <span>{{ statusForm.id }}</span>
        </el-form-item>
        <el-form-item label="物资名称">
          <span>{{ statusForm.materialName }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(statusForm.currentStatus)">{{ getStatusText(statusForm.currentStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="更新为">
          <el-radio-group v-model="statusForm.newStatus">
            <!-- 根据当前状态决定可选的新状态 -->
            <el-radio v-if="statusForm.currentStatus === 'approved'" label="delivered">已发货</el-radio>
            <el-radio v-if="statusForm.currentStatus === 'delivered'" label="received">已收货</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMyApplications, getApplicationList, getTrackInfo, updateApplicationStatus } from '@/api/application'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

// 判断是否为管理员
const isAdmin = computed(() => userStore.userRole === 'admin')

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  status: '',
  keyword: ''
})

// 列表数据
const applicationList = ref([])
const total = ref(0)
const loading = ref(false)

// 物流详情
const trackDialogVisible = ref(false)
const trackInfo = ref(null)

// 更新状态
const statusDialogVisible = ref(false)
const submitting = ref(false)
const statusForm = reactive({
  id: '',
  materialName: '',
  currentStatus: '',
  newStatus: ''
})

/**
 * 获取申请列表
 * 管理员：查询所有用户的申请
 * 普通用户：仅查询自己的申请
 * 仅查询已通过/已发货/已收货的订单
 */
const fetchList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: queryParams.page,
      size: queryParams.size
    }

    // 状态筛选：单状态用 status，多状态用 statuses
    if (queryParams.status) {
      params.status = queryParams.status
    } else {
      // 默认查询物流相关状态（多状态查询）
      params.statuses = 'approved,delivered,received'
    }

    // 根据角色调用不同的 API
    let res
    if (isAdmin.value) {
      // 管理员查询所有申请
      res = await getApplicationList(params)
    } else {
      // 普通用户查询自己的申请
      res = await getMyApplications(params)
    }

    if (res.code === 200) {
      applicationList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

/**
 * 筛选处理
 */
const handleFilter = () => {
  queryParams.page = 1
  fetchList()
}

/**
 * 查看物流详情
 */
const handleViewTrack = async (row) => {
  try {
    const res = await getTrackInfo(row.id)
    if (res.code === 200) {
      trackInfo.value = res.data
      trackDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取物流信息失败', error)
    ElMessage.error('获取物流信息失败')
  }
}

/**
 * 打开更新状态弹窗
 */
const handleUpdateStatus = (row) => {
  statusForm.id = row.id
  statusForm.materialName = row.materialName
  statusForm.currentStatus = row.status
  // 默认选择下一个状态
  if (row.status === 'approved') {
    statusForm.newStatus = 'delivered'
  } else if (row.status === 'delivered') {
    statusForm.newStatus = 'received'
  }
  statusDialogVisible.value = true
}

/**
 * 确认更新状态
 */
const confirmUpdateStatus = async () => {
  submitting.value = true
  try {
    const res = await updateApplicationStatus(statusForm.id, statusForm.newStatus)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      statusDialogVisible.value = false
      // 刷新列表
      fetchList()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 获取状态标签类型
 */
const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'primary',
    delivered: 'success',
    received: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已通过',
    delivered: '已发货',
    received: '已收货',
    rejected: '已驳回'
  }
  return map[status] || status
}

/**
 * 格式化日期
 */
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  // 如果从其他页面带参数过来，直接查询该订单
  if (route.query.id) {
    handleViewTrack({ id: route.query.id })
  }
  // 获取列表
  fetchList()
})
</script>

<style scoped lang="scss">
.material-track {
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

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.track-dialog-content {
  .info-section {
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 3px solid #1890ff;
  }

  .timeline-section {
    padding: 0 10px;
  }

  .operator {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }
}
</style>

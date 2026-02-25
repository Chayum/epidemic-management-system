<template>
  <div class="material-track">
    <div class="page-header">
      <h2 class="page-title">物资追踪</h2>
    </div>

    <div class="card-container">
      <div class="search-bar">
        <el-input 
          v-model="searchId" 
          placeholder="请输入申请单号查询" 
          style="width: 300px" 
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>查询
            </el-button>
          </template>
        </el-input>
      </div>

      <div v-if="trackInfo" class="track-content">
        <div class="info-section">
          <el-descriptions title="申请信息" :column="2" border>
            <el-descriptions-item label="申请单号">{{ trackInfo.applicationId }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(trackInfo.status)">{{ getStatusText(trackInfo.status) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="timeline-section">
          <h3 class="section-title">物流进度</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in trackInfo.tracks"
              :key="index"
              :timestamp="formatDate(activity.time)"
              :type="index === 0 ? 'primary' : ''"
              :hollow="index === 0"
            >
              <h4>{{ activity.status }}</h4>
              <p>{{ activity.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <el-empty v-else description="请输入申请单号查询物流信息" :image-size="120" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getTrackInfo } from '@/api/application'
import dayjs from 'dayjs'

const route = useRoute()
const searchId = ref('')
const trackInfo = ref(null)
const loading = ref(false)

const handleSearch = async () => {
  if (!searchId.value) {
    ElMessage.warning('请输入申请单号')
    return
  }
  
  loading.value = true
  try {
    const res = await getTrackInfo(searchId.value)
    if (res.code === 200) {
      trackInfo.value = res.data
    }
  } catch (error) {
    console.error('查询失败', error)
    trackInfo.value = null
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'primary',
    shipped: 'success',
    delivered: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已批准',
    shipped: '运输中',
    delivered: '已送达',
    rejected: '已驳回'
  }
  return map[status] || status
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  if (route.query.id) {
    searchId.value = route.query.id
    handleSearch()
  }
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

.search-bar {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.track-content {
  max-width: 800px;
  margin: 0 auto;
}

.info-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #1890ff;
}

.timeline-section {
  padding: 0 20px;
}
</style>

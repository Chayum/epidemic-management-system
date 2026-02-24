<template>
  <div class="material-track">
    <div class="page-header">
      <h2 class="page-title">物资追踪</h2>
    </div>
    
    <div class="search-section card-container">
      <el-form :inline="true">
        <el-form-item label="申请单号">
          <el-input v-model="searchId" placeholder="请输入申请单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div v-if="trackInfo" class="track-content">
      <div class="track-header card-container">
        <div class="track-info">
          <div class="info-item">
            <span class="label">申请单号</span>
            <span class="value">{{ trackInfo.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请人</span>
            <span class="value">{{ trackInfo.applicant }}</span>
          </div>
          <div class="info-item">
            <span class="label">物资名称</span>
            <span class="value">{{ trackInfo.materialName }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请数量</span>
            <span class="value">{{ trackInfo.quantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前状态</span>
            <el-tag :type="getStatusType(trackInfo.status)" size="large">{{ getStatusText(trackInfo.status) }}</el-tag>
          </div>
        </div>
      </div>
      
      <div class="track-timeline card-container">
        <h3 class="section-title">物资流转时间轴</h3>
        <div class="timeline">
          <div v-for="(item, index) in trackInfo.timeline" :key="index" class="timeline-item" :class="{ completed: item.completed, active: item.active }">
            <div class="timeline-dot">
              <el-icon v-if="item.completed"><Check /></el-icon>
              <el-icon v-else-if="item.active"><Loading /></el-icon>
            </div>
            <div class="timeline-content">
              <div class="timeline-title">{{ item.title }}</div>
              <div class="timeline-desc">{{ item.desc }}</div>
              <div class="timeline-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="track-detail card-container">
        <h3 class="section-title">详细信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请时间">{{ trackInfo.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ trackInfo.approveTime || '待审核' }}</el-descriptions-item>
          <el-descriptions-item label="出库时间">{{ trackInfo.outboundTime || '待出库' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ trackInfo.deliveryTime || '待发货' }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ trackInfo.receiveTime || '待收货' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ trackInfo.address }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
    
    <div v-else class="empty-state card-container">
      <el-empty description="请输入申请单号进行查询">
        <el-input v-model="searchId" placeholder="请输入申请单号" style="width: 300px" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check, Loading } from '@element-plus/icons-vue'

const searchId = ref('')
const trackInfo = ref(null)

const getStatusType = (status) => {
  const typeMap = { pending: 'warning', approved: 'success', outbound: 'primary', delivered: 'success', received: 'success' }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = { pending: '待审核', approved: '已审核', outbound: '已出库', delivered: '已发货', received: '已收货' }
  return textMap[status] || ''
}

const handleSearch = () => {
  if (!searchId.value) {
    ElMessage.warning('请输入申请单号')
    return
  }
  
  if (searchId.value === 'A2026024') {
    trackInfo.value = {
      id: 'A2026024',
      applicant: '张三',
      materialName: 'N95医用口罩',
      quantity: 1000,
      status: 'outbound',
      applyTime: '2026-02-24 10:30:00',
      approveTime: '2026-02-24 11:15:00',
      outboundTime: '2026-02-24 14:20:00',
      deliveryTime: '',
      receiveTime: '',
      address: '市第一医院',
      timeline: [
        { title: '提交申请', desc: '用户提交物资申请单', time: '2026-02-24 10:30:00', completed: true, active: false },
        { title: '审核审批', desc: '管理员审核通过申请', time: '2026-02-24 11:15:00', completed: true, active: false },
        { title: '物资出库', desc: '物资从仓库出库', time: '2026-02-24 14:20:00', completed: true, active: true },
        { title: '物流发货', desc: '物资配送中', time: '', completed: false, active: false },
        { title: '确认收货', desc: '申领单位确认收货', time: '', completed: false, active: false }
      ]
    }
    ElMessage.success('查询成功')
  } else {
    trackInfo.value = null
    ElMessage.warning('未找到该申请单信息')
  }
}
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

.search-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.track-header {
  margin-bottom: 20px;
}

.track-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .label {
    color: #8c8c8c;
  }
  
  .value {
    font-weight: 500;
    color: #1a1a1a;
  }
}

.track-timeline {
  margin-bottom: 20px;
}

.timeline {
  position: relative;
  padding-left: 30px;
  
  &::before {
    content: '';
    position: absolute;
    left: 7px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #e8e8e8;
  }
}

.timeline-item {
  position: relative;
  padding-bottom: 30px;
  
  &:last-child {
    padding-bottom: 0;
  }
  
  &.completed .timeline-dot {
    background: #52c41a;
    border-color: #52c41a;
    color: #fff;
  }
  
  &.active .timeline-dot {
    background: #1890ff;
    border-color: #1890ff;
    color: #fff;
    animation: pulse 2s infinite;
  }
}

.timeline-dot {
  position: absolute;
  left: -26px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #d9d9d9;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.timeline-content {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.timeline-desc {
  font-size: 13px;
  color: #595959;
  margin-bottom: 8px;
}

.timeline-time {
  font-size: 12px;
  color: #8c8c8c;
}

.track-detail {
  margin-bottom: 20px;
}

.empty-state {
  padding: 60px 0;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(24, 144, 255, 0);
  }
}
</style>

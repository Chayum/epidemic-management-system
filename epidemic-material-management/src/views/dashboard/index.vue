<!--
  管理端仪表盘 - 合并版
  整合控制台与数据大屏功能，统一展示统计卡片、趋势图表、库存预警、实时动态及操作日志
-->
<template>
  <div class="dashboard">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，管理员</h1>
        <p class="welcome-desc">今天是 {{ currentDate }}，疫情防控物资调度管理系统正常运行中</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="$router.push('/material/donation')">
          <el-icon><Plus /></el-icon>物资捐赠
        </el-button>
        <el-button @click="$router.push('/material/apply')">
          <el-icon><Box /></el-icon>物资申领
        </el-button>
        <el-button circle :loading="loading" @click="refreshData" title="刷新数据">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-icon-wrap" :class="'stat-icon-' + stat.type">
            <el-icon :size="26" color="#fff"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trendType">
            <el-icon><component :is="stat.trendType === 'up' ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
            {{ Math.abs(stat.trend) }}%
          </div>
          <div class="stat-bg-icon">
            <el-icon :size="100"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 图表区域：趋势图与饼图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <div class="card-container">
          <div class="card-header">
            <h3><el-icon><TrendCharts /></el-icon> 物资出入库趋势</h3>
            <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-container" ref="trendChartRef"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="card-container">
          <div class="card-header">
            <h3><el-icon><PieChart /></el-icon> 物资类型分布</h3>
          </div>
          <div class="chart-container" ref="pieChartRef"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 信息面板：待审核申请 + 库存预警 + 实时动态 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 待审核申请 -->
      <el-col :xs="24" :lg="8">
        <div class="card-container info-panel">
          <div class="card-header">
            <h3>待审核申请</h3>
            <el-button type="primary" link @click="$router.push('/material/approval')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <el-table :data="pendingApplications" style="width: 100%" size="small">
            <el-table-column prop="id" label="单号" width="110" />
            <el-table-column prop="applicant" label="申请人" show-overflow-tooltip />
            <el-table-column prop="material" label="物资" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="60" />
            <el-table-column prop="time" label="时间" width="60" />
          </el-table>
        </div>
      </el-col>
      
      <!-- 库存预警 -->
      <el-col :xs="24" :lg="8">
        <div class="card-container info-panel">
          <div class="card-header">
            <h3><el-icon><Warning /></el-icon> 库存预警</h3>
            <el-badge :value="warningList.length" :max="99" type="danger">
              <el-button type="primary" link @click="$router.push('/material/inventory')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-badge>
          </div>
          <div class="warning-list">
            <div v-for="item in warningList" :key="item.materialId || item.id" class="warning-item">
              <div class="warning-icon" :class="item.warningLevel || item.level">
                <el-icon><WarningFilled /></el-icon>
              </div>
              <div class="warning-content">
                <div class="warning-name">{{ item.materialName || item.name }}</div>
                <div class="warning-progress">
                  <el-progress 
                    :percentage="getStockPercentage(item)" 
                    :status="(item.warningLevel === 'high' || item.level === 'danger') ? 'exception' : 'warning'"
                    :stroke-width="6"
                  />
                  <div class="warning-detail">
                    当前库存：{{ item.currentStock ?? item.stock }} / 阈值：{{ item.warningThreshold ?? item.threshold }}
                  </div>
                </div>
              </div>
            </div>
            <div v-if="warningList.length === 0" class="empty-state-small">
              <el-empty description="暂无预警信息" :image-size="60" />
            </div>
          </div>
        </div>
      </el-col>
      
      <!-- 实时动态 -->
      <el-col :xs="24" :lg="8">
        <div class="card-container info-panel">
          <div class="card-header">
            <h3><el-icon><Bell /></el-icon> 实时动态</h3>
            <el-tag size="small" type="success" effect="dark">Live</el-tag>
          </div>
          <div class="activity-list">
            <div v-for="activity in activities" :key="activity.id" class="activity-item">
              <div class="activity-icon" :class="activity.type">
                <el-icon><component :is="activity.type === 'application' ? 'Document' : 'ChatDotRound'" /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
                <div class="activity-time">{{ formatTime(activity.createTime) }}</div>
              </div>
              <el-tag :type="getStatusType(activity.status)" size="small">{{ getStatusText(activity.status) }}</el-tag>
            </div>
            <div v-if="activities.length === 0" class="empty-state-small">
              <el-empty description="暂无动态" :image-size="60" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 日志区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <div class="card-container">
          <div class="card-header">
            <h3>近期操作日志</h3>
            <el-button type="primary" link>查看全部</el-button>
          </div>
          <el-table :data="operationLogs" style="width: 100%">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="操作人" width="120" />
            <el-table-column prop="action" label="操作类型" width="120" />
            <el-table-column prop="detail" label="操作详情" />
            <el-table-column prop="ip" label="IP地址" width="140" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getDashboardData, getTrendData, getMaterialStats } from '@/api/stats'
import { getApplicationList } from '@/api/application'
import {
  Plus, Box, Refresh, ArrowUp, ArrowDown, ArrowRight,
  WarningFilled, Warning, Bell, TrendCharts, PieChart,
  Document, ChatDotRound, Download, Upload
} from '@element-plus/icons-vue'

// 当前日期
const currentDate = computed(() => dayjs().format('YYYY年MM月DD日'))

// 图表周期
const trendPeriod = ref('week')

// 图表 DOM 引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)

// 加载状态
const loading = ref(false)

// 图表实例
let trendChart = null
let pieChart = null
let refreshTimer = null

// 统计卡片数据
const stats = ref([
  { label: '物资总量', value: '0', icon: 'Box', type: 'total', trend: 0, trendType: 'up' },
  { label: '今日入库', value: '0', icon: 'Download', type: 'inbound', trend: 0, trendType: 'up' },
  { label: '今日出库', value: '0', icon: 'Upload', type: 'outbound', trend: 0, trendType: 'up' },
  { label: '库存预警', value: '0', icon: 'Warning', type: 'warning', trend: 0, trendType: 'down' }
])

// 列表数据
const pendingApplications = ref([])
const warningList = ref([])
const activities = ref([])
const operationLogs = ref([])

/**
 * 加载仪表盘主数据（综合 API）
 */
const loadDashboardData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      const data = res.data

      // 更新统计卡片
      if (data.coreMetrics) {
        stats.value[0].value = (data.coreMetrics.totalMaterials || 0).toLocaleString()
        stats.value[1].value = (data.coreMetrics.todayInbound || 0).toLocaleString()
        stats.value[2].value = (data.coreMetrics.todayOutbound || 0).toLocaleString()
        stats.value[3].value = (data.coreMetrics.lowStockItems || 0).toString()
      }

      // 更新预警列表
      warningList.value = data.warningList || []

      // 更新实时动态
      activities.value = data.realtimeActivities || []

      // 更新趋势图
      if (data.trendData) {
        updateTrendChart(data.trendData)
      }
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }

  // 并行加载待审核申请
  try {
    const appRes = await getApplicationList({ page: 1, size: 4, status: 'pending' })
    if (appRes.code === 200) {
      pendingApplications.value = appRes.data.list?.map(item => ({
        id: item.id,
        applicant: item.applicant,
        material: item.materialName,
        quantity: item.quantity,
        time: dayjs(item.applyTime).format('HH:mm')
      })) || []
    }
  } catch (error) {
    console.error('获取待审核申请失败:', error)
  }
}

/**
 * 加载趋势数据（按周期）
 */
const loadTrendData = async () => {
  try {
    const res = await getTrendData(trendPeriod.value)
    if (res.code === 200) {
      updateTrendChart(res.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

/**
 * 更新趋势图配置
 */
const updateTrendChart = (trendData) => {
  if (!trendChart) return

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      textStyle: { color: '#1f2937' }
    },
    legend: {
      data: ['入库', '出库'],
      bottom: 10,
      textStyle: { color: '#6b7280' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.dates || [],
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: { color: '#6b7280' }
    },
    series: [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        data: trendData.inbound || [],
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.25)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
          ])
        }
      },
      {
        name: '出库',
        type: 'line',
        smooth: true,
        data: trendData.outbound || [],
        itemStyle: { color: '#06b6d4' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(6, 182, 212, 0.25)' },
            { offset: 1, color: 'rgba(6, 182, 212, 0.02)' }
          ])
        }
      }
    ]
  }

  trendChart.setOption(option)
}

/**
 * 初始化趋势折线图
 */
const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
}

/**
 * 初始化物资类型分布饼图
 */
const initPieChart = async () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)

  // 尝试从 API 获取饼图数据
  let pieData = [
    { value: 1048, name: '防护物资', itemStyle: { color: '#3b82f6' } },
    { value: 735, name: '消杀物资', itemStyle: { color: '#06b6d4' } },
    { value: 580, name: '检测物资', itemStyle: { color: '#10b981' } },
    { value: 484, name: '医疗设备', itemStyle: { color: '#f59e0b' } },
    { value: 300, name: '其他物资', itemStyle: { color: '#8b5cf6' } }
  ]

  try {
    const res = await getMaterialStats()
    if (res.code === 200 && res.data.typeStats?.length) {
      const colorMap = {
        '防护物资': '#3b82f6',
        '消杀物资': '#06b6d4',
        '检测物资': '#10b981',
        '医疗设备': '#f59e0b',
        '其他物资': '#8b5cf6'
      }
      pieData = res.data.typeStats.map(item => ({
        value: item.stock || item.count || 0,
        name: item.name,
        itemStyle: { color: colorMap[item.name] || '#8b5cf6' }
      }))
    }
  } catch (error) {
    console.error('获取物资分布失败, 使用默认数据:', error)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      textStyle: { color: '#1f2937' }
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#6b7280' }
    },
    series: [
      {
        name: '物资类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: pieData
      }
    ]
  }

  pieChart.setOption(option)
}

/**
 * 手动刷新
 */
const refreshData = () => {
  loadDashboardData()
  loadTrendData()
}

/**
 * 窗口 resize 处理
 */
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

// ===== 从 screen.vue 移植的工具函数 =====

/**
 * 相对时间格式化
 */
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

/**
 * 状态类型映射
 */
const getStatusType = (status) => {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger', allocated: 'info' }
  return map[status] || 'info'
}

/**
 * 状态文本映射
 */
const getStatusText = (status) => {
  const map = { pending: '待审核', approved: '已通过', rejected: '已拒绝', allocated: '已分配' }
  return map[status] || status
}

/**
 * 库存百分比计算（兼容两种数据结构）
 */
const getStockPercentage = (item) => {
  const threshold = item.warningThreshold ?? item.threshold
  const stock = item.currentStock ?? item.stock
  if (!threshold || threshold === 0) return 0
  return Math.min(100, Math.round((stock / threshold) * 100))
}

// ===== 生命周期 =====

onMounted(() => {
  loadDashboardData()
  initTrendChart()
  initPieChart()

  // 30 秒自动刷新
  refreshTimer = setInterval(loadDashboardData, 30000)

  // 监听窗口大小
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清理定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  // 销毁图表实例
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.dashboard {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ==================== 欢迎区域 ==================== */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.welcome-desc {
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  gap: 12px;
  
  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #fff;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

/* ==================== 统计卡片 ==================== */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  margin-right: 16px;
  flex-shrink: 0;
  
  &.stat-icon-total {
    background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  }
  &.stat-icon-inbound {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  }
  &.stat-icon-outbound {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  }
  &.stat-icon-warning {
    background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  }
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  color: rgba(0, 0, 0, 0.03);
  pointer-events: none;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary, #1f2937);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary, #6b7280);
  margin-top: 4px;
}

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 2px;
  
  &.up { color: #10b981; }
  &.down { color: #ef4444; }
}

/* ==================== 卡片通用 ==================== */
.chart-row {
  margin-bottom: 20px;
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary, #1f2937);
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0;
  }
}

.chart-container {
  height: 320px;
}

/* ==================== 信息面板 ==================== */
.info-panel {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  
  .el-table,
  .warning-list,
  .activity-list {
    flex: 1;
    overflow-y: auto;
  }
}

.empty-state-small {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 120px;
}

/* ==================== 库存预警列表 ==================== */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.warning-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f5f5;
  }
}

.warning-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  flex-shrink: 0;
  
  &.high, &.danger {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
  
  &.medium, &.warning {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
  }
  
  &.low {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
  }
}

.warning-content {
  flex: 1;
  min-width: 0;
}

.warning-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
  margin-bottom: 8px;
}

.warning-progress {
  .warning-detail {
    font-size: 11px;
    color: var(--text-secondary, #6b7280);
    margin-top: 4px;
  }
}

/* ==================== 实时动态列表 ==================== */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fafafa;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f5f5;
  }
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  &.application {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
  }
  
  &.donation {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
  }
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 12px;
  color: var(--text-secondary, #6b7280);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: 11px;
  color: var(--text-placeholder, #9ca3af);
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 24px;
  }
  
  .welcome-actions {
    justify-content: center;
  }

  .info-panel {
    min-height: auto;
  }
}
</style>

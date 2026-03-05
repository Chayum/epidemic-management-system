<!--
  管理端仪表盘组件
  展示系统整体运行状态，包括统计卡片、趋势图表、库存预警及操作日志
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
      </div>
    </div>
    
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ borderLeftColor: stat.color }">
          <div class="stat-icon-wrap" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            <el-icon><component :is="stat.trend > 0 ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
            {{ Math.abs(stat.trend) }}%
          </div>
        </div>
        </el-col>
    </el-row>
    
    <!-- 图表区域：趋势图与饼图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <div class="card-container">
          <div class="card-header">
            <h3>物资入库趋势</h3>
            <el-radio-group v-model="chartPeriod" size="small">
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
            <h3>物资类型分布</h3>
          </div>
          <div class="chart-container" ref="pieChartRef"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 列表区域：待审核申请与库存预警 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <div class="card-container">
          <div class="card-header">
            <h3>待审核申请</h3>
            <el-button type="primary" link @click="$router.push('/material/approval')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <el-table :data="pendingApplications" style="width: 100%">
            <el-table-column prop="id" label="申请单号" width="120" />
            <el-table-column prop="applicant" label="申请人" />
            <el-table-column prop="material" label="物资名称" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="time" label="申请时间" width="100" />
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="card-container">
          <div class="card-header">
            <h3>库存预警</h3>
            <el-badge :value="warningCount" type="warning">
              <el-button type="primary" link @click="$router.push('/material/inventory')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-badge>
          </div>
          <div class="warning-list">
            <div v-for="item in warningList" :key="item.id" class="warning-item">
              <div class="warning-icon" :class="item.level">
                <el-icon><WarningFilled /></el-icon>
              </div>
              <div class="warning-info">
                <div class="warning-name">{{ item.name }}</div>
                <div class="warning-desc">当前库存：{{ item.stock }}，阈值：{{ item.threshold }}</div>
              </div>
              <el-tag :type="item.level === 'danger' ? 'danger' : 'warning'" size="small">
                {{ item.level === 'danger' ? '紧急' : '预警' }}
              </el-tag>
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
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getDashboardStats, getTrendData, getMaterialStats } from '@/api/stats'
import { getApplicationList } from '@/api/application'
import { getMaterialList } from '@/api/material'

// 当前日期格式化
const currentDate = computed(() => dayjs().format('YYYY年MM月DD日'))

// 图表周期筛选（week/month/year）
const chartPeriod = ref('week')

// 图表 DOM 引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const loading = ref(false)

// 统计卡片数据初始化
const stats = ref([
  { label: '物资总数', value: '0', icon: 'Box', color: '#1890ff', bgColor: '#e6f7ff', trend: 0 },
  { label: '今日入库', value: '0', icon: 'Download', color: '#52c41a', bgColor: '#f6ffed', trend: 0 },
  { label: '今日出库', value: '0', icon: 'Upload', color: '#faad14', bgColor: '#fffbe6', trend: 0 },
  { label: '待审核', value: '0', icon: 'Clock', color: '#f5222d', bgColor: '#fff1f0', trend: 0 }
])

// 列表数据状态
const pendingApplications = ref([])
const warningCount = ref(0)
const warningList = ref([])
const operationLogs = ref([])

/**
 * 获取仪表盘核心统计数据
 * 并发请求多个接口以提升加载速度
 */
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 获取基础统计数字
    const res = await getDashboardStats()
    if (res.code === 200) {
      const data = res.data
      stats.value[0].value = data.totalMaterials?.toLocaleString() || '0'
      stats.value[1].value = data.todayInbound?.toLocaleString() || '0'
      stats.value[2].value = data.todayOutbound?.toLocaleString() || '0'
      stats.value[3].value = data.pendingApplications?.toString() || '0'
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
  
  try {
    // 获取待审核申请列表（取前4条）
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
  
  try {
    // 获取库存预警列表（取前3条）
    const matRes = await getMaterialList({ page: 1, size: 3 })
    if (matRes.code === 200) {
      const warnings = matRes.data.list?.filter(item => item.stock < item.threshold) || []
      warningCount.value = warnings.length
      warningList.value = warnings.slice(0, 3).map(item => ({
        id: item.id,
        name: item.name,
        stock: item.stock,
        threshold: item.threshold,
        level: item.stock < item.threshold * 0.5 ? 'danger' : 'warning'
      }))
    }
  } catch (error) {
    console.error('获取库存预警失败:', error)
  }
  
  loading.value = false
}

/**
 * 初始化入库出库趋势折线图
 * 支持按周期筛选刷新数据
 */
const initTrendChart = async () => {
  const chart = echarts.init(trendChartRef.value)
  
  try {
    const res = await getTrendData({ period: chartPeriod.value })
    if (res.code === 200) {
      const data = res.data
      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['入库', '出库'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.dates
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '入库',
            type: 'line',
            smooth: true,
            data: data.inbound,
            areaStyle: { color: 'rgba(24, 144, 255, 0.1)' },
            itemStyle: { color: '#1890ff' }
          },
          {
            name: '出库',
            type: 'line',
            smooth: true,
            data: data.outbound,
            areaStyle: { color: 'rgba(82, 196, 26, 0.1)' },
            itemStyle: { color: '#52c41a' }
          }
        ]
      }
      chart.setOption(option)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

/**
 * 初始化物资类型分布饼图
 */
const initPieChart = async () => {
  const chart = echarts.init(pieChartRef.value)
  
  try {
    const res = await getMaterialStats()
    if (res.code === 200) {
      const typeStats = res.data.typeStats || []
      const data = typeStats.map(item => ({
        value: item.stock || item.count || 0,
        name: item.name,
        itemStyle: { 
          color: item.name === '防护物资' ? '#1890ff' : 
                 item.name === '消杀物资' ? '#52c41a' : 
                 item.name === '检测物资' ? '#faad14' : '#722ed1' 
        }
      }))
      
      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', right: 10, top: 'center' },
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 14, fontWeight: 'bold' }
            },
            data: data
          }
        ]
      }
      chart.setOption(option)
    }
  } catch (error) {
    console.error('获取物资分布失败:', error)
  }
}

// 页面加载完成后初始化
onMounted(() => {
  fetchDashboardData()
  initTrendChart()
  initPieChart()
  
  // 监听窗口大小变化，自适应图表尺寸
  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(trendChartRef.value)?.resize()
    echarts.getInstanceByDom(pieChartRef.value)?.resize()
  })
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

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  margin-right: 16px;
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

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 2px;
  
  &.up { color: #52c41a; }
  &.down { color: #f5222d; }
}

.chart-row {
  margin-bottom: 20px;
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.chart-container {
  height: 300px;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.warning-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  gap: 12px;
}

.warning-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  
  &.danger {
    background: #fff1f0;
    color: #f5222d;
  }
  
  &.warning {
    background: #fffbe6;
    color: #faad14;
  }
}

.warning-info {
  flex: 1;
}

.warning-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.warning-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}
</style>

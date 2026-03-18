<template>
  <div class="user-home">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎使用防疫物资调度系统</h1>
        <p class="welcome-desc">今天是 {{ currentDate }}，让我们共同携手抗击疫情</p>
      </div>
    </div>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="8" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.bgColor }">
          <div class="stat-icon-wrap" :style="{ background: stat.iconBg }">
            <el-icon :size="28" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="card-container">
          <div class="card-header">
            <h3>快速操作</h3>
          </div>
          <div class="quick-actions">
            <div class="action-card" @click="$router.push('/user/donation')">
              <div class="action-icon" style="background: #e6f7ff;">
                <el-icon :size="32" color="#1890ff"><Present /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">我要捐赠</div>
                <div class="action-desc">捐赠防疫物资，共同抗疫</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="action-card" @click="$router.push('/user/apply')">
              <div class="action-icon" style="background: #f6ffed;">
                <el-icon :size="32" color="#52c41a"><ShoppingCart /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">物资申领</div>
                <div class="action-desc">申请所需防疫物资</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="action-card" @click="$router.push('/user/my-application')">
              <div class="action-icon" style="background: #fff7e6;">
                <el-icon :size="32" color="#fa8c16"><Document /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">我的申请</div>
                <div class="action-desc">查看申领进度</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="action-card" @click="$router.push('/user/my-donation')">
              <div class="action-icon" style="background: #fff1f0;">
                <el-icon :size="32" color="#f5222d"><Medal /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">我的捐赠</div>
                <div class="action-desc">查看捐赠记录</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="card-container recent-section">
          <div class="card-header">
            <h3>最新疫情动态</h3>
            <el-button type="primary" link>查看更多</el-button>
          </div>
          <div class="news-list">
            <div v-for="news in newsList" :key="news.id" class="news-item" @click="handleViewNews(news)">
              <div class="news-tag" :class="news.tagType">{{ news.tag }}</div>
              <div class="news-title">{{ news.title }}</div>
              <div class="news-time">{{ news.time }}</div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="card-container">
          <div class="card-header">
            <h3>库存预警</h3>
            <el-badge :value="warningCount" type="warning">
              <el-button type="primary" link>查看详情</el-button>
            </el-badge>
          </div>
          <div class="warning-list">
            <div v-for="item in warningList" :key="item.id" class="warning-item">
              <div class="warning-icon" :class="item.level">
                <el-icon><WarningFilled /></el-icon>
              </div>
              <div class="warning-info">
                <div class="warning-name">{{ item.name }}</div>
                <div class="warning-desc">库存: {{ item.stock }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card-container contact-section">
          <div class="card-header">
            <h3>联系我们</h3>
          </div>
          <div class="contact-list">
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>服务热线：400-888-8888</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>邮箱：support@epidemic.gov</span>
            </div>
            <div class="contact-item">
              <el-icon><Clock /></el-icon>
              <span>服务时间：全天24小时</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getWarningList } from '@/api/stats'
import { getNewsList } from '@/api/pandemic'

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日'))

const stats = ref([
  { label: '我的申领', value: '0', icon: 'Document', color: '#1890ff', bgColor: '#e6f7ff', iconBg: '#bae7ff' },
  { label: '待审核', value: '0', icon: 'Clock', color: '#faad14', bgColor: '#fffbe6', iconBg: '#ffe7ba' },
  { label: '我的捐赠', value: '0', icon: 'Present', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' }
])

const newsList = ref([])
const warningList = ref([])
const warningCount = ref(0)

const fetchHomeData = async () => {
  try {
    // 1. Get User Stats
    const statsRes = await getUserStats()
    if (statsRes.code === 200) {
      const data = statsRes.data
      stats.value[0].value = (data.myApplicationCount || 0).toString()
      stats.value[1].value = (data.pendingApplicationCount || 0).toString()
      stats.value[2].value = (data.myDonationCount || 0).toString()
    }

    // 2. Get Warning List
    const warningRes = await getWarningList()
    if (warningRes.code === 200) {
      const list = warningRes.data || []
      warningList.value = list.map(item => ({
        ...item,
        level: item.warningLevel === 'high' ? 'warning' : 'normal'
      }))
      warningCount.value = list.length
    }

    // 3. Get News
    const newsRes = await getNewsList({ page: 1, size: 5, status: 'published' })
    if (newsRes.code === 200) {
      newsList.value = (newsRes.data.list || []).map(item => ({
        id: item.id,
        tag: '动态', // Default tag or map from item.type if available
        tagType: 'blue',
        title: item.title,
        time: dayjs(item.publishTime).format('YYYY-MM-DD')
      }))
    }
  } catch (error) {
    console.error(error)
  }
}

const handleViewNews = (news) => {
  ElMessage.info(`查看: ${news.title}`)
}

onMounted(() => {
  fetchHomeData()
})
</script>

<style scoped lang="scss">
.user-home {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-section {
  padding: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
}

.welcome-desc {
  font-size: 16px;
  opacity: 0.9;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 12px;
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 4px;
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
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

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f0f0;
    transform: translateY(-2px);
  }
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.action-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
}

.action-arrow {
  color: #c0c0c0;
}

.recent-section {
  margin-top: 20px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: #f0f0f0;
  }
}

.news-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  
  &.red { background: #fff1f0; color: #f5222d; }
  &.blue { background: #e6f7ff; color: #1890ff; }
  &.green { background: #f6ffed; color: #52c41a; }
  &.orange { background: #fff7e6; color: #fa8c16; }
}

.news-title {
  flex: 1;
  font-size: 14px;
  color: #1a1a1a;
}

.news-time {
  font-size: 12px;
  color: #8c8c8c;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.warning-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.warning-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  
  &.warning {
    background: #fffbe6;
    color: #faad14;
  }
  
  &.normal {
    background: #e6f7ff;
    color: #1890ff;
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

.contact-section {
  margin-top: 20px;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #595959;
  
  .el-icon {
    color: #1890ff;
  }
}
</style>

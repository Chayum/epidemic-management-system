<template>
  <div class="pandemic-news-page">
    <div class="page-header">
      <h2 class="page-title">疫情动态</h2>
    </div>

    <div class="card-container">
      <div class="news-list" v-loading="loading">
        <div v-if="newsList.length === 0" class="empty-state">
          <el-empty description="暂无疫情动态" />
        </div>

        <div v-for="news in newsList" :key="news.id" class="news-card" @click="handleView(news)">
          <div class="news-card-header">
            <span class="news-tag" :class="getTagClass(news.status)">{{ getStatusText(news.status) }}</span>
            <span class="news-time">{{ formatDate(news.publishTime) }}</span>
          </div>
          <h3 class="news-title">{{ news.title }}</h3>
          <p class="news-summary">{{ news.summary || '暂无摘要' }}</p>
          <div class="news-card-footer">
            <span class="news-author">
              <el-icon><User /></el-icon> {{ news.author || '未知作者' }}
            </span>
            <span class="news-view">
              <el-icon><View /></el-icon> {{ news.viewCount || 0 }}
            </span>
          </div>
        </div>
      </div>

      <div class="pagination-container" v-if="page.total > 0">
        <el-pagination
          v-model:current-page="page.page"
          v-model:page-size="page.size"
          :page-sizes="[5, 10, 20]"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="新闻详情" width="700px" destroy-on-close>
      <div class="news-detail">
        <h2 class="detail-title">{{ currentNews.title }}</h2>
        <div class="detail-meta">
          <span><el-icon><User /></el-icon> {{ currentNews.author }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatDate(currentNews.publishTime) }}</span>
          <span><el-icon><View /></el-icon> {{ currentNews.viewCount }}</span>
        </div>
        <div class="detail-content">{{ currentNews.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { View, User, Clock } from '@element-plus/icons-vue'
import { getNewsList, getNewsDetail } from '@/api/pandemic'
import dayjs from 'dayjs'

const loading = ref(false)
const newsList = ref([])
const page = reactive({ page: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const currentNews = ref({})

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getNewsList({ page: page.page, size: page.size })
    if (res.code === 200) {
      newsList.value = res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取新闻列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleView = async (news) => {
  try {
    const res = await getNewsDetail(news.id)
    if (res.code === 200) {
      currentNews.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

const handleSizeChange = (size) => {
  page.size = size
  page.page = 1
  fetchNews()
}

const handleCurrentChange = (pageNum) => {
  page.page = pageNum
  fetchNews()
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getTagClass = (status) => {
  const map = { published: 'success', draft: 'info', archived: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { published: '最新', draft: '草稿', archived: '归档' }
  return map[status] || status
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped lang="scss">
.pandemic-news-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 20px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary, #1f2937);
  }
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-card {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.news-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.news-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;

  &.success {
    background: rgba(82, 196, 26, 0.1);
    color: #52c41a;
  }

  &.info {
    background: rgba(144, 147, 153, 0.1);
    color: #909399;
  }

  &.warning {
    background: rgba(250, 173, 20, 0.1);
    color: #faad14;
  }
}

.news-time {
  font-size: 12px;
  color: #8c8c8c;
}

.news-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  line-height: 1.4;
}

.news-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card-footer {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #8c8c8c;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.news-detail {
  padding: 0 10px;
}

.detail-title {
  text-align: center;
  margin-bottom: 20px;
  color: #1a1a1a;
  font-size: 20px;
}

.detail-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  color: #8c8c8c;
  font-size: 13px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.detail-content {
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  font-size: 14px;
}

.empty-state {
  padding: 60px 0;
}
</style>
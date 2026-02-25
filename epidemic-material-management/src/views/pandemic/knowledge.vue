<template>
  <div class="pandemic-knowledge">
    <div class="page-header">
      <h2 class="page-title">防控知识库</h2>
    </div>

    <div class="card-container">
      <div class="knowledge-grid">
        <div v-for="item in knowledgeList" :key="item.id" class="knowledge-card">
          <div class="knowledge-icon">
            <el-icon :size="40" :color="getCategoryColor(item.category)">
              <component :is="getCategoryIcon(item.category)" />
            </el-icon>
          </div>
          <div class="knowledge-content">
            <h3 class="knowledge-title">{{ item.title }}</h3>
            <p class="knowledge-summary">{{ item.summary }}</p>
            <div class="knowledge-meta">
              <el-tag size="small" :type="getCategoryType(item.category)">{{ getCategoryText(item.category) }}</el-tag>
              <span class="view-count"><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            </div>
            <el-button type="primary" link class="read-btn" @click="handleView(item)">阅读全文</el-button>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page.page"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="fetchKnowledge"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="知识详情" width="700px">
      <div class="knowledge-detail">
        <h2 class="detail-title">{{ currentKnowledge.title }}</h2>
        <div class="detail-meta">
          <span>来源: {{ currentKnowledge.author }}</span>
          <span>分类: {{ getCategoryText(currentKnowledge.category) }}</span>
        </div>
        <div class="detail-content">
          <p>{{ currentKnowledge.summary }}</p>
          <!-- Mock content as API might not return full content in list -->
          <p>此处应显示详细内容...</p> 
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { View, FirstAidKit, OfficeBuilding, HomeFilled } from '@element-plus/icons-vue'
import { getKnowledgeList } from '@/api/pandemic'

const knowledgeList = ref([])
const page = reactive({ page: 1, size: 12, total: 0 })
const detailVisible = ref(false)
const currentKnowledge = ref({})

const fetchKnowledge = async () => {
  try {
    const res = await getKnowledgeList({ page: page.page, size: page.size })
    if (res.code === 200) {
      knowledgeList.value = res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取知识库失败', error)
  }
}

const handleView = (item) => {
  currentKnowledge.value = item
  detailVisible.value = true
}

const getCategoryColor = (category) => {
  const map = {
    personal: '#409EFF',
    community: '#67C23A',
    hospital: '#F56C6C'
  }
  return map[category] || '#909399'
}

const getCategoryIcon = (category) => {
  const map = {
    personal: 'HomeFilled',
    community: 'OfficeBuilding',
    hospital: 'FirstAidKit'
  }
  return map[category] || 'InfoFilled'
}

const getCategoryType = (category) => {
  const map = {
    personal: '',
    community: 'success',
    hospital: 'danger'
  }
  return map[category] || 'info'
}

const getCategoryText = (category) => {
  const map = {
    personal: '个人防护',
    community: '社区防控',
    hospital: '医疗指南'
  }
  return map[category] || category
}

onMounted(() => {
  fetchKnowledge()
})
</script>

<style scoped lang="scss">
.pandemic-knowledge {
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

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.knowledge-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  gap: 15px;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transform: translateY(-2px);
  }
}

.knowledge-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
}

.knowledge-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.knowledge-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.knowledge-summary {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 40px;
}

.knowledge-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-count {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 4px;
}

.read-btn {
  padding: 0;
  margin-top: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.detail-title {
  text-align: center;
  margin-bottom: 20px;
}

.detail-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.detail-content {
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
}
</style>

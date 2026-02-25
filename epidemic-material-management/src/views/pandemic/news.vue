<template>
  <div class="pandemic-news">
    <div class="page-header">
      <h2 class="page-title">疫情动态</h2>
      <el-button type="primary" @click="handleAddNews">
        <el-icon><Plus /></el-icon>发布动态
      </el-button>
    </div>

    <div class="card-container">
      <el-table :data="newsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="author" label="发布人" width="120" />
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">{{ formatDate(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page.page"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="fetchNews"
        />
      </div>
    </div>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="newsForm" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newsForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="newsForm.summary" type="textarea" :rows="2" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="newsForm.content" type="textarea" :rows="10" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="newsForm.author" placeholder="请输入作者" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="新闻详情" width="700px">
      <div class="news-detail">
        <h2 class="detail-title">{{ currentNews.title }}</h2>
        <div class="detail-meta">
          <span>发布人: {{ currentNews.author }}</span>
          <span>时间: {{ formatDate(currentNews.publishTime) }}</span>
          <span>阅读: {{ currentNews.viewCount }}</span>
        </div>
        <div class="detail-content">{{ currentNews.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getNewsList, getNewsDetail, publishNews, deleteNews } from '@/api/pandemic' // Note: API method names might need update in api file
import dayjs from 'dayjs'

const loading = ref(false)
const newsList = ref([])
const page = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('发布动态')
const submitting = ref(false)
const formRef = ref(null)

const detailVisible = ref(false)
const currentNews = ref({})

const newsForm = reactive({
  title: '',
  summary: '',
  content: '',
  author: '',
  status: 'published'
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

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

const handleAddNews = () => {
  dialogTitle.value = '发布动态'
  Object.assign(newsForm, {
    title: '',
    summary: '',
    content: '',
    author: '管理员',
    status: 'published'
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await publishNews(newsForm)
        if (res.code === 200) {
          ElMessage.success('发布成功')
          dialogVisible.value = false
          fetchNews()
        }
      } catch (error) {
        ElMessage.error('发布失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleView = async (row) => {
  try {
    const res = await getNewsDetail(row.id)
    if (res.code === 200) {
      currentNews.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteNews(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchNews()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped lang="scss">
.pandemic-news {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.news-detail {
  padding: 0 20px;
}

.detail-title {
  text-align: center;
  margin-bottom: 20px;
  color: #1a1a1a;
}

.detail-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #8c8c8c;
  font-size: 13px;
  margin-bottom: 30px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.detail-content {
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}
</style>

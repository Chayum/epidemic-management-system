<template>
  <div class="pandemic-knowledge">
    <div class="page-header">
      <h2 class="page-title">防控知识库</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="6">
        <div class="category-panel card-container">
          <h3 class="section-title">知识分类</h3>
          <div class="category-list">
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="category-item"
              :class="{ active: activeCategory === cat.id }"
              @click="handleCategoryClick(cat.id)"
            >
              <el-icon><component :is="cat.icon" /></el-icon>
              <span>{{ cat.name }}</span>
              <el-tag size="small">{{ cat.count }}</el-tag>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="18">
        <div class="search-bar card-container">
          <el-form :inline="true">
            <el-form-item label="关键词">
              <el-input v-model="searchKeyword" placeholder="请输入关键词搜索" clearable style="width: 300px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>搜索
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="knowledge-list">
          <div v-for="item in filteredList" :key="item.id" class="knowledge-item card-container">
            <div class="item-header">
              <div class="item-title">{{ item.title }}</div>
              <el-tag size="small">{{ item.category }}</el-tag>
            </div>
            <div class="item-content">{{ item.summary }}</div>
            <div class="item-footer">
              <span class="item-meta">
                <el-icon><View /></el-icon> {{ item.views }}
                <el-icon style="margin-left: 12px"><Download /></el-icon> {{ item.downloads }}
              </span>
              <el-button type="primary" link @click="handleView(item)">
                查看详情 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
          
          <el-empty v-if="filteredList.length === 0" description="暂无数据" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, View, Download, ArrowRight } from '@element-plus/icons-vue'

const activeCategory = ref(1)
const searchKeyword = ref('')

const categories = ref([
  { id: 1, name: '个人防护', icon: 'User', count: 45 },
  { id: 2, name: '社区防控', icon: 'OfficeBuilding', count: 32 },
  { id: 3, name: '医疗机构指南', icon: 'FirstAidKit', count: 28 },
  { id: 4, name: '心理健康', icon: 'ChatDotRound', count: 15 }
])

const knowledgeList = ref([
  { id: 1, title: '如何正确佩戴N95口罩', category: '个人防护', summary: 'N95口罩的正确佩戴方法及注意事项，包括检查口罩完整性、正确调整鼻夹、确保密封性等步骤。', views: 1256, downloads: 89 },
  { id: 2, title: '七步洗手法详解', category: '个人防护', summary: '标准七步洗手法操作步骤，使用肥皂或洗手液在流动水下清洗至少20秒。', views: 892, downloads: 156 },
  { id: 3, title: '社区出入口防控管理指引', category: '社区防控', summary: '社区疫情防控期间出入口管理要求，包括人员登记、体温监测、健康码核验等。', views: 567, downloads: 45 },
  { id: 4, title: '居家隔离医学观察指南', category: '社区防控', summary: '居家隔离人员注意事项，包括单独居住、佩戴口罩、每日健康监测、环境消毒等。', views: 723, downloads: 67 },
  { id: 5, title: '医疗机构消毒技术规范', category: '医疗机构指南', summary: '医疗机构日常消毒技术要点，包括物表消毒、空气消毒、医疗废物处理等。', views: 445, downloads: 78 },
  { id: 6, title: '医护人员防护用品穿脱流程', category: '医疗机构指南', summary: '医护人员个人防护装备（PPE）正确穿脱顺序和注意事项。', views: 634, downloads: 92 },
  { id: 7, title: '疫情期间心理调适建议', category: '心理健康', summary: '疫情防控期间公众心理调适方法，包括保持规律作息、适度运动、情绪管理技巧等。', views: 389, downloads: 34 },
  { id: 8, title: '疫情防控违法违规行为警示', category: '个人防护', summary: '疫情防控期间常见违法违规行为及法律后果，呼吁公众遵守防疫规定。', views: 712, downloads: 56 }
])

const filteredList = computed(() => {
  let list = knowledgeList.value
  if (activeCategory.value) {
    const cat = categories.value.find(c => c.id === activeCategory.value)
    if (cat) {
      list = list.filter(item => item.category === cat.name)
    }
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      item.summary.toLowerCase().includes(keyword)
    )
  }
  return list
})

const handleCategoryClick = (id) => {
  activeCategory.value = id
}

const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleView = (item) => {
  ElMessage.info(`查看: ${item.title}`)
}
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.category-panel {
  margin-bottom: 20px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f5f5;
  }
  
  &.active {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  span {
    flex: 1;
  }
}

.search-bar {
  margin-bottom: 20px;
}

.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.knowledge-item {
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.item-content {
  font-size: 14px;
  color: #595959;
  line-height: 1.8;
  margin-bottom: 16px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #8c8c8c;
}
</style>

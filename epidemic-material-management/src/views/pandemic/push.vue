<template>
  <div class="pandemic-push">
    <div class="page-header">
      <h2 class="page-title">消息推送</h2>
      <el-button type="primary" @click="handlePush">
        <el-icon><Promotion /></el-icon>发送推送
      </el-button>
    </div>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="stat in pushStats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.bgColor }">
          <div class="stat-icon-wrap" :style="{ background: stat.iconBg }">
            <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
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
          <h3 class="section-title">推送记录</h3>
          <el-table :data="pushRecords" style="width: 100%" :scroll-x="true">
            <el-table-column prop="title" label="推送标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="target" label="推送对象" min-width="120" show-overflow-tooltip />
            <el-table-column prop="time" label="推送时间" min-width="160" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" min-width="80" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页组件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="card-container">
          <h3 class="section-title">用户角色分布</h3>
          <div class="chart-container" ref="roleChartRef"></div>
        </div>
      </el-col>
    </el-row>
    
    <el-dialog v-model="dialogVisible" title="发送推送" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="推送标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入推送标题" />
        </el-form-item>
        <el-form-item label="推送内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入推送内容" />
        </el-form-item>
        <el-form-item label="推送对象" prop="target">
          <el-select v-model="form.target" placeholder="请选择推送对象" style="width: 100%">
            <el-option label="全部用户" value="all" />
            <el-option label="申请方" value="applicant" />
            <el-option label="捐赠方" value="donor" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getPushStats, getPushList, sendPush, getUserRoleStats, deletePushRecord } from '@/api/pandemic'

const dialogVisible = ref(false)
const formRef = ref(null)
const roleChartRef = ref(null)

const form = reactive({
  title: '',
  content: '',
  target: ''
})

const formRules = {
  title: [{ required: true, message: '请输入推送标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入推送内容', trigger: 'blur' }],
  target: [{ required: true, message: '请选择推送对象', trigger: 'change' }]
}

const pushStats = ref([])

const pushRecords = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchStats = async () => {
  try {
    const res = await getPushStats()
    if (res.code === 200) {
      const styles = {
        '推送总数': { icon: 'Promotion', color: '#1890ff', bgColor: '#e6f7ff', iconBg: '#bae7ff' },
        '今日推送': { icon: 'Clock', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' },
        '推送成功': { icon: 'CircleCheck', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' },
        '推送失败': { icon: 'Warning', color: '#f5222d', bgColor: '#fff1f0', iconBg: '#ffccc7' }
      }
      pushStats.value = res.data.map(item => ({
        ...item,
        ...styles[item.label]
      }))
    }
  } catch (error) {
    console.error('获取推送统计失败', error)
  }
}

const fetchRecords = async () => {
  try {
    const res = await getPushList({
      page: pagination.page,
      size: pagination.size
    })
    if (res.code === 200) {
      pushRecords.value = res.data.list || res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取推送记录失败', error)
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchRecords()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchRecords()
}

const roleChartData = ref([])

const fetchRoleChart = async () => {
  try {
    const res = await getUserRoleStats()
    if (res.code === 200) {
      roleChartData.value = res.data
      initRoleChart(res.data)
    }
  } catch (error) {
    console.error('获取用户角色分布失败', error)
  }
}

const initRoleChart = (data) => {
  if (!roleChartRef.value) return
  const chart = echarts.init(roleChartRef.value)
  const chartData = data || roleChartData.value
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'horizontal', bottom: 0, left: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: chartData.length > 0 ? chartData : [
          { value: 0, name: '暂无数据' }
        ]
      }
    ]
  }
  chart.setOption(option)
}

const handlePush = () => {
  Object.assign(form, { title: '', content: '', target: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await sendPush(form)
        if (res.code === 200) {
          ElMessage.success('推送发送成功')
          dialogVisible.value = false
          fetchStats()
          fetchRecords()
        }
      } catch (error) {
        ElMessage.error('推送发送失败')
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条推送记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePushRecord(row.id)
    ElMessage.success('删除成功')
    fetchRecords()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  fetchStats()
  fetchRecords()
  fetchRoleChart()
})
</script>

<style scoped lang="scss">
.pandemic-push {
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

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.chart-container {
  height: 320px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

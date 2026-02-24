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
      <el-col :xs="24" :lg="12">
        <div class="card-container">
          <h3 class="section-title">推送记录</h3>
          <el-table :data="pushRecords" style="width: 100%">
            <el-table-column prop="title" label="推送标题" min-width="150" />
            <el-table-column prop="target" label="推送对象" width="120" />
            <el-table-column prop="channel" label="推送渠道" width="100">
              <template #default="{ row }">
                <el-tag v-for="ch in row.channelList" :key="ch" size="small" style="margin-right: 4px">{{ ch }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="推送时间" width="160" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="12">
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
            <el-option label="医院用户" value="hospital_user" />
            <el-option label="社区人员" value="community_staff" />
            <el-option label="物资审核员" value="material_approver" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送渠道" prop="channel">
          <el-checkbox-group v-model="form.channel">
            <el-checkbox label="APP">APP通知</el-checkbox>
            <el-checkbox label="SMS">短信</el-checkbox>
            <el-checkbox label="WEB">网页</el-checkbox>
          </el-checkbox-group>
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
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const dialogVisible = ref(false)
const formRef = ref(null)
const roleChartRef = ref(null)

const form = reactive({
  title: '',
  content: '',
  target: '',
  channel: []
})

const formRules = {
  title: [{ required: true, message: '请输入推送标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入推送内容', trigger: 'blur' }],
  target: [{ required: true, message: '请选择推送对象', trigger: 'change' }],
  channel: [{ type: 'array', required: true, message: '请选择推送渠道', trigger: 'change' }]
}

const pushStats = ref([
  { label: '推送总数', value: '1,256', icon: 'Promotion', color: '#1890ff', bgColor: '#e6f7ff', iconBg: '#bae7ff' },
  { label: '今日推送', value: '28', icon: 'Clock', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' },
  { label: '推送成功', value: '1,234', icon: 'CircleCheck', color: '#52c41a', bgColor: '#f6ffed', iconBg: '#d9f7be' },
  { label: '推送失败', value: '22', icon: 'Warning', color: '#f5222d', bgColor: '#fff1f0', iconBg: '#ffccc7' }
])

const pushRecords = ref([
  { title: '物资紧缺提醒', target: '医院用户', channelList: ['APP', 'SMS'], time: '2026-02-24 10:30:00', status: '成功' },
  { title: '新政策发布', target: '全部用户', channelList: ['APP', 'WEB'], time: '2026-02-24 09:00:00', status: '成功' },
  { title: '库存预警通知', target: '物资审核员', channelList: ['APP'], time: '2026-02-23 16:20:00', status: '成功' },
  { title: '社区防控指南', target: '社区人员', channelList: ['APP', 'SMS'], time: '2026-02-23 14:00:00', status: '失败' }
])

const initRoleChart = () => {
  const chart = echarts.init(roleChartRef.value)
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
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
        data: [
          { value: 350, name: '医院用户', itemStyle: { color: '#1890ff' } },
          { value: 280, name: '社区人员', itemStyle: { color: '#52c41a' } },
          { value: 120, name: '物资审核员', itemStyle: { color: '#faad14' } },
          { value: 50, name: '管理员', itemStyle: { color: '#722ed1' } }
        ]
      }
    ]
  }
  chart.setOption(option)
}

const handlePush = () => {
  Object.assign(form, { title: '', content: '', target: '', channel: [] })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('推送发送成功')
      dialogVisible.value = false
    }
  })
}

onMounted(() => {
  initRoleChart()
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
  height: 280px;
}
</style>

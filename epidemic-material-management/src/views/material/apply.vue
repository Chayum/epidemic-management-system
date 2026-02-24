<template>
  <div class="material-apply">
    <div class="page-header">
      <h2 class="page-title">物资申领</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <div class="card-container">
          <h3 class="section-title">申领申请</h3>
          <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
            <el-form-item label="物资类型" prop="type">
              <el-select v-model="applyForm.type" placeholder="请选择物资类型" style="width: 100%" @change="handleTypeChange">
                <el-option label="防护物资" value="protective" />
                <el-option label="消杀物资" value="disinfection" />
                <el-option label="检测物资" value="testing" />
              </el-select>
            </el-form-item>
            <el-form-item label="物资名称" prop="materialId">
              <el-select v-model="applyForm.materialId" placeholder="请选择物资" style="width: 100%">
                <el-option
                  v-for="item in materialOptions"
                  :key="item.id"
                  :label="`${item.name} (库存: ${item.stock})`"
                  :value="item.id"
                  :disabled="item.stock === 0"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="申请数量" prop="quantity">
              <el-input-number v-model="applyForm.quantity" :min="1" :max="maxQuantity" style="width: 100%" />
              <div class="quantity-tip">当前库存: {{ maxQuantity }}</div>
            </el-form-item>
            <el-form-item label="用途说明" prop="purpose">
              <el-input v-model="applyForm.purpose" type="textarea" :rows="3" placeholder="请输入物资用途说明，如：ICU急需N95口罩" />
            </el-form-item>
            <el-form-item label="收货地址" prop="address">
              <el-input v-model="applyForm.address" placeholder="请输入收货地址" />
            </el-form-item>
            <el-form-item label="紧急程度" prop="urgency">
              <el-radio-group v-model="applyForm.urgency">
                <el-radio label="normal">普通</el-radio>
                <el-radio label="urgent">紧急</el-radio>
                <el-radio label="critical">紧急</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmitApply" :loading="submitting">
                <el-icon><Plus /></el-icon>提交申请
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="10">
        <div class="card-container my-applications">
          <h3 class="section-title">我的申请</h3>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="待审核" name="pending">
              <div class="application-list">
                <div v-for="item in pendingList" :key="item.id" class="application-item">
                  <div class="app-header">
                    <span class="app-id">{{ item.id }}</span>
                    <el-tag type="warning" size="small">{{ item.urgencyText }}</el-tag>
                  </div>
                  <div class="app-content">
                    <div class="app-name">{{ item.name }}</div>
                    <div class="app-meta">申请数量: {{ item.quantity }} | {{ item.time }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="已通过" name="approved">
              <div class="application-list">
                <div v-for="item in approvedList" :key="item.id" class="application-item">
                  <div class="app-header">
                    <span class="app-id">{{ item.id }}</span>
                    <el-tag type="success" size="small">已通过</el-tag>
                  </div>
                  <div class="app-content">
                    <div class="app-name">{{ item.name }}</div>
                    <div class="app-meta">申请数量: {{ item.quantity }} | {{ item.time }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="已驳回" name="rejected">
              <el-empty description="暂无数据" :image-size="80" />
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div class="card-container application-tips">
          <h3 class="section-title">申请须知</h3>
          <ul class="tips-list">
            <li>请确保申请数量不超过当前库存</li>
            <li>紧急申请将优先处理</li>
            <li>提交后可在"我的申请"中查看进度</li>
            <li>如有疑问请联系物资管理员</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const applyFormRef = ref(null)
const submitting = ref(false)
const activeTab = ref('pending')

const applyForm = reactive({
  type: '',
  materialId: '',
  quantity: 1,
  purpose: '',
  address: '',
  urgency: 'normal'
})

const applyRules = {
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入申请数量', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用途说明', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const materialOptions = ref([
  { id: 'M001', name: 'N95医用口罩', stock: 850, type: 'protective' },
  { id: 'M002', name: '医用防护服', stock: 420, type: 'protective' },
  { id: 'M003', name: '84消毒液', stock: 2500, type: 'disinfection' },
  { id: 'M004', name: '医用酒精', stock: 680, type: 'disinfection' },
  { id: 'M005', name: '检测试剂', stock: 320, type: 'testing' }
])

const maxQuantity = computed(() => {
  const selected = materialOptions.value.find(m => m.id === applyForm.materialId)
  return selected ? selected.stock : 0
})

const pendingList = ref([
  { id: 'A2026024', name: 'N95医用口罩', quantity: 1000, time: '2026-02-24 10:30', urgencyText: '紧急' },
  { id: 'A2026020', name: '医用防护服', quantity: 200, time: '2026-02-23 15:20', urgencyText: '普通' }
])

const approvedList = ref([
  { id: 'A2026018', name: '84消毒液', quantity: 50, time: '2026-02-22 09:15' },
  { id: 'A2026015', name: '检测试剂', quantity: 100, time: '2026-02-20 14:30' }
])

const handleTypeChange = () => {
  applyForm.materialId = ''
  applyForm.quantity = 1
}

const handleSubmitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        submitting.value = false
        ElMessage.success('申请提交成功')
        handleReset()
      }, 1000)
    }
  })
}

const handleReset = () => {
  applyFormRef.value?.resetFields()
  Object.assign(applyForm, {
    type: '',
    materialId: '',
    quantity: 1,
    purpose: '',
    address: '',
    urgency: 'normal'
  })
}
</script>

<style scoped lang="scss">
.material-apply {
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
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.quantity-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.my-applications {
  margin-bottom: 20px;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.application-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.app-id {
  font-size: 13px;
  font-weight: 500;
  color: #1890ff;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.app-meta {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.application-tips {
  margin-top: 20px;
}

.tips-list {
  padding-left: 20px;
  margin: 0;
  
  li {
    font-size: 13px;
    color: #595959;
    line-height: 2;
  }
}
</style>

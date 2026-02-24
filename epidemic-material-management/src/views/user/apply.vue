<template>
  <div class="user-apply">
    <div class="page-header">
      <h2 class="page-title">物资申领</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="card-container">
          <el-steps :active="currentStep" finish-status="success" align-center class="apply-steps">
            <el-step title="选择物资" />
            <el-step title="填写申请" />
            <el-step title="提交成功" />
          </el-steps>
          
          <el-form v-show="currentStep === 0" ref="materialFormRef" :model="materialForm" :rules="materialRules" label-width="100px">
            <div class="form-section">
              <h3 class="section-title">选择物资</h3>
              <el-form-item label="物资类型" prop="type">
                <el-select v-model="materialForm.type" placeholder="请选择物资类型" style="width: 100%" @change="handleTypeChange">
                  <el-option label="防护物资" value="protective" />
                  <el-option label="消杀物资" value="disinfection" />
                  <el-option label="检测物资" value="testing" />
                </el-select>
              </el-form-item>
              <el-form-item label="物资名称" prop="materialId">
                <el-select v-model="materialForm.materialId" placeholder="请选择物资" style="width: 100%" :disabled="!materialForm.type">
                  <el-option
                    v-for="item in materialOptions"
                    :key="item.id"
                    :label="`${item.name} (当前库存: ${item.stock} ${item.unit})`"
                    :value="item.id"
                    :disabled="item.stock === 0"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="可申请量">
                <div class="stock-info">
                  <span class="stock-value">{{ selectedMaterial?.stock || 0 }}</span>
                  <span class="stock-unit">{{ selectedMaterial?.unit || '' }}</span>
                  <span class="stock-tip">（请在库存范围内申请）</span>
                </div>
              </el-form-item>
            </div>
            
            <el-form-item>
              <el-button type="primary" @click="handleNextStep">下一步</el-button>
            </el-form-item>
          </el-form>
          
          <el-form v-show="currentStep === 1" ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
            <div class="form-section">
              <h3 class="section-title">申请信息</h3>
              <el-form-item label="申请数量" prop="quantity">
                <el-input-number v-model="applyForm.quantity" :min="1" :max="selectedMaterial?.stock || 1" style="width: 100%" />
                <div class="quantity-tip">当前库存: {{ selectedMaterial?.stock || 0 }} {{ selectedMaterial?.unit || '' }}</div>
              </el-form-item>
              <el-form-item label="用途说明" prop="purpose">
                <el-input v-model="applyForm.purpose" type="textarea" :rows="3" placeholder="请详细说明物资用途，如：用于发热门诊日常防护" />
              </el-form-item>
              <el-form-item label="收货地址" prop="address">
                <el-input v-model="applyForm.address" placeholder="请输入详细的收货地址" />
              </el-form-item>
              <el-form-item label="收货人" prop="receiver">
                <el-input v-model="applyForm.receiver" placeholder="请输入收货人姓名" />
              </el-form-item>
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="applyForm.phone" placeholder="请输入收货人联系电话" />
              </el-form-item>
              <el-form-item label="紧急程度" prop="urgency">
                <el-radio-group v-model="applyForm.urgency">
                  <el-radio label="normal">普通</el-radio>
                  <el-radio label="urgent">较急</el-radio>
                  <el-radio label="critical">紧急</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            
            <div class="form-actions">
              <el-button @click="currentStep = 0">上一步</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
            </div>
          </el-form>
          
          <div v-show="currentStep === 2" class="success-section">
            <div class="success-icon">
              <el-icon :size="64" color="#52c41a"><CircleCheck /></el-icon>
            </div>
            <h3 class="success-title">申请提交成功！</h3>
            <p class="success-desc">您的物资申领申请已提交，请等待审核</p>
            <div class="success-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="申请单号">A2026024001</el-descriptions-item>
                <el-descriptions-item label="物资名称">{{ selectedMaterial?.name }}</el-descriptions-item>
                <el-descriptions-item label="申请数量">{{ applyForm.quantity }} {{ selectedMaterial?.unit }}</el-descriptions-item>
                <el-descriptions-item label="审核状态"><el-tag type="warning">待审核</el-tag></el-descriptions-item>
              </el-descriptions>
            </div>
            <div class="success-actions">
              <el-button type="primary" @click="$router.push('/user/my-application')">查看申请进度</el-button>
              <el-button @click="handleReset">继续申请</el-button>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="card-container apply-notice">
          <h3 class="section-title">申请须知</h3>
          <ul class="notice-list">
            <li>请确保申请数量不超过当前库存</li>
            <li>物资仅限疫情防控使用，不得挪作他用</li>
            <li>紧急申请将优先处理</li>
            <li>提交后可在"我的申请"中查看进度</li>
            <li>审核通过后将自动扣减库存</li>
          </ul>
        </div>
        
        <div class="card-container my-history">
          <h3 class="section-title">最近申请</h3>
          <div class="history-list">
            <div v-for="item in applicationHistory" :key="item.id" class="history-item">
              <div class="history-info">
                <div class="history-name">{{ item.name }}</div>
                <div class="history-meta">数量: {{ item.quantity }} | {{ item.time }}</div>
              </div>
              <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'

const materialFormRef = ref(null)
const applyFormRef = ref(null)
const currentStep = ref(0)
const submitting = ref(false)

const materialForm = reactive({
  type: '',
  materialId: ''
})

const materialRules = {
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  materialId: [{ required: true, message: '请选择物资', trigger: 'change' }]
}

const applyForm = reactive({
  quantity: 1,
  purpose: '',
  address: '',
  receiver: '',
  phone: '',
  urgency: 'normal'
})

const applyRules = {
  quantity: [{ required: true, message: '请输入申请数量', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用途说明', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const materialOptions = ref([
  { id: 'M001', name: 'N95医用口罩', stock: 850, unit: '个', type: 'protective' },
  { id: 'M002', name: '医用防护服', stock: 420, unit: '套', type: 'protective' },
  { id: 'M003', name: '84消毒液', stock: 2500, unit: '瓶', type: 'disinfection' },
  { id: 'M004', name: '医用酒精', stock: 680, unit: '瓶', type: 'disinfection' },
  { id: 'M005', name: '检测试剂', stock: 320, unit: '盒', type: 'testing' }
])

const selectedMaterial = computed(() => {
  return materialOptions.value.find(m => m.id === materialForm.materialId)
})

const applicationHistory = ref([
  { id: 'A2026023', name: 'N95医用口罩', quantity: 1000, time: '2026-02-24', status: 'pending' },
  { id: 'A2026018', name: '84消毒液', quantity: 50, time: '2026-02-22', status: 'approved' },
  { id: 'A2026015', name: '检测试剂', quantity: 100, time: '2026-02-20', status: 'approved' }
])

const getStatusType = (status) => {
  const typeMap = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return textMap[status] || ''
}

const handleTypeChange = () => {
  materialForm.materialId = ''
}

const handleNextStep = async () => {
  if (!materialFormRef.value) return
  await materialFormRef.value.validate((valid) => {
    if (valid) {
      currentStep.value = 1
    }
  })
}

const handleSubmit = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        submitting.value = false
        currentStep.value = 2
        ElMessage.success('申请提交成功')
      }, 1500)
    }
  })
}

const handleReset = () => {
  materialFormRef.value?.resetFields()
  applyFormRef.value?.resetFields()
  Object.assign(applyForm, { quantity: 1, purpose: '', address: '', receiver: '', phone: '', urgency: 'normal' })
  currentStep.value = 0
}
</script>

<style scoped lang="scss">
.user-apply {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.apply-steps {
  margin-bottom: 40px;
  padding: 20px 0;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .stock-value {
    font-size: 20px;
    font-weight: 600;
    color: #1890ff;
  }
  
  .stock-tip {
    font-size: 12px;
    color: #8c8c8c;
  }
}

.quantity-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.success-section {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.success-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 24px;
}

.success-info {
  text-align: left;
  max-width: 400px;
  margin: 0 auto 32px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.apply-notice {
  margin-bottom: 20px;
}

.notice-list {
  padding-left: 20px;
  margin: 0;
  
  li {
    font-size: 13px;
    color: #595959;
    line-height: 2;
  }
}

.my-history {
  margin-top: 20px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.history-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.history-meta {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
</style>

<template>
  <div class="material-donation">
    <div class="page-header">
      <h2 class="page-title">物资捐赠</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <div class="card-container">
          <h3 class="section-title">捐赠登记</h3>
          <el-form ref="donationFormRef" :model="donationForm" :rules="donationRules" label-width="100px">
            <el-form-item label="物资名称" prop="name">
              <el-input v-model="donationForm.name" placeholder="请输入物资名称" />
            </el-form-item>
            <el-form-item label="物资类型" prop="type">
              <el-select v-model="donationForm.type" placeholder="请选择物资类型" style="width: 100%">
                <el-option label="防护物资" value="protective" />
                <el-option label="消杀物资" value="disinfection" />
                <el-option label="检测物资" value="testing" />
              </el-select>
            </el-form-item>
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="donationForm.quantity" :min="1" :max="100000" style="width: 100%" />
            </el-form-item>
            <el-form-item label="单位" prop="unit">
              <el-select v-model="donationForm.unit" placeholder="请选择单位" style="width: 100%">
                <el-option label="个" value="个" />
                <el-option label="箱" value="箱" />
                <el-option label="套" value="套" />
                <el-option label="瓶" value="瓶" />
                <el-option label="盒" value="盒" />
              </el-select>
            </el-form-item>
            <el-form-item label="捐赠单位" prop="donorUnit">
              <el-input v-model="donationForm.donorUnit" placeholder="请输入捐赠单位名称" />
            </el-form-item>
            <el-form-item label="有效期" prop="expiryDate">
              <el-date-picker
                v-model="donationForm.expiryDate"
                type="date"
                placeholder="请选择有效期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="备注说明">
              <el-input v-model="donationForm.remark" type="textarea" :rows="3" placeholder="请输入备注说明" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmitDonation" :loading="submitting">
                <el-icon><Plus /></el-icon>提交捐赠
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="10">
        <div class="card-container donation-stats">
          <h3 class="section-title">捐赠统计</h3>
          <div class="stat-item">
            <div class="stat-icon-wrap" style="background: #e6f7ff;">
              <el-icon :size="24" color="#1890ff"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">1,256</div>
              <div class="stat-label">总捐赠次数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon-wrap" style="background: #f6ffed;">
              <el-icon :size="24" color="#52c41a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">45,890</div>
              <div class="stat-label">物资总数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon-wrap" style="background: #fffbe6;">
              <el-icon :size="24" color="#faad14"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">86</div>
              <div class="stat-label">捐赠单位数</div>
            </div>
          </div>
        </div>
        
        <div class="card-container recent-donations">
          <h3 class="section-title">最近捐赠</h3>
          <div class="donation-list">
            <div v-for="item in recentDonations" :key="item.id" class="donation-item">
              <div class="donation-info">
                <div class="donation-name">{{ item.name }}</div>
                <div class="donation-meta">{{ item.donorUnit }} · {{ item.quantity }}{{ item.unit }}</div>
              </div>
              <div class="donation-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Box, CircleCheck, OfficeBuilding } from '@element-plus/icons-vue'

const donationFormRef = ref(null)
const submitting = ref(false)

const donationForm = reactive({
  name: '',
  type: '',
  quantity: 1,
  unit: '',
  donorUnit: '',
  expiryDate: '',
  remark: ''
})

const donationRules = {
  name: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  donorUnit: [{ required: true, message: '请输入捐赠单位', trigger: 'blur' }],
  expiryDate: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

const recentDonations = ref([
  { id: 1, name: 'N95医用口罩', donorUnit: '市慈善总会', quantity: 5000, unit: '个', time: '10:30' },
  { id: 2, name: '84消毒液', donorUnit: '爱心企业A', quantity: 200, unit: '箱', time: '09:15' },
  { id: 3, name: '防护服', donorUnit: '市红十字会', quantity: 300, unit: '套', time: '昨天' },
  { id: 4, name: '医用酒精', donorUnit: '爱心人士', quantity: 150, unit: '瓶', time: '昨天' }
])

const handleSubmitDonation = async () => {
  if (!donationFormRef.value) return
  await donationFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        submitting.value = false
        ElMessage.success('捐赠登记成功')
        handleReset()
      }, 1000)
    }
  })
}

const handleReset = () => {
  donationFormRef.value?.resetFields()
  Object.assign(donationForm, {
    name: '',
    type: '',
    quantity: 1,
    unit: '',
    donorUnit: '',
    expiryDate: '',
    remark: ''
  })
}
</script>

<style scoped lang="scss">
.material-donation {
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

.donation-stats {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  margin-right: 16px;
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

.recent-donations {
  margin-top: 20px;
}

.donation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.donation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.donation-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.donation-meta {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.donation-time {
  font-size: 12px;
  color: #8c8c8c;
}
</style>

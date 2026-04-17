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
            <el-form-item label="物资类型" prop="type">
              <el-select v-model="donationForm.type" placeholder="请选择物资类型" style="width: 100%">
                <el-option label="防护物资" value="protective" />
                <el-option label="消杀物资" value="disinfection" />
                <el-option label="检测物资" value="testing" />
              </el-select>
            </el-form-item>
            <el-form-item label="物资名称" prop="materialName">
              <el-input v-model="donationForm.materialName" placeholder="请输入物资名称" />
            </el-form-item>
            <el-form-item label="捐赠数量" prop="quantity">
              <el-input-number v-model="donationForm.quantity" :min="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="单位" prop="unit">
              <el-input v-model="donationForm.unit" placeholder="如：个、箱、件" />
            </el-form-item>
            <el-form-item label="捐赠来源" prop="source">
              <el-radio-group v-model="donationForm.source">
                <el-radio label="personal">个人</el-radio>
                <el-radio label="enterprise">企业/组织</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="捐赠方名称" prop="donorUnit">
              <el-input v-model="donationForm.donorUnit" placeholder="请输入个人姓名或企业名称" />
            </el-form-item>
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="donationForm.contactPerson" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="donationForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="取货地址" prop="receiveAddress">
              <el-input v-model="donationForm.receiveAddress" placeholder="请输入取货地址" />
            </el-form-item>
            <el-form-item label="生产日期" prop="productionDate">
              <el-date-picker v-model="donationForm.productionDate" type="date" placeholder="选择生产日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="有效期至" prop="expiryDate">
              <el-date-picker v-model="donationForm.expiryDate" type="date" placeholder="选择有效期" style="width: 100%" />
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
        <div class="card-container donation-list-container">
          <h3 class="section-title">我的捐赠记录</h3>
          <div class="donation-list">
            <el-empty v-if="donationList.length === 0" description="暂无捐赠记录" :image-size="80" />
            <div v-else v-for="item in donationList" :key="item.id" class="donation-item">
              <div class="donation-header">
                <span class="donation-name">{{ item.materialName }}</span>
                <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
              </div>
              <div class="donation-meta">
                <span>数量: {{ item.quantity }}{{ item.unit }}</span>
                <span>时间: {{ formatDate(item.donateTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card-container donation-tips">
          <h3 class="section-title">捐赠须知</h3>
          <ul class="tips-list">
            <li>请确保捐赠物资符合国家质量标准</li>
            <li>易燃易爆物品请提前说明</li>
            <li>提交后会有工作人员与您联系确认取货事宜</li>
            <li>感谢您的爱心捐赠！</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitDonation, getDonationList } from '@/api/donation'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

// 获取用户 store
const userStore = useUserStore()
// 从 store 获取当前用户 ID
const currentUserId = computed(() => userStore.userInfo?.id)

const donationFormRef = ref(null)
const submitting = ref(false)
const donationList = ref([])

const donationForm = reactive({
  type: '',
  materialName: '',
  quantity: 1,
  unit: '',
  source: 'personal',
  donorUnit: '',
  contactPerson: '',
  contactPhone: '',
  receiveAddress: '',
  productionDate: '',
  expiryDate: ''
})

const donationRules = {
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  materialName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  donorUnit: [{ required: true, message: '请输入捐赠方名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const fetchMyDonations = async () => {
  try {
    // 从 store 获取当前用户 ID
    const res = await getDonationList({ page: 1, size: 20, donorId: currentUserId.value })
    if (res.code === 200) {
      donationList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取捐赠记录失败', error)
  }
}

const handleSubmitDonation = async () => {
  if (!donationFormRef.value) return
  await donationFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          ...donationForm,
          donorId: currentUserId.value // 从 store 获取当前用户 ID
        }
        const res = await submitDonation(submitData)
        if (res.code === 200) {
          ElMessage.success('捐赠提交成功')
          handleReset()
          fetchMyDonations()
        }
      } catch (error) {
        ElMessage.error('提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleReset = () => {
  donationFormRef.value?.resetFields()
  Object.assign(donationForm, {
    type: '',
    materialName: '',
    quantity: 1,
    unit: '',
    source: 'personal',
    donorUnit: '',
    contactPerson: '',
    contactPhone: '',
    receiveAddress: '',
    productionDate: '',
    expiryDate: ''
  })
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已接收',
    rejected: '已驳回'
  }
  return map[status] || status
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchMyDonations()
})
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

.donation-list-container {
  margin-bottom: 20px;
}

.donation-list {
  max-height: 400px;
  overflow-y: auto;
}

.donation-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.donation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.donation-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.donation-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8c8c8c;
}

.donation-tips {
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

<template>
  <div class="user-donation">
    <div class="page-header">
      <h2 class="page-title">我要捐赠</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="card-container">
          <div class="donation-steps">
            <el-steps :active="currentStep" finish-status="success" align-center>
              <el-step title="填写物资信息" />
              <el-step title="确认捐赠信息" />
              <el-step title="提交成功" />
            </el-steps>
          </div>
          
          <el-form v-show="currentStep === 0" ref="donationFormRef" :model="donationForm" :rules="donationRules" label-width="120px" class="donation-form">
            <div class="form-section">
              <h3 class="section-title">物资信息</h3>
              <el-form-item label="物资名称" prop="name">
                <el-input v-model="donationForm.name" placeholder="请输入物资名称" />
              </el-form-item>
              <el-form-item label="物资类型" prop="type">
                <el-select v-model="donationForm.type" placeholder="请选择物资类型" style="width: 100%">
                  <el-option label="防护物资（口罩、防护服等）" value="protective" />
                  <el-option label="消杀物资（消毒液、酒精等）" value="disinfection" />
                  <el-option label="检测物资（检测试剂等）" value="testing" />
                  <el-option label="医疗设备" value="equipment" />
                  <el-option label="其他物资" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="捐赠数量" prop="quantity">
                <el-input-number v-model="donationForm.quantity" :min="1" :max="1000000" style="width: 100%" />
              </el-form-item>
              <el-form-item label="单位" prop="unit">
                <el-select v-model="donationForm.unit" placeholder="请选择单位" style="width: 100%">
                  <el-option label="个" value="个" />
                  <el-option label="箱" value="箱" />
                  <el-option label="套" value="套" />
                  <el-option label="瓶" value="瓶" />
                  <el-option label="盒" value="盒" />
                  <el-option label="台" value="台" />
                </el-select>
              </el-form-item>
              <el-form-item label="生产日期">
                <el-date-picker
                  v-model="donationForm.productionDate"
                  type="date"
                  placeholder="请选择生产日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="有效期至">
                <el-date-picker
                  v-model="donationForm.expiryDate"
                  type="date"
                  placeholder="请选择有效期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>
            
            <div class="form-section">
              <h3 class="section-title">捐赠单位信息</h3>
              <el-form-item label="捐赠单位名称" prop="donorUnit">
                <el-input v-model="donationForm.donorUnit" placeholder="请输入捐赠单位名称" />
              </el-form-item>
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="donationForm.contactPerson" placeholder="请输入联系人姓名" />
              </el-form-item>
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="donationForm.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="收货地址" prop="receiveAddress">
                <el-input v-model="donationForm.receiveAddress" placeholder="请输入收货地址" />
              </el-form-item>
            </div>
            
            <div class="form-section">
              <h3 class="section-title">其他信息</h3>
              <el-form-item label="物资来源">
                <el-input v-model="donationForm.source" placeholder="如：采购/自制/社会捐赠" />
              </el-form-item>
              <el-form-item label="物资说明">
                <el-input v-model="donationForm.remark" type="textarea" :rows="3" placeholder="请输入物资说明（如规格型号、使用说明等）" />
              </el-form-item>
              <el-form-item label="相关凭证">
                <el-upload
                  class="upload-demo"
                  action="#"
                  :auto-upload="false"
                  :limit="5"
                  list-type="picture-card"
                  :on-change="handleFileChange"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <div class="upload-tip">支持上传采购发票、产品合格证等图片，最多5张</div>
              </el-form-item>
            </div>
            
            <el-form-item>
              <el-button type="primary" @click="handleNextStep">下一步</el-button>
            </el-form-item>
          </el-form>
          
          <div v-show="currentStep === 1" class="confirm-section">
            <el-descriptions title="捐赠信息确认" :column="1" border class="confirm-info">
              <el-descriptions-item label="物资名称">{{ donationForm.name }}</el-descriptions-item>
              <el-descriptions-item label="物资类型">{{ getTypeName(donationForm.type) }}</el-descriptions-item>
              <el-descriptions-item label="捐赠数量">{{ donationForm.quantity }} {{ donationForm.unit }}</el-descriptions-item>
              <el-descriptions-item label="有效期至">{{ donationForm.expiryDate || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="捐赠单位">{{ donationForm.donorUnit }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ donationForm.contactPerson }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ donationForm.contactPhone }}</el-descriptions-item>
              <el-descriptions-item label="收货地址">{{ donationForm.receiveAddress }}</el-descriptions-item>
            </el-descriptions>
            
            <div class="confirm-notice">
              <el-icon><InfoFilled /></el-icon>
              <span>请确认以上信息准确无误，提交后将进入审核流程</span>
            </div>
            
            <div class="confirm-actions">
              <el-button @click="currentStep = 0">上一步</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">确认提交</el-button>
            </div>
          </div>
          
          <div v-show="currentStep === 2" class="success-section">
            <div class="success-icon">
              <el-icon :size="64" color="#52c41a"><CircleCheck /></el-icon>
            </div>
            <h3 class="success-title">捐赠提交成功！</h3>
            <p class="success-desc">您的捐赠信息已提交成功，预计1-2个工作日内完成审核</p>
            <div class="success-info">
              <p>捐赠单号：<span class="highlight">D2026024001</span></p>
              <p>审核状态：<el-tag type="warning">待审核</el-tag></p>
            </div>
            <div class="success-actions">
              <el-button type="primary" @click="$router.push('/user/my-donation')">查看我的捐赠</el-button>
              <el-button @click="handleReset">继续捐赠</el-button>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <div class="card-container donation-guide">
          <h3 class="section-title">捐赠指南</h3>
          <div class="guide-list">
            <div class="guide-item">
              <div class="guide-step">1</div>
              <div class="guide-content">
                <div class="guide-title">填写物资信息</div>
                <div class="guide-desc">如实填写捐赠物资的详细信息</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-step">2</div>
              <div class="guide-content">
                <div class="guide-title">提交审核</div>
                <div class="guide-desc">1-2个工作日内完成审核</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-step">3</div>
              <div class="guide-content">
                <div class="guide-title">配送物资</div>
                <div class="guide-desc">审核通过后配送至指定仓库</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-step">4</div>
              <div class="guide-content">
                <div class="guide-title">获取证书</div>
                <div class="guide-desc">获得捐赠荣誉证书</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card-container donation-contact">
          <h3 class="section-title">咨询热线</h3>
          <div class="contact-info">
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>400-888-8888</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>donation@epidemic.gov</span>
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
import { Plus, InfoFilled, CircleCheck, Phone, Message } from '@element-plus/icons-vue'
import { submitDonation } from '@/api/donation'

// 表单引用和状态变量
const donationFormRef = ref(null)
const currentStep = ref(0) // 当前步骤：0-填写信息, 1-确认信息, 2-提交成功
const submitting = ref(false)

// 捐赠表单数据模型
const donationForm = reactive({
  name: '', // 物资名称
  type: '', // 物资类型
  quantity: 1, // 数量
  unit: '', // 单位
  productionDate: '', // 生产日期
  expiryDate: '', // 有效期
  donorUnit: '', // 捐赠单位
  contactPerson: '', // 联系人
  contactPhone: '', // 联系电话
  receiveAddress: '', // 收货地址
  source: '', // 来源
  remark: '' // 备注
})

// 表单校验规则
const donationRules = {
  name: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择物资类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入捐赠数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  donorUnit: [{ required: true, message: '请输入捐赠单位名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiveAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

/**
 * 获取物资类型显示名称
 * @param {string} type - 物资类型代码
 * @returns {string} 物资类型名称
 */
const getTypeName = (type) => {
  const typeMap = {
    protective: '防护物资',
    disinfection: '消杀物资',
    testing: '检测物资',
    equipment: '医疗设备',
    other: '其他物资'
  }
  return typeMap[type] || type
}

/**
 * 处理下一步点击事件
 * 校验第一步表单，通过后进入确认步骤
 */
const handleNextStep = async () => {
  if (!donationFormRef.value) return
  await donationFormRef.value.validate((valid) => {
    if (valid) {
      currentStep.value = 1
    }
  })
}

/**
 * 提交捐赠申请
 * 调用后端接口提交数据
 */
const handleSubmit = async () => {
  submitting.value = true
  try {
    const submitData = {
      ...donationForm,
      materialName: donationForm.name // 后端字段名为 materialName
    }
    // 移除不必要的字段
    delete submitData.name
    
    const res = await submitDonation(submitData)
    if (res.code === 200) {
      currentStep.value = 2 // 进入成功页面
      ElMessage.success('捐赠提交成功')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单，继续捐赠
 */
const handleReset = () => {
  donationFormRef.value?.resetFields()
  // 手动重置所有字段，因为 resetFields 可能无法覆盖所有响应式属性
  Object.assign(donationForm, {
    name: '',
    type: '',
    quantity: 1,
    unit: '',
    productionDate: '',
    expiryDate: '',
    donorUnit: '',
    contactPerson: '',
    contactPhone: '',
    receiveAddress: '',
    source: '',
    remark: ''
  })
  currentStep.value = 0
}

// 文件上传变化处理（暂未实现实际上传逻辑）
const handleFileChange = (file, files) => {
  console.log(files)
}
</script>

<style scoped lang="scss">
.user-donation {
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

.donation-steps {
  margin-bottom: 40px;
  padding: 20px 0;
}

.form-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #e8e8e8;
  
  &:last-of-type {
    border-bottom: none;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
}

.confirm-section {
  padding: 20px 0;
}

.confirm-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #e6f7ff;
  border-radius: 8px;
  margin: 24px 0;
  color: #1890ff;
  font-size: 14px;
}

.confirm-actions {
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
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 32px;
  
  p {
    margin: 8px 0;
    font-size: 14px;
    color: #595959;
  }
  
  .highlight {
    color: #1890ff;
    font-weight: 600;
  }
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.donation-guide {
  margin-bottom: 20px;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-item {
  display: flex;
  gap: 16px;
}

.guide-step {
  width: 32px;
  height: 32px;
  background: #1890ff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.guide-content {
  flex: 1;
}

.guide-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.guide-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.donation-contact {
  margin-top: 20px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #595959;
  
  .el-icon {
    color: #1890ff;
  }
}
</style>

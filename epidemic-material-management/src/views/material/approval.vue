<template>
  <div class="material-approval">
    <div class="page-header">
      <h2 class="page-title">审核审批</h2>
    </div>
    
    <div class="card-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="物资申领审核" name="application">
          <el-table :data="applicationList" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="申请单号" width="180" />
            <el-table-column prop="applicantId" label="申请人ID" width="120" />
            <el-table-column prop="materialId" label="物资ID" width="120" />
            <el-table-column prop="quantity" label="申请数量" width="120" />
            <el-table-column prop="urgency" label="紧急程度" width="120">
              <template #default="scope">
                <el-tag :type="getUrgencyType(scope.row.urgency)">{{ getUrgencyText(scope.row.urgency) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="purpose" label="用途" />
            <el-table-column prop="applyTime" label="申请时间" width="180">
              <template #default="scope">{{ formatDate(scope.row.applyTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="success" link @click="handleApprove(scope.row, 'approved')">通过</el-button>
                <el-button type="danger" link @click="handleApprove(scope.row, 'rejected')">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="appPage.page"
              v-model:page-size="appPage.size"
              :total="appPage.total"
              layout="total, prev, pager, next"
              @current-change="fetchApplications"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="申领审核记录" name="applicationRecord">
          <el-table :data="applicationRecordList" v-loading="recordLoading" style="width: 100%">
            <el-table-column prop="id" label="申请单号" width="180" />
            <el-table-column prop="materialName" label="物资名称" width="150" />
            <el-table-column prop="quantity" label="申请数量" width="100" />
            <el-table-column prop="urgency" label="紧急程度" width="100">
              <template #default="scope">
                <el-tag :type="getUrgencyType(scope.row.urgency)">{{ getUrgencyText(scope.row.urgency) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="100" />
            <el-table-column prop="applicantUnit" label="申请单位" width="150" />
            <el-table-column prop="receiver" label="收货人" width="100" />
            <el-table-column prop="receiverPhone" label="收货电话" width="130" />
            <el-table-column prop="status" label="审核状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'approved' ? 'success' : 'danger'">
                  {{ scope.row.status === 'approved' ? '已通过' : '已驳回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approveTime" label="审核时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.approveTime) }}</template>
            </el-table-column>
            <el-table-column prop="approverName" label="审核人" width="100" />
            <el-table-column prop="approveRemark" label="审核备注" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="appRecordPage.page"
              v-model:page-size="appRecordPage.size"
              :total="appRecordPage.total"
              layout="total, prev, pager, next"
              @current-change="fetchApplicationRecords"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="物资捐赠审核" name="donation">
          <el-table :data="donationList" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="捐赠单号" width="180" />
            <el-table-column prop="donorUnit" label="捐赠方" width="180" />
            <el-table-column prop="materialName" label="物资名称" width="150" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="contactPerson" label="联系人" width="120" />
            <el-table-column prop="contactPhone" label="电话" width="150" />
            <el-table-column prop="donateTime" label="提交时间" width="180">
              <template #default="scope">{{ formatDate(scope.row.donateTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="success" link @click="handleApproveDonation(scope.row, 'approved')">接收</el-button>
                <el-button type="danger" link @click="handleApproveDonation(scope.row, 'rejected')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="donationPage.page"
              v-model:page-size="donationPage.size"
              :total="donationPage.total"
              layout="total, prev, pager, next"
              @current-change="fetchDonations"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="捐赠审核记录" name="donationRecord">
          <el-table :data="donationRecordList" v-loading="recordLoading" style="width: 100%">
            <el-table-column prop="id" label="捐赠单号" width="180" />
            <el-table-column prop="donorUnit" label="捐赠方" width="180" />
            <el-table-column prop="materialName" label="物资名称" width="150" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="contactPerson" label="联系人" width="120" />
            <el-table-column prop="contactPhone" label="电话" width="150" />
            <el-table-column prop="source" label="来源" width="120" />
            <el-table-column prop="status" label="审核状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'approved' ? 'success' : 'danger'">
                  {{ scope.row.status === 'approved' ? '已通过' : '已驳回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approveTime" label="审核时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.approveTime) }}</template>
            </el-table-column>
            <el-table-column prop="approverName" label="审核人" width="100" />
            <el-table-column prop="approveRemark" label="审核备注" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="donRecordPage.page"
              v-model:page-size="donRecordPage.size"
              :total="donRecordPage.total"
              layout="total, prev, pager, next"
              @current-change="fetchDonationRecords"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="审核处理" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-tag :type="approveForm.status === 'approved' ? 'success' : 'danger'">
            {{ approveForm.status === 'approved' ? '通过' : '驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmApprove" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getApplicationList, approveApplication } from '@/api/application'
import { getDonationList, approveDonation } from '@/api/donation'
import dayjs from 'dayjs'

const activeTab = ref('application')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const applicationList = ref([])
const appPage = reactive({ page: 1, size: 10, total: 0 })

// 已审核的申请记录
const applicationRecordList = ref([])
const appRecordPage = reactive({ page: 1, size: 10, total: 0 })

const donationList = ref([])
const donationPage = reactive({ page: 1, size: 10, total: 0 })

// 已审核的捐赠记录
const donationRecordList = ref([])
const donRecordPage = reactive({ page: 1, size: 10, total: 0 })

const recordLoading = ref(false)

const approveForm = reactive({
  id: '',
  type: '', // application or donation
  status: '',
  remark: ''
})

const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await getApplicationList({ 
      page: appPage.page, 
      size: appPage.size, 
      status: 'pending' 
    })
    if (res.code === 200) {
      applicationList.value = res.data.list || []
      appPage.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchDonations = async () => {
  loading.value = true
  try {
    const res = await getDonationList({
      page: donationPage.page,
      size: donationPage.size,
      status: 'pending'
    })
    if (res.code === 200) {
      donationList.value = res.data.list || []
      donationPage.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取捐赠列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取已审核的申请记录
const fetchApplicationRecords = async () => {
  recordLoading.value = true
  try {
    // 获取已通过的申请
    const approvedRes = await getApplicationList({
      page: appRecordPage.page,
      size: appRecordPage.size,
      status: 'approved'
    })
    // 获取已驳回的申请
    const rejectedRes = await getApplicationList({
      page: appRecordPage.page,
      size: appRecordPage.size,
      status: 'rejected'
    })

    let approvedList = approvedRes.data?.list || []
    let rejectedList = rejectedRes.data?.list || []

    // 合并列表
    applicationRecordList.value = [...approvedList, ...rejectedList]
    const totalApproved = approvedRes.data?.total || 0
    const totalRejected = rejectedRes.data?.total || 0
    appRecordPage.total = totalApproved + totalRejected
  } catch (error) {
    console.error('获取申请审核记录失败', error)
  } finally {
    recordLoading.value = false
  }
}

// 获取已审核的捐赠记录
const fetchDonationRecords = async () => {
  recordLoading.value = true
  try {
    // 获取已通过的捐赠
    const approvedRes = await getDonationList({
      page: donRecordPage.page,
      size: donRecordPage.size,
      status: 'approved'
    })
    // 获取已拒绝的捐赠
    const rejectedRes = await getDonationList({
      page: donRecordPage.page,
      size: donRecordPage.size,
      status: 'rejected'
    })

    let approvedList = approvedRes.data?.list || []
    let rejectedList = rejectedRes.data?.list || []

    // 合并列表
    donationRecordList.value = [...approvedList, ...rejectedList]
    const totalApproved = approvedRes.data?.total || 0
    const totalRejected = rejectedRes.data?.total || 0
    donRecordPage.total = totalApproved + totalRejected
  } catch (error) {
    console.error('获取捐赠审核记录失败', error)
  } finally {
    recordLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'application') {
    fetchApplications()
  } else if (tab === 'applicationRecord') {
    fetchApplicationRecords()
  } else if (tab === 'donation') {
    fetchDonations()
  } else if (tab === 'donationRecord') {
    fetchDonationRecords()
  }
}

const handleApprove = (row, status) => {
  approveForm.id = row.id
  approveForm.type = 'application'
  approveForm.status = status
  approveForm.remark = ''
  dialogVisible.value = true
}

const handleApproveDonation = (row, status) => {
  approveForm.id = row.id
  approveForm.type = 'donation'
  approveForm.status = status
  approveForm.remark = ''
  dialogVisible.value = true
}

const confirmApprove = async () => {
  submitting.value = true
  try {
    let res
    if (approveForm.type === 'application') {
      res = await approveApplication({
        applicationId: approveForm.id,
        status: approveForm.status,
        remark: approveForm.remark
      })
    } else {
      res = await approveDonation({
        donationId: approveForm.id,
        status: approveForm.status,
        remark: approveForm.remark
      })
    }
    
    if (res.code === 200) {
      ElMessage.success('审核完成')
      dialogVisible.value = false
      if (approveForm.type === 'application') {
        fetchApplications()
      } else {
        fetchDonations()
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const getUrgencyType = (urgency) => {
  const map = {
    normal: 'info',
    urgent: 'warning',
    critical: 'danger'
  }
  return map[urgency] || 'info'
}

const getUrgencyText = (urgency) => {
  const map = {
    normal: '普通',
    urgent: '较急',
    critical: '紧急'
  }
  return map[urgency] || urgency
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped lang="scss">
.material-approval {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

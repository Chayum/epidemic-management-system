<template>
  <div class="notification-page">
    <div class="page-header">
      <h2 class="page-title">我的消息</h2>
      <el-button type="primary" link @click="handleMarkAllAsRead" v-if="unreadCount > 0">
        全部标为已读
      </el-button>
    </div>

    <div class="filter-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="未读" name="unread">
          <template #label>
            <span>未读 <el-badge :value="unreadCount" :max="99" v-if="unreadCount > 0" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已读" name="read" />
      </el-tabs>
    </div>

    <div class="notification-list" v-loading="loading">
      <div v-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无消息" />
      </div>

      <div
        v-for="item in tableData"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClick(item)"
      >
        <div class="notification-icon" :class="item.type">
          <el-icon v-if="item.type === 'push'" :size="24"><Bell /></el-icon>
          <el-icon v-else :size="24"><Message /></el-icon>
        </div>
        <div class="notification-content">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-text">{{ item.content }}</div>
          <div class="notification-time">{{ item.createTime }}</div>
        </div>
        <div class="notification-action">
          <el-tag v-if="item.isRead === 0" type="warning" size="small">未读</el-tag>
          <el-tag v-else type="success" size="small" plain>已读</el-tag>
          <el-button type="danger" link size="small" class="delete-btn" @click.stop="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Message } from '@element-plus/icons-vue'
import { getMyNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const unreadCount = ref(0)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

let refreshTimer = null

const fetchData = async () => {
  loading.value = true
  try {
    const isRead = activeTab.value === 'unread' ? 0 : activeTab.value === 'read' ? 1 : undefined
    const res = await getMyNotifications({
      page: pagination.page,
      size: pagination.size,
      isRead
    })
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取通知列表失败', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

const handleSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const handlePageChange = () => {
  fetchData()
}

const handleClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

const handleMarkAllAsRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    unreadCount.value = 0
    fetchData()
  } catch (error) {
    console.error('标记全部已读失败', error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotification(item.id)
    ElMessage.success('删除成功')
    if (item.isRead === 0) {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  fetchUnreadCount()
  fetchData()

  // 每30秒刷新未读数量
  refreshTimer = setInterval(() => {
    fetchUnreadCount()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 暴露方法给外部调用（如从布局组件）
defineExpose({
  fetchUnreadCount
})
</script>

<style scoped lang="scss">
.notification-page {
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

.filter-tabs {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.notification-list {
  background: #fff;
  border-radius: 12px;
  padding: 8px 0;
}

.empty-state {
  padding: 60px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #fafafa;
  }

  &.unread {
    background: #f0f7ff;

    &:hover {
      background: #e6f0ff;
    }
  }
}

.notification-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #e6f7ff;
  color: #1890ff;
  flex-shrink: 0;

  &.system {
    background: #fff7e6;
    color: #fa8c16;
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #c0c0c0;
}

.notification-action {
  flex-shrink: 0;
  align-self: center;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.delete-btn {
  margin-top: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
}
</style>
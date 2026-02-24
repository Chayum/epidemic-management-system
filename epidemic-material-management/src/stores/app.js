import { defineStore } from 'pinia'
import { ref } from 'vue'

// 定义应用状态 store
export const useAppStore = defineStore('app', () => {
  // 侧边栏是否折叠
  const sidebarCollapsed = ref(false)
  
  // 全局加载状态
  const loading = ref(false)
  
  // 当前激活的菜单项
  const activeMenu = ref('')
  
  // 消息通知列表
  const notifications = ref([])

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置加载状态
  const setLoading = (status) => {
    loading.value = status
  }

  // 设置激活菜单
  const setActiveMenu = (menu) => {
    activeMenu.value = menu
  }

  // 添加通知
  const addNotification = (notification) => {
    notifications.value.unshift({
      id: Date.now(),
      ...notification,
      read: false,
      time: new Date().toLocaleString()
    })
  }

  // 标记通知已读
  const markAsRead = (id) => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  // 清除所有已读通知
  const clearReadNotifications = () => {
    notifications.value = notifications.value.filter(n => !n.read)
  }

  return {
    sidebarCollapsed,
    loading,
    activeMenu,
    notifications,
    toggleSidebar,
    setLoading,
    setActiveMenu,
    addNotification,
    markAsRead,
    clearReadNotifications
  }
})

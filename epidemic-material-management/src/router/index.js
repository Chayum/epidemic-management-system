import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/user/list',
    redirect: '/user-manage/list'
  },
  {
    path: '/user/login',
    redirect: to => ({ path: '/login', query: { type: 'user' } })
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/user',
    component: () => import('@/layout/user.vue'),
    redirect: '/user/home',
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/home.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'donation',
        name: 'UserDonation',
        component: () => import('@/views/user/donation.vue'),
        meta: { title: '我要捐赠', requiresAuth: true, roles: ['donor', 'community_staff', 'hospital_user'] }
      },
      {
        path: 'apply',
        name: 'UserApply',
        component: () => import('@/views/user/apply.vue'),
        meta: { title: '物资申领', requiresAuth: true, roles: ['hospital_user', 'community_staff'] }
      },
      {
        path: 'my-application',
        name: 'UserMyApplication',
        component: () => import('@/views/user/my-application.vue'),
        meta: { title: '我的申请', requiresAuth: true, roles: ['hospital_user', 'community_staff'] }
      },
      {
        path: 'my-donation',
        name: 'UserMyDonation',
        component: () => import('@/views/user/my-donation.vue'),
        meta: { title: '我的捐赠', requiresAuth: true, roles: ['donor', 'community_staff', 'hospital_user'] }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'Monitor', requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        meta: { title: '用户管理', icon: 'User', requiresAuth: true, roles: ['admin'] },
        children: [
          {
            path: '',
            redirect: 'list'
          },
          {
            path: 'list',
            name: 'UserList',
            component: () => import('@/views/user/list.vue'),
            meta: { title: '用户列表', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'profile',
            name: 'UserProfileManage',
            component: () => import('@/views/user/profile.vue'),
            meta: { title: '个人信息', requiresAuth: true, roles: ['admin'] }
          }
        ]
      },
      {
        path: 'material',
        name: 'Material',
        meta: { title: '物资管理', icon: 'Box', requiresAuth: true, roles: ['admin'] },
        children: [
          {
            path: '',
            redirect: 'donation'
          },
          {
            path: 'donation',
            name: 'MaterialDonation',
            component: () => import('@/views/material/donation.vue'),
            meta: { title: '物资捐赠', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'inventory',
            name: 'MaterialInventory',
            component: () => import('@/views/material/inventory.vue'),
            meta: { title: '库存管理', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'apply',
            name: 'MaterialApply',
            component: () => import('@/views/material/apply.vue'),
            meta: { title: '物资申领', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'approval',
            name: 'MaterialApproval',
            component: () => import('@/views/material/approval.vue'),
            meta: { title: '申领审批', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'track',
            name: 'MaterialTrack',
            component: () => import('@/views/material/track.vue'),
            meta: { title: '物资追踪', requiresAuth: true, roles: ['admin'] }
          }
        ]
      },
      {
        path: 'pandemic',
        name: 'Pandemic',
        meta: { title: '疫情信息', icon: 'DataAnalysis', requiresAuth: true, roles: ['admin'] },
        children: [
          {
            path: '',
            redirect: 'news'
          },
          {
            path: 'news',
            name: 'PandemicNews',
            component: () => import('@/views/pandemic/news.vue'),
            meta: { title: '疫情动态', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'push',
            name: 'PandemicPush',
            component: () => import('@/views/pandemic/push.vue'),
            meta: { title: '消息推送', requiresAuth: true, roles: ['admin'] }
          },
          {
            path: 'knowledge',
            name: 'PandemicKnowledge',
            component: () => import('@/views/pandemic/knowledge.vue'),
            meta: { title: '防控知识库', requiresAuth: true, roles: ['admin'] }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const title = to.meta.title
  if (title) {
    document.title = `${title} - 疫情防控物资调度管理系统`
  }

  const getLoginPath = () => (to.path.startsWith('/user') ? '/user/login' : '/login')

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next(getLoginPath())
    return
  }

  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userStore.userRole)) {
      next(getLoginPath())
      return
    }
  }

  // 公开页面或者已登录且有权限
  next()
})

export default router

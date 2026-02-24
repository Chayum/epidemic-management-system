import Mock from 'mockjs'

// 设置拦截 Ajax 请求的延时
Mock.setup({
  timeout: '200-600'
})

// 登录接口
Mock.mock('/api/auth/login', 'post', (options) => {
  const body = JSON.parse(options.body)
  return {
    code: 200,
    message: '登录成功',
    data: {
      token: 'mock-token-' + Date.now(),
      userInfo: {
        id: 1,
        username: body.username,
        name: body.username === 'admin' ? '管理员' : body.username,
        role: body.role || 'admin',
        phone: '13800138000',
        unit: body.role === 'admin' ? '疫情防控指挥部' : '市第一医院'
      }
    }
  }
})

// 获取用户信息
Mock.mock('/api/auth/info', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      id: 1,
      username: 'admin',
      name: '管理员',
      role: 'admin',
      phone: '13800138000',
      unit: '疫情防控指挥部'
    }
  }
})

// 获取仪表盘统计
Mock.mock('/api/stats/dashboard', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      totalMaterials: 128,
      pendingApplications: 15,
      pendingDonations: 8,
      lowStockItems: 23
    }
  }
})

// 获取物资列表
Mock.mock(/\/api\/material\/list.*/, 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      list: [
        { id: 'M001', name: 'N95医用口罩', type: 'protective', stock: 8500, unit: '个', threshold: 5000, status: 'normal' },
        { id: 'M002', name: '医用防护服', type: 'protective', stock: 4200, unit: '套', threshold: 3000, status: 'normal' },
        { id: 'M003', name: '84消毒液', type: 'disinfection', stock: 25000, unit: '瓶', threshold: 20000, status: 'normal' },
        { id: 'M004', name: '医用酒精', type: 'disinfection', stock: 6800, unit: '瓶', threshold: 5000, status: 'normal' },
        { id: 'M005', name: '检测试剂', type: 'testing', stock: 3200, unit: '盒', threshold: 5000, status: 'warning' }
      ],
      total: 5
    }
  }
})

// 获取申请列表
Mock.mock(/\/api\/application\/list.*/, 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      list: [
        { id: 'A2026024001', materialName: 'N95医用口罩', quantity: 1000, unit: '个', applicant: '市第一医院', applyTime: '2026-02-24 10:30:00', status: 'pending', urgency: 'critical' },
        { id: 'A2026024002', materialName: '医用防护服', quantity: 500, unit: '套', applicant: '市第二医院', applyTime: '2026-02-24 09:45:00', status: 'approved', urgency: 'urgent' }
      ],
      total: 2
    }
  }
})

// 获取捐赠列表
Mock.mock(/\/api\/donation\/list.*/, 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      list: [
        { id: 'D2026024001', materialName: 'N95医用口罩', quantity: 5000, unit: '个', donorUnit: '市慈善总会', donateTime: '2026-02-24 10:30:00', status: 'pending' },
        { id: 'D2026024002', materialName: '84消毒液', quantity: 200, unit: '箱', donorUnit: '爱心企业A', donateTime: '2026-02-24 09:15:00', status: 'approved' }
      ],
      total: 2
    }
  }
})

// 获取疫情新闻
Mock.mock(/\/api\/pandemic\/news.*/, 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      list: [
        { id: 'N001', title: '我市新增3例确诊病例', summary: '今日新增3例确诊病例，均在隔离管控中发现', publishTime: '2026-02-24 09:00:00', status: 'published' },
        { id: 'N002', title: '疫情防控物资储备充足', summary: '我市防疫物资储备充足，请市民无需担心', publishTime: '2026-02-23 15:00:00', status: 'published' }
      ],
      total: 2
    }
  }
})

// 提交申请
Mock.mock('/api/application', 'post', () => {
  return {
    code: 200,
    message: '提交成功',
    data: { id: 'A' + Date.now() }
  }
})

// 提交捐赠
Mock.mock('/api/donation', 'post', () => {
  return {
    code: 200,
    message: '提交成功',
    data: { id: 'D' + Date.now() }
  }
})

console.log('[Mock] Mock 数据已加载')

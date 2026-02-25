import { describe, it, expect, vi } from 'vitest'
import { getDashboardStats, getTrendData } from '@/api/stats'
import request from '@/utils/request'

// Mock request module
vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

describe('Stats API', () => {
  it('getDashboardStats calls correct endpoint', async () => {
    const mockData = { code: 200, data: { total: 100 } }
    request.mockResolvedValue(mockData)

    const result = await getDashboardStats()
    
    expect(request).toHaveBeenCalledWith({
      url: '/stats/dashboard',
      method: 'get'
    })
    expect(result).toEqual(mockData)
  })

  it('getTrendData calls correct endpoint with params', async () => {
    const mockData = { code: 200, data: {} }
    request.mockResolvedValue(mockData)
    
    const params = { period: 'week' }
    const result = await getTrendData(params)
    
    expect(request).toHaveBeenCalledWith({
      url: '/stats/trend',
      method: 'get',
      params
    })
    expect(result).toEqual(mockData)
  })
})

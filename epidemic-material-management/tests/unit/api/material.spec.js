import { describe, it, expect, vi } from 'vitest'
import { getMaterialList } from '@/api/material'
import request from '@/utils/request'

vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

describe('Material API', () => {
  it('getMaterialList calls correct endpoint', async () => {
    const mockData = { code: 200, data: { list: [] } }
    request.mockResolvedValue(mockData)
    
    const params = { page: 1, size: 10 }
    const result = await getMaterialList(params)
    
    expect(request).toHaveBeenCalledWith({
      url: '/material/list',
      method: 'get',
      params
    })
    expect(result).toEqual(mockData)
  })
})

import request from '@/utils/request'

/**
 * 创建订单
 * @param {Array<{cartItemId: number, price: number, quantity: number}>} items 下单明细，携带用户确认的单价；为空则下单全部
 */
export function createOrder(items) {
  return request.post('/v1/order', items || [])
}

/** 查询订单详情 */
export function getOrderById(id) {
  return request.get(`/v1/order/${id}`)
}

/** 分页查询当前用户订单列表 */
export function getOrderPage(params) {
  return request.get('/v1/orders', { params })
}

/** 取消订单（仅限待支付状态） */
export function cancelOrder(id) {
  return request.put(`/v1/order/${id}/cancel`)
}

/** 更新订单状态（管理员操作） */
export function updateOrderStatus(id, data) {
  return request.put(`/v1/order/${id}/status`, data)
}

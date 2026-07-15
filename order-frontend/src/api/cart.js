import request from '@/utils/request'

export function getCartList() {
  return request.get('/v1/cart')
}

export function addToCart(data) {
  return request.post('/v1/cart', data)
}

export function updateCartQuantity(id, data) {
  return request.put(`/v1/cart/${id}`, data)
}

export function deleteCartItem(id) {
  return request.delete(`/v1/cart/${id}`)
}

export function clearCart() {
  return request.delete('/v1/cart')
}

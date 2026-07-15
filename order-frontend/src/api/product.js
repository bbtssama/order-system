import request from '@/utils/request'

export function getProductPage(params) {
  return request.get('/v1/products', { params })
}

export function getProductById(id) {
  return request.get(`/v1/product/${id}`)
}

export function getProductsByCategory(categoryId, params) {
  return request.get(`/v1/products/category/${categoryId}`, { params })
}

export function createProduct(data) {
  return request.post('/v1/product', data)
}

export function updateProduct(data) {
  return request.put('/v1/product', data)
}

export function deleteProduct(id) {
  return request.delete(`/v1/product/${id}`)
}

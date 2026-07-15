import request from '@/utils/request'

export function getCategories() {
  return request.get('/v1/categories')
}

export function createCategory(data) {
  return request.post('/v1/category', data)
}

export function updateCategory(data) {
  return request.put('/v1/category', data)
}

export function deleteCategory(id) {
  return request.delete(`/v1/category/${id}`)
}

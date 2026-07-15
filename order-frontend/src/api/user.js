import request from '@/utils/request'

export function login(username, password) {
  return request.post('/v1/user/login', { username, password })
}

export function register(userData) {
  return request.post('/v1/user/register', userData)
}

export function getUserInfo() {
  return request.get('/v1/user/info')
}

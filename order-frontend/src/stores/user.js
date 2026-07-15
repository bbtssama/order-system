import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  function setToken(val) {
    token.value = val
    if (val) {
      localStorage.setItem('token', val)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  async function login(username, password) {
    const res = await loginApi(username, password)
    // 响应拦截器已返回 R 对象 {code, msg, data}，res.data 即为 token 字符串
    setToken(res.data)
    await fetchUserInfo()
    return res
  }

  async function register(userData) {
    return await registerApi(userData)
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      // res.data 即为 User 对象（拦截器已解包 R 结构）
      setUserInfo(res.data)
    } catch (e) {
      setUserInfo(null)
      setToken('')
    }
  }

  function logout() {
    setToken('')
    setUserInfo(null)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    fetchUserInfo,
    logout
  }
})

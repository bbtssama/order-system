import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由表定义
 * meta.requireAuth: 需要登录才能访问
 * meta.requireAdmin: 需要管理员角色才能访问
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'ProductList',
    component: () => import('@/views/ProductList.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue')
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/admin/products',
    name: 'ProductManage',
    component: () => import('@/views/ProductManage.vue'),
    meta: { requireAuth: true, requireAdmin: true }
  },
  {
    path: '/admin/categories',
    name: 'CategoryManage',
    component: () => import('@/views/CategoryManage.vue'),
    meta: { requireAuth: true, requireAdmin: true }
  },
  {
    /** 兜底 404 路由，匹配所有未定义路径 */
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  /** 切换路由时滚动到页面顶部 */
  scrollBehavior() {
    return { top: 0 }
  }
})

/**
 * 全局前置守卫：
 * 1. 未登录用户访问受保护页面 → 重定向到登录页
 * 2. 非管理员访问管理页面 → 重定向到首页
 */
router.beforeEach((to) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  // 需要登录但未登录 → 跳转登录页，并记录来源地址
  if (to.meta.requireAuth && !isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 需要管理员权限但当前用户非管理员 → 跳转首页
  if (to.meta.requireAdmin && !userStore.isAdmin) {
    return { path: '/' }
  }

  // 已登录用户访问登录/注册页 → 跳转首页
  if (to.meta.public && isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    return { path: '/' }
  }

  return true
})

export default router

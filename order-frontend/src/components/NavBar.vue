<template>
  <el-menu mode="horizontal" :ellipsis="false" class="navbar">
    <div class="nav-left">
      <el-menu-item index="/" @click="$router.push('/')">
        <h2 style="margin:0;color:#409eff;">订单系统</h2>
      </el-menu-item>
      <el-menu-item index="/cart" @click="$router.push('/cart')" v-if="userStore.isLoggedIn">
        <el-icon><ShoppingCart /></el-icon>
        购物车
        <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
      </el-menu-item>
      <el-menu-item index="/orders" @click="$router.push('/orders')" v-if="userStore.isLoggedIn">
        我的订单
      </el-menu-item>
      <template v-if="userStore.isAdmin">
        <el-menu-item index="/admin/products" @click="$router.push('/admin/products')">
          商品管理
        </el-menu-item>
        <el-menu-item index="/admin/categories" @click="$router.push('/admin/categories')">
          分类管理
        </el-menu-item>
      </template>
    </div>
    <div class="nav-right">
      <template v-if="userStore.isLoggedIn">
        <el-dropdown>
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ userStore.userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <el-menu-item index="/login" @click="$router.push('/login')">登录</el-menu-item>
        <el-menu-item index="/register" @click="$router.push('/register')">注册</el-menu-item>
      </template>
    </div>
  </el-menu>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCartList } from '@/api/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartCount = ref(0)

async function fetchCartCount() {
  if (!userStore.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const res = await getCartList()
    const list = res.data || []
    cartCount.value = list.reduce((sum, item) => sum + (item.quantity || 0), 0)
  } catch (e) {
    cartCount.value = 0
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchCartCount()
})

// 监听路由变化，刷新购物车数量（加入购物车后跳转时实时更新）
watch(() => route.path, () => {
  fetchCartCount()
})

defineExpose({ fetchCartCount })
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  border-bottom: solid 1px #e6e6e6;
}

.nav-left {
  display: flex;
  align-items: center;
}

.nav-right {
  display: flex;
  align-items: center;
}

.cart-badge {
  margin-left: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #333;
  padding: 0 16px;
  height: 60px;
  line-height: 60px;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}
</style>

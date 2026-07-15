<template>
  <div class="cart-page">
    <h2>我的购物车</h2>

    <!-- 未登录状态 -->
    <el-empty
      v-if="!userStore.isLoggedIn"
      description="尚未登录，请先登录后查看购物车"
    >
      <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
    </el-empty>

    <!-- 已登录：正常显示购物车 -->
    <div v-else v-loading="loading">
      <el-empty v-if="!loading && cartList.length === 0" description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>

      <template v-else-if="!loading">
        <el-table :data="cartList" style="width:100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品信息" min-width="300">
            <template #default="{ row }">
              <div class="cart-product">
                <el-image
                  :src="row.productImageUrl || 'https://placehold.co/80x80/e0e0e0/999'"
                  fit="cover"
                  style="width:80px;height:80px;border-radius:4px;flex-shrink:0;"
                />
                <div class="cart-product-info">
                  <p class="cart-product-name">{{ row.productName }}</p>
                  <p class="cart-product-price">￥{{ row.productPrice }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              <span style="color:#f56c6c;">￥{{ row.productPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="180" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.productStock"
                @change="(val) => handleQuantityChange(row, val)"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span style="color:#f56c6c;font-weight:600;">
                ￥{{ (row.productPrice * row.quantity).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer">
          <div class="cart-footer-left">
            <el-button @click="handleClearCart" :disabled="cartList.length === 0">清空购物车</el-button>
          </div>
          <div class="cart-footer-right">
            <span class="total-text">
              已选 <strong>{{ selectedItems.length }}</strong> 件商品，合计：
              <span class="total-price">￥{{ totalPrice.toFixed(2) }}</span>
            </span>
            <el-button
              type="primary"
              size="large"
              :disabled="selectedItems.length === 0"
              :loading="ordering"
              @click="handleCreateOrder"
            >
              立即下单
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, deleteCartItem, clearCart as clearCartApi } from '@/api/cart'
import { createOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartList = ref([])
const selectedItems = ref([])
const loading = ref(true)
const ordering = ref(false)

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + (item.productPrice || 0) * (item.quantity || 0)
  }, 0)
})

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data || []
  } catch (e) {
    cartList.value = []
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(selection) {
  selectedItems.value = selection
}

async function handleQuantityChange(row, val) {
  try {
    await updateCartQuantity(row.id, { quantity: val })
  } catch (e) {
    loadCart()
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    await deleteCartItem(id)
    ElMessage.success('已删除')
    loadCart()
  } catch (e) { /* ignore */ }
}

async function handleClearCart() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', { type: 'warning' })
    await clearCartApi()
    ElMessage.success('购物车已清空')
    cartList.value = []
  } catch (e) { /* ignore */ }
}

async function handleCreateOrder() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要购买的商品')
    return
  }
  try {
    await ElMessageBox.confirm('确认提交订单吗？', '确认下单', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch (e) {
    return
  }
  ordering.value = true
  try {
    // 构造下单明细：携带用户确认的单价，供后端比对防价格漂移
    const items = selectedItems.value.map(item => ({
      cartItemId: item.id,
      price: item.productPrice,
      quantity: item.quantity
    }))
    const res = await createOrder(items)
    ElMessage.success('下单成功！')
    // 下单成功后重新加载购物车（未选中的商品仍保留）
    await loadCart()
    selectedItems.value = []
    router.push(`/order/${res.data.id}`)
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    ordering.value = false
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadCart()
  }
})
</script>

<style scoped>
.cart-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.cart-page h2 {
  margin-bottom: 20px;
}

.cart-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-product-name {
  font-size: 14px;
  color: #333;
}

.cart-product-price {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.total-text {
  margin-right: 20px;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}
</style>

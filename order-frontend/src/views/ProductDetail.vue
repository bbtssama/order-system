<template>
  <div class="product-detail-page" v-loading="loading">
    <div v-if="product" class="detail-container">
      <div class="detail-left">
        <el-image
          :src="product.imageUrl || 'https://placehold.co/400x400/e0e0e0/999?text=商品'"
          fit="cover"
          style="width:100%;height:400px;border-radius:8px;"
        >
          <template #error>
            <div class="image-fallback">
              <el-icon :size="64"><Picture /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <div class="detail-right">
        <h1 class="product-name">{{ product.name }}</h1>
        <div class="product-price">
          <span class="price-symbol">￥</span>
          <span class="price-value">{{ product.price }}</span>
        </div>
        <div class="product-stock">
          <el-tag :type="product.stock > 0 ? 'success' : 'danger'">
            {{ product.stock > 0 ? `有货 (库存${product.stock}件)` : '缺货' }}
          </el-tag>
        </div>
        <div class="product-desc" v-if="product.description">
          <h4>商品描述</h4>
          <p>{{ product.description }}</p>
        </div>
        <div class="action-bar" v-if="userStore.isLoggedIn">
          <div class="quantity-select">
            <span>数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              :disabled="product.stock <= 0"
              size="large"
            />
          </div>
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="product.stock <= 0"
              :loading="addingCart"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button size="large" @click="$router.push('/cart')">
              去购物车结算
            </el-button>
          </div>
        </div>
        <div class="action-bar" v-else>
          <el-button type="primary" size="large" @click="$router.push('/login')">
            请先登录后再购买
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProductById } from '@/api/product'
import { addToCart } from '@/api/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const loading = ref(false)
const quantity = ref(1)
const addingCart = ref(false)

async function loadProduct() {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
  } catch (e) {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function handleAddToCart() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  addingCart.value = true
  try {
    await addToCart({
      productId: product.value.id,
      quantity: quantity.value
    })
    ElMessage.success('已加入购物车')
  } catch (e) {
    /* ignore */
  } finally {
    addingCart.value = false
  }
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail-page {
  padding: 20px 0;
}

.detail-container {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 32px;
  border-radius: 8px;
}

.detail-left {
  flex: 0 0 400px;
}

.image-fallback {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  background: #f5f5f5;
  border-radius: 8px;
}

.detail-right {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 16px;
}

.product-price {
  color: #f56c6c;
  margin-bottom: 16px;
}

.price-symbol {
  font-size: 18px;
}

.price-value {
  font-size: 32px;
  font-weight: 700;
}

.product-stock {
  margin-bottom: 20px;
}

.product-desc {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 24px;
}

.product-desc h4 {
  margin-bottom: 8px;
  color: #666;
}

.product-desc p {
  color: #666;
  line-height: 1.8;
}

.quantity-select {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .detail-container {
    flex-direction: column;
  }
  .detail-left {
    flex: none;
    width: 100%;
  }
}
</style>

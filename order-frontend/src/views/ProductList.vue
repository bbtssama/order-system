<template>
  <div class="product-list-page">
    <div class="filter-bar">
      <el-radio-group v-model="currentCategory" @change="handleCategoryChange" size="large">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
      <div class="search-box">
        <el-input
          v-model="searchName"
          placeholder="搜索商品名称"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width:280px;"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
      <div v-else-if="!loading" class="product-grid">
        <el-card
          v-for="product in productList"
          :key="product.id"
          class="product-card"
          shadow="hover"
          @click="$router.push(`/product/${product.id}`)"
        >
          <div class="product-image">
            <el-image
              :src="product.imageUrl || 'https://placehold.co/200x200/e0e0e0/999?text=商品'"
              fit="cover"
              style="width:100%;height:200px;"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="48"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-price">
              <span class="price-symbol">￥</span>
              <span class="price-value">{{ product.price }}</span>
            </p>
            <p class="product-stock">库存: {{ product.stock }}</p>
          </div>
        </el-card>
      </div>
    </div>

    <Pagination
      v-if="total > 0"
      v-model:page="page"
      v-model:page-size="pageSize"
      :total="total"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getProductPage, getProductsByCategory } from '@/api/product'
import { getCategories } from '@/api/category'
import Pagination from '@/components/Pagination.vue'

const categories = ref([])
const productList = ref([])
const loading = ref(true)
const currentCategory = ref(null)
const searchName = ref('')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function loadProducts() {
  loading.value = true
  try {
    let res
    if (currentCategory.value) {
      res = await getProductsByCategory(currentCategory.value, { page: page.value, pageSize: pageSize.value })
    } else {
      res = await getProductPage({
        page: page.value,
        pageSize: pageSize.value,
        name: searchName.value || undefined,
        status: 'ON'
      })
    }
    const data = res.data
    productList.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    productList.value = []
  } finally {
    loading.value = false
  }
}

function handleCategoryChange() {
  // 若当前不在第一页，重置为第一页会触发 watch 自动加载；否则需手动加载
  if (page.value !== 1) {
    page.value = 1
  } else {
    loadProducts()
  }
}

function handleSearch() {
  currentCategory.value = null
  if (page.value !== 1) {
    page.value = 1
  } else {
    loadProducts()
  }
}

watch([page, pageSize], () => {
  loadProducts()
})

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  flex-shrink: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f5f5;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  background: #f5f5f5;
}

.product-info {
  padding: 12px 4px 0;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.product-price {
  color: #f56c6c;
  margin-bottom: 4px;
}

.price-symbol {
  font-size: 14px;
}

.price-value {
  font-size: 22px;
  font-weight: 600;
}

.product-stock {
  font-size: 12px;
  color: #999;
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

<template>
  <div class="order-list-page">
    <h2>我的订单</h2>
    <div v-loading="loading">
      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>

      <template v-else-if="!loading">
        <el-table :data="orderList" style="width:100%">
          <el-table-column prop="orderNo" label="订单号" width="220" />
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div v-if="row.orderItems && row.orderItems.length > 0" class="order-products">
                <span v-for="item in row.orderItems.slice(0, 3)" :key="item.id" class="order-product-tag">
                  {{ item.productName }} ×{{ item.quantity }}
                </span>
                <span v-if="row.orderItems.length > 3" style="color:#999;">
                  等{{ row.orderItems.length }}件商品
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="总金额" width="120" align="center">
            <template #default="{ row }">
              <span style="color:#f56c6c;font-weight:600;">￥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              {{ row.createTime?.substring(0, 16) || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="$router.push(`/order/${row.id}`)">详情</el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="danger"
                @click="handleCancel(row.id)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          v-if="total > 0"
          v-model:page="page"
          v-model:page-size="pageSize"
          :total="total"
        />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getOrderPage, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'

const orderList = ref([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrderPage({ page: page.value, pageSize: pageSize.value })
    const data = res.data
    orderList.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    orderList.value = []
  } finally {
    loading.value = false
  }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后将恢复库存。', '提示', { type: 'warning' })
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) { /* ignore */ }
}

function getStatusType(status) {
  const map = { PENDING: 'warning', PAID: 'primary', SHIPPED: 'success', COMPLETED: 'info', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

watch([page, pageSize], () => { loadOrders() })

onMounted(() => { loadOrders() })
</script>

<style scoped>
.order-list-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.order-list-page h2 {
  margin-bottom: 20px;
}

.order-products {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.order-product-tag {
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}
</style>

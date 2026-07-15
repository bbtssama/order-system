<template>
  <div class="order-detail-page" v-loading="loading">
    <div v-if="order" class="detail-container">
      <el-page-header @back="$router.push('/orders')" title="返回订单列表">
        <template #content>
          <span>订单详情 - {{ order.orderNo }}</span>
        </template>
      </el-page-header>

      <div class="order-status-bar">
        <el-steps :active="statusStep" finish-status="success" align-center>
          <el-step title="待支付" :description="statusTimes[0]" />
          <el-step title="已支付" :description="statusTimes[1]" />
          <el-step title="已发货" :description="statusTimes[2]" />
          <el-step title="已完成" :description="statusTimes[3]" />
        </el-steps>
      </div>

      <div class="order-info-grid">
        <div>
          <p><strong>订单号：</strong>{{ order.orderNo }}</p>
          <p><strong>下单时间：</strong>{{ order.createTime }}</p>
          <p><strong>更新时间：</strong>{{ order.updateTime }}</p>
        </div>
        <div>
          <p><strong>订单状态：</strong>
            <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag>
          </p>
          <p><strong>总金额：</strong><span style="color:#f56c6c;font-size:20px;font-weight:700;">￥{{ order.totalAmount }}</span></p>
        </div>
      </div>

      <h3 style="margin:20px 0 12px;">订单商品</h3>
      <el-table :data="order.orderItems" style="width:100%">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <span>{{ row.productName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productPrice" label="单价" width="120" align="center">
          <template #default="{ row }">￥{{ row.productPrice }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="totalPrice" label="小计" width="120" align="center">
          <template #default="{ row }">
            <span style="color:#f56c6c;font-weight:600;">￥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align:right;margin-top:16px;font-size:16px;">
        共 <strong>{{ totalCount }}</strong> 件商品，合计：
        <span style="color:#f56c6c;font-size:24px;font-weight:700;">￥{{ order.totalAmount }}</span>
      </div>

      <div class="order-actions" v-if="order.status === 'PENDING'">
        <el-button type="danger" size="large" @click="handleCancel">
          取消订单
        </el-button>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="订单不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)

const statusStep = computed(() => {
  if (!order.value) return 0
  const map = { PENDING: 0, PAID: 1, SHIPPED: 2, COMPLETED: 3, CANCELLED: -1 }
  return map[order.value.status] || 0
})

const statusTimes = computed(() => {
  if (!order.value) return ['', '', '', '']
  const times = ['', '', '', '']
  if (order.value.status === 'PENDING') times[0] = order.value.createTime?.substring(0, 16) || ''
  return times
})

const totalCount = computed(() => {
  if (!order.value?.orderItems) return 0
  return order.value.orderItems.reduce((sum, item) => sum + item.quantity, 0)
})

async function loadOrder() {
  loading.value = true
  try {
    const res = await getOrderById(route.params.id)
    order.value = res.data
  } catch (e) {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    loadOrder()
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

onMounted(() => { loadOrder() })
</script>

<style scoped>
.order-detail-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.order-status-bar {
  margin: 32px 0;
  padding: 20px 0;
}

.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.order-info-grid p {
  line-height: 2;
}

.order-actions {
  text-align: right;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>

<template>
  <div class="product-manage-page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchName"
        placeholder="搜索商品名称"
        clearable
        @clear="loadProducts"
        @keyup.enter="loadProducts"
        style="width:260px;"
      >
        <template #append>
          <el-button @click="loadProducts"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-select v-model="searchStatus" placeholder="商品状态" clearable @change="loadProducts" style="width:140px;">
        <el-option label="上架" value="ON" />
        <el-option label="下架" value="OFF" />
      </el-select>
    </div>

    <el-table :data="productList" v-loading="loading" border stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="100" align="center">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ON' ? 'success' : 'info'">
            {{ row.status === 'ON' ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-if="total > 0"
      v-model:page="page"
      v-model:page-size="pageSize"
      :total="total"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width:200px;" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width:200px;" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:200px;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ON">上架</el-radio>
            <el-radio value="OFF">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategories } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'

const productList = ref([])
const categories = ref([])
const loading = ref(false)
const searchName = ref('')
const searchStatus = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  name: '',
  description: '',
  price: 0,
  stock: 0,
  categoryId: null,
  imageUrl: '',
  status: 'ON'
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProductPage({
      page: page.value,
      pageSize: pageSize.value,
      name: searchName.value || undefined,
      status: searchStatus.value || undefined
    })
    const data = res.data
    productList.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    productList.value = []
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) { /* ignore */ }
}

function handleAdd() {
  isEdit.value = false
  form.value = { id: null, name: '', description: '', price: 0, stock: 0, categoryId: null, imageUrl: '', status: 'ON' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateProduct(form.value)
      ElMessage.success('更新成功')
    } else {
      await createProduct(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch (e) {
    // ignore
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    await deleteProduct(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) { /* ignore */ }
}

watch([page, pageSize], () => { loadProducts() })

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.product-manage-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>

<template>
  <div class="admin-books">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>平台 · 图书管理</span>
          <span class="hint">管理全站已发布书籍（上下架、删除）</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="书名">
          <el-input v-model="queryForm.title" placeholder="书名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in categories" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="在售" :value="1" />
            <el-option label="已下架" :value="0" />
            <el-option label="已售出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="bookId" label="ID" width="72" />
        <el-table-column prop="title" label="书名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sellerName" label="卖家" width="100" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column prop="price" label="价格" width="88">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">在售</el-tag>
            <el-tag v-else-if="row.status === 0" type="info" size="small">下架</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning" size="small">已售出</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              size="small"
              @click="toggleStatus(row, 0)"
            >
              下架
            </el-button>
            <el-button
              v-else-if="row.status === 0"
              link
              type="success"
              size="small"
              @click="toggleStatus(row, 1)"
            >
              上架
            </el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="书籍详情" width="560px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="书名">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ detail.sellerName }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ detail.price }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detail.status === 1" type="success" size="small">在售</el-tag>
          <el-tag v-else-if="detail.status === 0" type="info" size="small">下架</el-tag>
          <el-tag v-else type="warning" size="small">已售出</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookApi, BookCategoryApi } from '@/api/BookApi'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const detailVisible = ref(false)
const detail = ref(null)

const queryForm = reactive({
  title: '',
  categoryId: null,
  status: null,
  page: 1,
  size: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await BookApi.page(queryForm)
    if (res.status === 0) {
      tableData.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      total.value = res.recordsTotal ? parseInt(res.recordsTotal, 10) : res.data?.total || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.title = ''
  queryForm.categoryId = null
  queryForm.status = null
  queryForm.page = 1
  handleQuery()
}

const loadCategories = async () => {
  try {
    const res = await BookCategoryApi.list()
    if (res.status === 0) categories.value = res.data || []
  } catch {
    /* ignore */
  }
}

const openDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

const toggleStatus = async (row, status) => {
  try {
    const res = await BookApi.updateStatus(row.bookId, status)
    if (res.status === 0) {
      ElMessage.success(res.msg || '操作成功')
      handleQuery()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const remove = async row => {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》？此操作为逻辑删除。`, '确认', { type: 'warning' })
    const res = await BookApi.delete(row.bookId)
    if (res.status === 0) {
      ElMessage.success('已删除')
      handleQuery()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadCategories()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.admin-books {
  padding: 12px;
}
.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  .hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    font-weight: normal;
  }
}
.search-form {
  margin-bottom: 16px;
}
</style>

<template>
  <div class="admin-wanted">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>平台 · 求购管理</span>
          <span class="hint">管理全站已发布的求购（状态、删除）</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="书名">
          <el-input v-model="queryForm.bookTitle" placeholder="书名" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="求购中" :value="1" />
            <el-option label="已找到" :value="2" />
            <el-option label="已关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="wantedId" label="ID" width="72" />
        <el-table-column prop="bookTitle" label="求购书名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="username" label="发布人" width="100" />
        <el-table-column prop="maxPrice" label="期望价" width="90">
          <template #default="{ row }">
            <span style="color: #67c23a">&lt;= &yen;{{ row.maxPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">求购中</el-tag>
            <el-tag v-else-if="row.status === 2" type="info" size="small">已找到</el-tag>
            <el-tag v-else type="danger" size="small">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" size="small" @click="setStatus(row, 0)">关闭</el-button>
            <el-button link type="success" size="small" @click="setStatus(row, 1)">求购中</el-button>
            <el-button link type="info" size="small" @click="setStatus(row, 2)">已找到</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookWantedApi } from '@/api/OtherApi'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  bookTitle: '',
  status: null,
  page: 1,
  size: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await BookWantedApi.page(queryForm)
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
  queryForm.bookTitle = ''
  queryForm.status = null
  queryForm.page = 1
  handleQuery()
}

const setStatus = async (row, status) => {
  try {
    const res = await BookWantedApi.updateStatus(row.wantedId, status)
    if (res.status === 0) {
      ElMessage.success('状态已更新')
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
    await ElMessageBox.confirm(`确定删除该求购「${row.bookTitle}」？`, '确认', { type: 'warning' })
    const res = await BookWantedApi.delete(row.wantedId)
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
  handleQuery()
})
</script>

<style lang="scss" scoped>
.admin-wanted {
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

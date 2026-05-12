<template>
  <div class="admin-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>平台 · 用户管理</span>
          <span class="hint">仅展示普通用户（不含管理员）；可禁用账号或删除</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="userId" label="ID" width="72" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="140" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="88">
          <template #default="{ row }">
            <el-tag v-if="row.role === 1" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="88">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="二手币" width="100" />
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 1"
              link
              type="warning"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.role !== 1"
              link
              type="danger"
              size="small"
              @click="remove(row)"
            >
              删除
            </el-button>
            <span v-else class="muted">—</span>
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
import { UserApi } from '@/api/OtherApi'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  role: 0,
  status: null,
  page: 1,
  size: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await UserApi.page(queryForm)
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
  queryForm.status = null
  queryForm.page = 1
  handleQuery()
}

const toggleStatus = async row => {
  const next = row.status === 1 ? 0 : 1
  try {
    const res = await UserApi.update({
      userId: row.userId,
      status: next
    })
    if (res.status === 0) {
      ElMessage.success(next === 1 ? '已启用' : '已禁用')
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
    await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '确认', { type: 'warning' })
    const res = await UserApi.deleteUser(row.userId)
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
.admin-users {
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
.muted {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
</style>

<template>
  <div class="order-list">
    <el-card>
      <template #header>
        <span>我的订单</span>
      </template>

      <!-- 筛选表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="待支付" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="待收货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="bookTitle" label="书籍名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">待发货</el-tag>
            <el-tag v-else-if="row.status === 3" type="info">待收货</el-tag>
            <el-tag v-else-if="row.status === 4" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 5" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号" :span="2">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="detailData.status === 1" type="warning">待支付</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="primary">待发货</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="info">待收货</el-tag>
          <el-tag v-else-if="detailData.status === 4" type="success">已完成</el-tag>
          <el-tag v-else-if="detailData.status === 5" type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="书籍名称" :span="2">{{ detailData.bookTitle }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ detailData.buyerName }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ detailData.sellerName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detailData.quantity }}</el-descriptions-item>
        <el-descriptions-item label="总价">
          <span style="color: #f56c6c; font-weight: bold">¥{{ detailData.totalPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间" :span="2">{{ detailData.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间" :span="2">{{ detailData.deliveryTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" :span="2">{{ detailData.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px">
        <h4>订单进度</h4>
        <el-steps :active="getStepActive(detailData.status)" align-center>
          <el-step title="下单" :description="detailData.createTime" />
          <el-step title="支付" :description="detailData.paymentTime || '待支付'" />
          <el-step title="发货" :description="detailData.deliveryTime || '待发货'" />
          <el-step title="收货" :description="detailData.finishTime ? '已完成' : '待收货'" />
        </el-steps>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OrderApi } from '@/api/OtherApi'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref({})

const queryForm = reactive({
  status: null,
  page: 1,
  size: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await OrderApi.page(queryForm)
    if (res.status === 0) {
      // 修复：响应数据格式是 {data: Array, recordsTotal: string}
      tableData.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      total.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.status = null
  queryForm.page = 1
  handleQuery()
}

const handleViewDetail = async (row) => {
  try {
    const res = await OrderApi.detail(row.orderId)
    if (res.status === 0) {
      detailData.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('查询详情失败')
  }
}

const getStepActive = (status) => {
  // 1-待支付，2-待发货，3-待收货，4-已完成，5-已取消
  if (status === 5) return -1 // 已取消
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 3
  if (status === 4) return 4
  return 0
}

onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.order-list {
  padding: 20px;

  .query-form {
    margin-bottom: 20px;
  }
}
</style>

<template>
  <div class="my-wanted">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__accent"></div>
      <div class="page-header__text">
        <h1 class="page-header__title">我的求购</h1>
        <p class="page-header__sub">管理您发布的求购，随时更新状态</p>
      </div>
      <el-button type="primary" class="page-header__btn" @click="goPublish">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        发布新求购
      </el-button>
    </div>

    <!-- 求购卡片列表 -->
    <div class="wanted-container">
      <div 
        v-if="tableData.length > 0" 
        class="wanted-grid"
      >
        <div 
          v-for="wanted in tableData" 
          :key="wanted.wantedId" 
          class="wanted-card"
          @click="goToDetail(wanted)"
        >
          <div class="wanted-card__cover-wrap">
            <el-image
              v-if="wanted.coverImage"
              :src="wanted.coverImage"
              fit="cover"
              class="wanted-card__cover"
              :preview-src-list="[wanted.coverImage]"
              preview-teleported
            />
            <div v-else class="wanted-card__cover--empty">
              <span class="wanted-card__cover-text">暂无图片</span>
            </div>
            <div class="wanted-card__status" :class="getStatusClass(wanted.status)">
              {{ getStatusText(wanted.status) }}
            </div>
          </div>
          
          <div class="wanted-card__info">
            <h3 class="wanted-card__title" :title="wanted.bookTitle">{{ wanted.bookTitle }}</h3>
            <p class="wanted-card__author">{{ wanted.author || '作者未填' }}</p>
            <div class="wanted-card__price">期望 ≤ ¥{{ formatPrice(wanted.maxPrice) }}</div>
            <div class="wanted-card__tags">
              <span class="wanted-card__tag wanted-card__tag--views">浏览 {{ wanted.viewCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon">🔍</div>
        <h3 class="empty-state__title">暂无求购</h3>
        <p class="empty-state__text">您还没有发布任何求购信息</p>
        <el-button type="primary" @click="goPublish">立即发布</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="tableData.length > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
    </div>

    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { BookWantedApi } from '@/api/OtherApi'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 6
})

const getStatusText = (status) => {
  const map = {
    1: '求购中',
    2: '已找到',
    0: '已关闭'
  }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = {
    1: 'is-open',
    2: 'is-done',
    0: 'is-off'
  }
  return map[status] || ''
}

const formatPrice = (val) => {
  if (val === undefined || val === null || val === '') return '0'
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return String(parseFloat(n.toFixed(2)))
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await BookWantedApi.myWanted(queryForm)
    if (res.status === 0) {
      tableData.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      total.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (wanted) => {
  router.push(`/dashboard/wanted/detail/${wanted.wantedId}`)
}

const handleUpdateStatus = async (row, status) => {
  const text = status === 2 ? '标记为已找到' : '关闭'
  try {
    await ElMessageBox.confirm(`确定要${text}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await BookWantedApi.updateStatus(row.wantedId, status)
    if (res.status === 0) {
      ElMessage.success('操作成功')
      handleQuery()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await BookWantedApi.delete(row.wantedId)
    if (res.status === 0) {
      ElMessage.success('删除成功')
      handleQuery()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const goPublish = () => {
  router.push('/dashboard/wanted/publish')
}

onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
$paper: #f6f1e9;
$paper-light: #faf6ef;
$wine: #7b1f2a;
$wine-light: #9a2f3c;
$green: #059669;
$border-light: rgba(44, 24, 16, 0.08);
$text-primary: #2c1810;
$text-secondary: #6b5344;
$text-muted: #a8a29e;

.my-wanted {
  min-height: calc(100vh - 72px);
  padding: 24px 32px;
  background: linear-gradient(180deg, $paper-light 0%, $paper 42%, #efe8dc 100%);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(123, 31, 42, 0.12);
  
  &__accent {
    width: 4px;
    height: 40px;
    border-radius: 3px;
    background: linear-gradient(180deg, $wine-light, $wine);
    flex-shrink: 0;
  }
  
  &__text {
    flex: 1;
    min-width: 0;
  }
  
  &__title {
    margin: 0 0 4px;
    font-size: 24px;
    font-weight: 800;
    color: $text-primary;
    letter-spacing: 0.06em;
    font-family: 'Source Han Serif SC', 'Songti SC', 'SimSun', serif;
  }
  
  &__sub {
    margin: 0;
    font-size: 13px;
    color: $text-secondary;
    letter-spacing: 0.08em;
  }
  
  &__btn {
    display: flex;
    align-items: center;
    gap: 6px;
    background: $wine;
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-size: 14px;
    font-weight: 600;
    
    &:hover {
      background: $wine-light;
    }
  }
}

.wanted-container {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  background: #fffefb;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(44, 24, 16, 0.06);
}

.wanted-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.wanted-card {
  background: #fffefb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.05);
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(44, 24, 16, 0.1);
  }
  
  &__cover-wrap {
    position: relative;
    width: 100%;
    height: 160px;
    overflow: hidden;
    flex-shrink: 0;
  }
  
  &__cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &__cover--empty {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(145deg, #ecfdf5, #d1fae5);
  }
  
  &__cover-text {
    font-size: 12px;
    color: #34d399;
  }
  
  &__status {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 600;
    
    &.is-open {
      background: $green;
      color: #fff;
    }
    
    &.is-done {
      background: #b45309;
      color: #fff;
    }
    
    &.is-off {
      background: #78716c;
      color: #fff;
    }
  }
  
  &__info {
    padding: 12px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  &__title {
    margin: 0 0 4px;
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: 'Source Han Serif SC', 'Songti SC', 'SimSun', serif;
  }
  
  &__author {
    margin: 0 0 6px;
    font-size: 12px;
    color: $text-secondary;
  }
  
  &__price {
    margin-bottom: 8px;
    font-size: 15px;
    font-weight: 700;
    color: $green;
  }
  
  &__tags {
    display: flex;
    gap: 8px;
    margin-top: auto;
  }
  
  &__tag {
    padding: 2px 8px;
    border-radius: 8px;
    font-size: 11px;
    
    &--views {
      background: rgba(168, 162, 158, 0.15);
      color: $text-secondary;
    }
  }
  
  &__actions {
    padding: 10px 12px;
    border-top: 1px solid $border-light;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    
    .el-button--text {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: #fffefb;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.05);
  
  &__icon {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.6;
  }
  
  &__title {
    margin: 0 0 6px;
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }
  
  &__text {
    margin: 0 0 16px;
    font-size: 13px;
    color: $text-secondary;
  }
}

.pagination-wrap {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid $border-light;
  display: flex;
  justify-content: center;
}

.pagination {
  :deep(.el-pagination) {
    padding: 8px 16px;
    background: #fffefb;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(44, 24, 16, 0.05);
  }
  
  :deep(.el-pager li.is-active) {
    background: $wine;
    color: #fff;
  }
}
</style>
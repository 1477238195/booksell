<template>
  <div class="my-books">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header__accent"></div>
      <div class="page-header__text">
        <h1 class="page-header__title">我的书籍</h1>
        <p class="page-header__sub">管理您发布的书籍，随时更新状态</p>
      </div>
      <el-button type="primary" class="page-header__btn" @click="goPublish">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        发布新书籍
      </el-button>
    </div>

    <!-- 书籍卡片列表 -->
    <div class="books-container">
      <div 
        v-if="tableData.length > 0" 
        class="books-grid"
      >
        <div 
          v-for="book in tableData" 
          :key="book.bookId" 
          class="book-card"
          @click="goToDetail(book)"
        >
          <div class="book-card__cover-wrap">
            <el-image
              v-if="book.coverImage"
              :src="book.coverImage"
              fit="cover"
              class="book-card__cover"
              :preview-src-list="[book.coverImage]"
              preview-teleported
            />
            <div v-else class="book-card__cover--empty">
              <span class="book-card__cover-text">暂无封面</span>
            </div>
            <div class="book-card__status">
              {{ getStatusText(book.status) }}
            </div>
          </div>
          
          <div class="book-card__info">
            <h3 class="book-card__title" :title="book.title">{{ book.title }}</h3>
            <div class="book-card__price">¥{{ book.price }}</div>
            <div class="book-card__tags">
              <span class="book-card__tag book-card__tag--condition">{{ getConditionText(book.condition) }}</span>
              <span class="book-card__tag book-card__tag--views">浏览 {{ book.viewCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon">📖</div>
        <h3 class="empty-state__title">暂无书籍</h3>
        <p class="empty-state__text">您还没有发布任何书籍</p>
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
import { useRouter } from 'vue-router'
import { BookApi } from '@/api/BookApi'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  sellerId: null,
  page: 1,
  size: 6
})

const getStatusText = (status) => {
  const map = {
    1: '在售',
    0: '已下架',
    2: '已售出'
  }
  return map[status] || '未知'
}

const getConditionText = (condition) => {
  const map = {
    1: '全新',
    2: '几乎全新',
    3: '轻微使用痕迹',
    4: '明显使用痕迹'
  }
  return map[condition] || '未知'
}

const handleQuery = async () => {
  loading.value = true
  try {
    queryForm.sellerId = userStore.userInfo?.userId
    const res = await BookApi.page(queryForm)
    if (res.status === 0) {
      tableData.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      total.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
    }
  } catch (error) {
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (book) => {
  router.push(`/dashboard/book/detail/${book.bookId}`)
}

const goPublish = () => {
  router.push('/dashboard/book/publish')
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
$orange: #ff6a00;
$border-light: rgba(44, 24, 16, 0.08);
$text-primary: #2c1810;
$text-secondary: #6b5344;
$text-muted: #a8a29e;

.my-books {
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

.books-container {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  background: #fffefb;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(44, 24, 16, 0.06);
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.book-card {
  background: #fffefb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.05);
  cursor: pointer;
  transition: all 0.25s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(44, 24, 16, 0.1);
  }
  
  &__cover-wrap {
    position: relative;
    width: 100%;
    height: 160px;
    overflow: hidden;
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
    background: $paper;
  }
  
  &__cover-text {
    font-size: 12px;
    color: $text-muted;
  }
  
  &__status {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 600;
    background: $wine;
    color: #fff;
  }
  
  &__info {
    padding: 12px;
  }
  
  &__title {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: 'Source Han Serif SC', 'Songti SC', 'SimSun', serif;
  }
  
  &__price {
    margin-bottom: 8px;
    font-size: 16px;
    font-weight: 700;
    color: $orange;
  }
  
  &__tags {
    display: flex;
    gap: 8px;
  }
  
  &__tag {
    padding: 2px 8px;
    border-radius: 8px;
    font-size: 11px;
    
    &--condition {
      background: rgba(123, 31, 42, 0.06);
      color: $wine;
    }
    
    &--views {
      background: rgba(168, 162, 158, 0.15);
      color: $text-secondary;
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

<template>
  <div class="hub-page">
    <div class="hub-bg" aria-hidden="true" />
    <div class="hub-inner">
      <header class="hub-hero">
        <p class="hub-kicker">二手书交易平台</p>
        <h1 class="hub-title">欢迎回来，{{ displayName }}</h1>
        <p class="hub-sub">在书市里发现下一本好书 · 让知识流动起来</p>
      </header>

      <section class="hub-panel">
        <HubUserStrip />
        <HubTileGrid :tiles="tiles" />
      </section>

      <!-- 左右分栏：左边书籍，右边求购 -->
      <section class="hub-split">
        <!-- 左侧：书籍列表 -->
        <div class="split-panel split-panel--books">
          <div class="split-panel__header">
            <div class="split-panel__title-row">
              <span class="split-panel__icon">
                <el-icon><Reading /></el-icon>
              </span>
              <h2 class="split-panel__title">在售书籍</h2>
              <span class="split-panel__badge">{{ booksTotal }} 本</span>
            </div>
            <el-button text type="primary" class="split-panel__more" @click="goBooks">
              查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>

          <div v-loading="booksLoading" class="split-panel__content">
            <div v-if="books.length > 0" class="mini-grid">
              <article
                v-for="book in books"
                :key="book.bookId"
                class="mini-card"
                @click="goBookDetail(book)"
              >
                <div class="mini-card__media">
                  <el-image
                    v-if="book.coverImage"
                    :src="book.coverImage"
                    fit="cover"
                    class="mini-card__img"
                    lazy
                  >
                    <template #error>
                      <div class="mini-card__placeholder">
                        <el-icon :size="24"><Reading /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="mini-card__placeholder">
                    <el-icon :size="24"><Reading /></el-icon>
                  </div>
                  <span
                    class="mini-card__status"
                    :class="{
                      'mini-card__status--sale': book.status === 1,
                      'mini-card__status--sold': book.status === 2
                    }"
                  >
                    {{ book.status === 1 ? '在售' : book.status === 2 ? '已售' : '下架' }}
                  </span>
                </div>
                <div class="mini-card__body">
                  <h3 class="mini-card__name" :title="book.title">{{ book.title }}</h3>
                  <div class="mini-card__price">
                    <span class="mini-card__yen">¥</span>
                    <span class="mini-card__num">{{ formatPrice(book.price) }}</span>
                  </div>
                </div>
              </article>
            </div>
            <el-empty v-else-if="!booksLoading" description="暂无在售书籍" :image-size="60" />
          </div>

          <div v-if="booksTotal > pageSize" class="split-panel__footer">
            <el-pagination
              small
              :current-page="booksPage"
              :page-size="pageSize"
              :total="booksTotal"
              layout="prev, pager, next"
              @current-change="loadBooks"
            />
          </div>
        </div>

        <!-- 右侧：求购列表 -->
        <div class="split-panel split-panel--wanted">
          <div class="split-panel__header">
            <div class="split-panel__title-row">
              <span class="split-panel__icon split-panel__icon--wanted">
                <el-icon><Search /></el-icon>
              </span>
              <h2 class="split-panel__title">求购信息</h2>
              <span class="split-panel__badge">{{ wantedTotal }} 条</span>
            </div>
            <el-button text type="primary" class="split-panel__more" @click="goWanted">
              查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>

          <div v-loading="wantedLoading" class="split-panel__content">
            <div v-if="wanted.length > 0" class="mini-grid">
              <article
                v-for="item in wanted"
                :key="item.wantedId"
                class="mini-card mini-card--wanted"
                @click="goWantedDetail(item)"
              >
                <div class="mini-card__media">
                  <el-image
                    v-if="item.coverImage"
                    :src="item.coverImage"
                    fit="cover"
                    class="mini-card__img"
                    lazy
                  >
                    <template #error>
                      <div class="mini-card__placeholder">
                        <el-icon :size="24"><Search /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="mini-card__placeholder">
                    <el-icon :size="24"><Search /></el-icon>
                  </div>
                  <span
                    class="mini-card__status mini-card__status--wanted"
                    :class="{ 'mini-card__status--found': item.status === 2 }"
                  >
                    {{ item.status === 1 ? '求购中' : item.status === 2 ? '已找到' : '已关闭' }}
                  </span>
                </div>
                <div class="mini-card__body">
                  <h3 class="mini-card__name" :title="item.bookTitle">{{ item.bookTitle }}</h3>
                  <div class="mini-card__price">
                    <span class="mini-card__label">期望</span>
                    <span class="mini-card__yen">¥</span>
                    <span class="mini-card__num">{{ formatPrice(item.maxPrice) }}</span>
                  </div>
                </div>
              </article>
            </div>
            <el-empty v-else-if="!wantedLoading" description="暂无求购信息" :image-size="60" />
          </div>

          <div v-if="wantedTotal > pageSize" class="split-panel__footer">
            <el-pagination
              small
              :current-page="wantedPage"
              :page-size="pageSize"
              :total="wantedTotal"
              layout="prev, pager, next"
              @current-change="loadWanted"
            />
          </div>
        </div>
      </section>

      <footer class="hub-panel-foot">点击下方卡片进入各模块；子功能在模块内以相同方式选择</footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, ShoppingCart, Search, ChatDotRound, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { BookApi } from '@/api/BookApi'
import { BookWantedApi } from '@/api/OtherApi'
import HubUserStrip from './HubUserStrip.vue'
import HubTileGrid from './HubTileGrid.vue'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userInfo.value?.username || '书友')

const tiles = [
  {
    path: '/dashboard/book/index',
    label: '书籍管理',
    hint: '书市、发布、分类等',
    icon: Reading,
    tint: 'linear-gradient(135deg, #9a2f3c 0%, #7b1f2a 100%)'
  },
  {
    path: '/dashboard/order',
    label: '订单管理',
    hint: '订单与买卖记录',
    icon: ShoppingCart,
    tint: 'linear-gradient(135deg, #10b981 0%, #14b8a6 100%)'
  },
  {
    path: '/dashboard/wanted/index',
    label: '求购信息',
    hint: '求购列表与发布',
    icon: Search,
    tint: 'linear-gradient(135deg, #f59e0b 0%, #eab308 100%)'
  },
  {
    path: '/dashboard/message/list',
    label: '消息列表',
    hint: '站内消息与对话',
    icon: ChatDotRound,
    tint: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)'
  }
]

// 书籍列表
const books = ref([])
const booksTotal = ref(0)
const booksPage = ref(1)
const booksLoading = ref(false)

// 求购列表
const wanted = ref([])
const wantedTotal = ref(0)
const wantedPage = ref(1)
const wantedLoading = ref(false)

// 统一的分页大小，确保左右两边卡片数量一致
const pageSize = 8

function formatPrice(val) {
  if (val === undefined || val === null || val === '') return '0'
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return String(parseFloat(n.toFixed(2)))
}

async function loadBooks(page = 1) {
  booksLoading.value = true
  try {
    const res = await BookApi.page({ page, size: 8, status: 1 })
    if (res.status === 0) {
      books.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      booksTotal.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
      booksPage.value = page
    }
  } catch {
    /* 错误由拦截器处理 */
  } finally {
    booksLoading.value = false
  }
}

async function loadWanted(page = 1) {
  wantedLoading.value = true
  try {
    const res = await BookWantedApi.page({ page, size: 8, status: 1 })
    if (res.status === 0) {
      wanted.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      wantedTotal.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
      wantedPage.value = page
    }
  } catch {
    /* 错误由拦截器处理 */
  } finally {
    wantedLoading.value = false
  }
}

function goBooks() {
  router.push('/dashboard/book/list')
}

function goBookDetail(book) {
  router.push({ name: 'BookDetail', params: { bookId: String(book.bookId) } })
}

function goWanted() {
  router.push('/dashboard/wanted/list')
}

function goWantedDetail(item) {
  router.push({ name: 'WantedDetail', params: { wantedId: String(item.wantedId) } })
}

onMounted(() => {
  loadBooks()
  loadWanted()
  void import('@/views/hub/BookHub.vue')
  void import('@/views/book/list.vue')
  void import('@/views/hub/PlatformHub.vue')
})
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';

// 左右分栏布局 - 确保左右两边高度一致
.hub-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 20px;
  align-items: start;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.split-panel {
  background: #fffefb;
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 12px 40px rgba(44, 24, 16, 0.08);
  // 确保左右两边最小高度一致
  min-height: 480px;
  display: flex;
  flex-direction: column;

  &--books {
    border-top: 3px solid #9a2f3c;
  }

  &--wanted {
    border-top: 3px solid #f59e0b;
  }
}

.split-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(44, 24, 16, 0.08);
  flex-shrink: 0;
}

.split-panel__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.split-panel__icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-size: 18px;

  &--wanted {
    background: linear-gradient(135deg, #f59e0b, #eab308);
  }
}

.split-panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1c1917;
}

.split-panel__badge {
  font-size: 12px;
  color: #9a2f3c;
  background: rgba(154, 47, 60, 0.08);
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;

  .split-panel--wanted & {
    color: #b45309;
    background: rgba(245, 158, 11, 0.1);
  }
}

.split-panel__more {
  font-size: 13px;
  font-weight: 500;

  .el-icon {
    transition: transform 0.2s;
  }

  &:hover .el-icon {
    transform: translateX(3px);
  }
}

.split-panel__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 280px;
}

.split-panel__footer {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(44, 24, 16, 0.06);

  :deep(.el-pagination) {
    font-weight: 500;

    .btn-prev,
    .btn-next {
      background: #faf8f5;
      border-radius: 6px;
      border: 1px solid rgba(44, 24, 16, 0.1);

      &:hover:not(:disabled) {
        background: #fff;
        border-color: rgba(255, 106, 0, 0.5);
        color: #9a2f3c;
      }
    }

    .el-pager li {
      border-radius: 6px;
      margin: 0 2px;
      background: #faf8f5;
      border: 1px solid rgba(44, 24, 16, 0.08);

      &:hover:not(.is-active):not(:disabled) {
        color: #9a2f3c;
        border-color: rgba(255, 106, 0, 0.5);
      }

      &.is-active {
        background: linear-gradient(135deg, #9a2f3c, #7b1f2a) !important;
        border-color: #9a2f3c !important;
        color: #fff !important;
      }
    }
  }
}

// 迷你卡片网格 - 4列布局确保内容均衡
.mini-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.mini-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.1);
  }
}

.mini-card__media {
  position: relative;
  aspect-ratio: 1;
  background: linear-gradient(145deg, #f3ece4, #e8dfd4);
  overflow: hidden;
}

.mini-card__img {
  width: 100%;
  height: 100%;
  display: block;
}

.mini-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a8a29e;
  background: linear-gradient(145deg, #f3ece4, #e8dfd4);
}

.mini-card__status {
  position: absolute;
  top: 6px;
  right: 6px;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 600;
  border-radius: 4px;
  background: rgba(34, 197, 94, 0.9);
  color: #fff;

  &--sold {
    background: rgba(245, 158, 11, 0.9);
  }

  &--found {
    background: rgba(245, 158, 11, 0.9);
  }

  &--wanted {
    background: rgba(34, 197, 94, 0.9);
  }
}

.mini-card__body {
  padding: 10px;
}

.mini-card__name {
  margin: 0 0 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1c1917;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 34px;
}

.mini-card__price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.mini-card__label {
  font-size: 11px;
  color: #a8a29e;
}

.mini-card__yen {
  font-size: 12px;
  font-weight: 700;
  color: #d4380d;
}

.mini-card__num {
  font-size: 15px;
  font-weight: 700;
  color: #d4380d;
}

.mini-card--wanted {
  .mini-card__status--active {
    background: rgba(34, 197, 94, 0.9);
  }
}
</style>

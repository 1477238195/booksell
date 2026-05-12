<template>
  <div class="book-list">
    <header class="book-list__hero">
      <div class="book-list__hero-inner">
        <div class="book-list__hero-copy">
          <p class="book-list__eyebrow">二手书 · 好书接力</p>
          <h1 class="book-list__title">书籍列表</h1>
          <p class="book-list__subtitle">筛选心仪读物，一键查看详情或联系卖家</p>
        </div>
        <el-button type="primary" round size="large" class="book-list__publish" @click="goPublish">
          <el-icon class="el-icon--left"><Plus /></el-icon>
          发布书籍
        </el-button>
      </div>
    </header>

    <section class="book-list__panel">
      <div class="book-search">
        <p class="book-search__tip">书名支持<strong>模糊匹配</strong>；选好分类、状态后点击搜索</p>
        <div class="book-search__bar">
          <div class="book-search__seg book-search__seg--category">
            <el-select
              v-model="queryForm.categoryId"
              placeholder="全部分类"
              clearable
              filterable
              class="book-search__select"
              @keyup.enter="submitSearch"
            >
              <el-option
                v-for="item in categories"
                :key="item.categoryId"
                :label="item.name"
                :value="item.categoryId"
              />
            </el-select>
          </div>
          <div class="book-search__seg book-search__seg--status">
            <el-select
              v-model="queryForm.status"
              placeholder="全部状态"
              clearable
              class="book-search__select"
              @keyup.enter="submitSearch"
            >
              <el-option label="在售" :value="1" />
              <el-option label="已下架" :value="0" />
              <el-option label="已售出" :value="2" />
            </el-select>
          </div>
          <div class="book-search__seg book-search__seg--input">
            <el-input
              v-model="queryForm.title"
              placeholder="输入书名关键词，模糊搜索"
              clearable
              class="book-search__input"
              @keyup.enter="submitSearch"
            >
              <template #prefix>
                <el-icon class="book-search__input-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button type="primary" class="book-search__submit" @click="submitSearch">搜索</el-button>
        </div>
        <div class="book-search__actions">
          <el-button text type="primary" @click="handleReset">重置条件</el-button>
        </div>
      </div>

      <div v-loading="loading" class="book-list__grid-wrap">
        <div v-if="tableData.length > 0" class="book-grid">
          <article v-for="row in tableData" :key="row.bookId" class="book-card">
            <div class="book-card__media" @click="handleView(row)">
              <div class="book-card__media-inner">
                <el-image
                  v-if="row.coverImage"
                  :src="row.coverImage"
                  fit="cover"
                  class="book-card__img"
                  lazy
                >
                  <template #error>
                    <div class="book-card__placeholder">
                      <el-icon :size="36"><Picture /></el-icon>
                      <span>封面加载失败</span>
                    </div>
                  </template>
                </el-image>
                <div v-else class="book-card__placeholder">
                  <el-icon :size="40"><Reading /></el-icon>
                  <span>暂无封面</span>
                </div>
              </div>
              <span
                class="book-card__status"
                :class="{
                  'book-card__status--sale': row.status === 1,
                  'book-card__status--sold': row.status === 2,
                  'book-card__status--off': row.status !== 1 && row.status !== 2
                }"
              >
                {{ statusText(row.status) }}
              </span>
            </div>

            <div class="book-card__body">
              <div class="book-card__title-row">
                <span class="book-card__badge">优选</span>
                <h3 class="book-card__name" :title="row.title">{{ row.title }}</h3>
              </div>

              <div class="book-card__price-row">
                <span class="book-card__yen">¥</span>
                <span class="book-card__price">{{ formatPrice(row.price) }}</span>
              </div>

              <div class="book-card__meta">
                <span class="book-card__seller">
                  <el-icon><User /></el-icon>
                  {{ row.sellerName || '匿名卖家' }}
                </span>
                <span class="book-card__views">
                  <el-icon><View /></el-icon>
                  {{ row.viewCount ?? 0 }}
                </span>
              </div>

              <p class="book-card__chips">{{ chipSummary(row) }}</p>

              <div class="book-card__actions">
                <button type="button" class="book-card__btn" @click.stop="handleView(row)">详情</button>
                <button
                  type="button"
                  class="book-card__btn book-card__btn--primary"
                  :disabled="row.status !== 1 || isOwnListing(row)"
                  @click.stop="handleBuy(row)"
                >
                  购买
                </button>
                <button
                  type="button"
                  class="book-card__btn"
                  :disabled="!row.sellerId || isOwnListing(row)"
                  @click.stop="handleContact(row)"
                >
                  私信
                </button>
              </div>
              <p v-if="isOwnListing(row)" class="book-card__own-hint">当前为您的上架商品</p>
            </div>
          </article>
        </div>
        <el-empty v-else-if="!loading" description="没有找到符合条件的书籍" class="book-list__empty" />
      </div>

      <div class="book-list__pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, Reading, Search, User, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { BookApi, BookCategoryApi } from '@/api/BookApi'
import { OrderApi } from '@/api/OtherApi'

const router = useRouter()
const userStore = useUserStore()

const myUserId = computed(() => {
  const id = userStore.userInfo?.userId
  if (id === undefined || id === null || id === '') return null
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})

const isOwnListing = (row) => {
  if (!row || myUserId.value == null || row.sellerId === undefined || row.sellerId === null || row.sellerId === '')
    return false
  return Number(row.sellerId) === myUserId.value
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const queryForm = reactive({
  title: '',
  categoryId: null,
  /** null 表示不限状态；默认在售更符合书市浏览习惯 */
  status: 1,
  page: 1,
  size: 12
})

function submitSearch() {
  if (typeof queryForm.title === 'string') {
    queryForm.title = queryForm.title.trim()
  }
  queryForm.page = 1
  handleQuery()
}

function formatPrice(val) {
  if (val === undefined || val === null || val === '') return '0'
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return String(parseFloat(n.toFixed(2)))
}

function statusText(status) {
  if (status === 1) return '在售'
  if (status === 2) return '已售出'
  if (status === 0) return '已下架'
  return '未知'
}

function getConditionText(condition) {
  const map = {
    1: '全新',
    2: '几乎全新',
    3: '轻微使用痕迹',
    4: '明显使用痕迹'
  }
  return map[condition] || '未知'
}

function chipSummary(row) {
  const parts = [row.categoryName || '未分类', getConditionText(row.condition)]
  if (row.stock != null && row.stock !== '') {
    parts.push(`库存 ${row.stock}`)
  }
  return parts.join(' · ')
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await BookApi.page(queryForm)
    if (res.status === 0) {
      tableData.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      total.value = res.recordsTotal ? parseInt(res.recordsTotal, 10) : res.data?.total || 0
    }
  } catch {
    // 错误提示由 @/utils/request 拦截器统一处理
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.title = ''
  queryForm.categoryId = null
  queryForm.status = 1
  queryForm.page = 1
  handleQuery()
}

const loadCategories = async () => {
  try {
    const res = await BookCategoryApi.list()
    if (res.status === 0) {
      categories.value = res.data || []
    }
  } catch {
    /* 同上 */
  }
}

const handleView = (row) => {
  if (!row?.bookId) return
  router.push({ name: 'BookDetail', params: { bookId: String(row.bookId) } })
}

const handleBuy = async (row) => {
  if (isOwnListing(row)) {
    ElMessage.warning('不能购买自己发布的书籍')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要购买《${row.title}》吗？\n价格：¥${formatPrice(row.price)}\n数量：1本`,
      '确认购买',
      {
        confirmButtonText: '确定购买',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    try {
      const res = await OrderApi.createOrder(row.bookId, 1)
      if (res.status === 0) {
        ElMessage.success('下单成功，请前往"我的买家订单"完成支付')
        window.dispatchEvent(new CustomEvent('balance-updated'))
      }
    } catch {
      // request 拦截器已提示
    } finally {
      loading.value = false
    }
  } catch {
    /* 用户取消 */
  }
}

const goPublish = () => {
  router.push('/dashboard/book/publish')
}

const handleContact = (row) => {
  if (isOwnListing(row)) {
    ElMessage.warning('无需联系自己')
    return
  }
  if (!row.sellerId) {
    ElMessage.warning('卖家信息不存在')
    return
  }
  router.push({
    path: '/dashboard/message/chat',
    query: {
      userId: row.sellerId,
      userName: row.sellerName || '卖家'
    }
  })
}

onMounted(() => {
  loadCategories()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.book-list {
  min-height: 100%;
  padding: 20px 20px 32px;
  box-sizing: border-box;
  background: var(--market-gradient-page);
}

.book-list__hero {
  margin-bottom: 20px;
}

.book-list__hero-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 26px;
  border-radius: 20px;
  background: linear-gradient(125deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.72) 100%);
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow:
    0 4px 6px -1px rgba(44, 24, 16, 0.06),
    0 20px 40px -16px rgba(123, 31, 42, 0.12);
  backdrop-filter: blur(10px);
}

.book-list__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9a2f3c;
}

.book-list__title {
  margin: 0 0 8px;
  font-size: clamp(1.5rem, 3vw, 1.85rem);
  font-weight: 800;
  color: #1c1917;
  letter-spacing: -0.03em;
}

.book-list__subtitle {
  margin: 0;
  font-size: 14px;
  color: #78716c;
  max-width: 420px;
  line-height: 1.5;
}

.book-list__publish {
  font-weight: 700 !important;
  padding: 12px 22px !important;
  border: none !important;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a) !important;
  box-shadow: 0 10px 24px rgba(123, 31, 42, 0.28);

  &:hover {
    filter: brightness(1.06);
  }
}

.book-list__panel {
  background: #fffefb;
  border-radius: 20px;
  padding: 20px 22px 24px;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 12px 40px rgba(44, 24, 16, 0.08);
}

.book-search {
  margin-bottom: 8px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(44, 24, 16, 0.08);
}

.book-search__tip {
  margin: 0 0 10px;
  font-size: 13px;
  color: #78716c;

  strong {
    color: #7b1f2a;
    font-weight: 600;
  }
}

.book-search__bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border: 1.5px solid rgba(44, 24, 16, 0.12);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.06);
}

.book-search__seg {
  display: flex;
  align-items: center;
  background: #faf8f5;
  border: 1px solid rgba(44, 24, 16, 0.1);
  border-radius: 10px;
  padding: 0 12px;
  height: 40px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: rgba(255, 106, 0, 0.4);
    box-shadow: 0 0 0 3px rgba(255, 106, 0, 0.08);
  }

  &--category {
    flex: 0 0 148px;
    min-width: 0;
  }

  &--status {
    flex: 0 0 112px;
    min-width: 0;
  }

  &--input {
    flex: 1;
    min-width: 180px;
    background: #fff;
    border-color: rgba(44, 24, 16, 0.1);
    padding: 0;
    padding-left: 12px;

    &:hover {
      border-color: rgba(255, 106, 0, 0.4);
    }
  }
}

.book-search__select {
  width: 100%;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;

  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    background: transparent !important;
    padding: 0 !important;
  }

  :deep(.el-input__inner) {
    color: #44403c !important;
    font-size: 14px !important;
  }
}

.book-search__input {
  width: 100%;
}

.book-search__submit {
  flex-shrink: 0;
  height: 40px;
  margin: 0 !important;
  font-weight: 700 !important;
  font-size: 14px !important;
  padding: 0 22px !important;
  border: none !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #ff7a1a, #e85d00) !important;
  box-shadow: 0 4px 12px rgba(232, 93, 0, 0.3);

  &:hover {
    filter: brightness(1.06);
    box-shadow: 0 6px 16px rgba(232, 93, 0, 0.4);
  }
}

.book-search__input-icon {
  color: #a8a29e;
  font-size: 16px;
}

.book-search__actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.book-list__grid-wrap {
  min-height: 220px;
  padding-top: 8px;
}

.book-grid {
  display: grid;
  gap: 16px;
  /* 使用固定列数确保对称布局，3列或4列取决于可用宽度 */
  grid-template-columns: repeat(3, 1fr);

  /* 小屏幕保持3列 */
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}

.book-card {
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow:
      0 16px 36px rgba(15, 23, 42, 0.1),
      0 0 0 1px rgba(255, 106, 0, 0.25);
    border-color: rgba(255, 106, 0, 0.45);
  }
}

.book-card__media {
  position: relative;
  cursor: pointer;
  /* 调整封面比例为4:3，更好的视觉平衡 */
  aspect-ratio: 4 / 3;
  background: linear-gradient(145deg, #f3ece4, #e8dfd4);
  /* 移除不必要的空白 */
  overflow: hidden;
}

.book-card__media-inner {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.book-card__img {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.35s ease;

  .book-card:hover & {
    transform: scale(1.04);
  }
}

.book-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #a8a29e;
  font-size: 13px;
  font-weight: 500;
  background:
    radial-gradient(ellipse 80% 60% at 50% 40%, rgba(154, 47, 60, 0.12), transparent),
    linear-gradient(160deg, #faf8f5 0%, #f3ece4 100%);
}

.book-card__status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 999px;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.book-card__status--sale {
  background: rgba(34, 197, 94, 0.92);
  color: #fff;
}

.book-card__status--sold {
  background: rgba(245, 158, 11, 0.94);
  color: #fff;
}

.book-card__status--off {
  background: rgba(100, 116, 139, 0.9);
  color: #fff;
}

.book-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 14px 14px 16px;
  gap: 8px;
}

.book-card__title-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 44px;
}

.book-card__badge {
  flex-shrink: 0;
  margin-top: 2px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 6px;
  color: #7b1f2a;
  background: rgba(123, 31, 42, 0.08);
  border: 1px solid rgba(123, 31, 42, 0.2);
}

.book-card__name {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1c1917;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.book-card__yen {
  font-size: 14px;
  font-weight: 800;
  color: #d4380d;
}

.book-card__price {
  font-size: 22px;
  font-weight: 800;
  color: #d4380d;
  letter-spacing: -0.03em;
  line-height: 1;
}

.book-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #78716c;
}

.book-card__seller,
.book-card__views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;

  .el-icon {
    font-size: 14px;
    color: #a8a29e;
  }
}

.book-card__seller {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-card__chips {
  margin: 0;
  font-size: 12px;
  color: #a8a29e;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-card__actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: auto;
  padding-top: 12px;
}

.book-card__btn {
  border: 1px solid rgba(44, 24, 16, 0.1);
  background: #faf8f5;
  color: #44403c;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 4px;
  border-radius: 10px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;

  &:hover:not(:disabled) {
    background: #fffefb;
    border-color: rgba(255, 106, 0, 0.45);
    color: #7b1f2a;
  }

  &:disabled {
    opacity: 0.38;
    cursor: not-allowed;
  }
}

.book-card__btn--primary {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border-color: rgba(251, 146, 60, 0.55);
  color: #c2410c;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #ffedd5, #fed7aa);
    border-color: #fb923c;
    color: #9a3412;
  }
}

.book-card__own-hint {
  margin: 0;
  font-size: 11px;
  color: #9a2f3c;
  text-align: center;
}

.book-list__empty {
  padding: 48px 0;
}

.book-list__pagination {
  display: flex;
  justify-content: center;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid rgba(44, 24, 16, 0.08);

  :deep(.el-pagination) {
    font-weight: 500;

    .btn-prev,
    .btn-next {
      border-radius: 8px;
      background: #faf8f5;
      border: 1px solid rgba(44, 24, 16, 0.1);

      &:hover:not(:disabled) {
        background: #fff;
        border-color: rgba(255, 106, 0, 0.5);
        color: #9a2f3c;
      }
    }

    .el-pager li {
      border-radius: 8px;
      margin: 0 3px;
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
        box-shadow: 0 4px 12px rgba(154, 47, 60, 0.3);
      }
    }

    .el-pagination__sizes {
      margin: 0 12px;
    }

    .el-pagination__jump {
      margin-left: 12px;
    }
  }
}

@media (max-width: 768px) {
  .book-list__hero-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .book-list__publish {
    width: 100%;
  }

  .book-search__bar {
    flex-direction: column;
    align-items: stretch;
    padding: 10px 12px;
    border-radius: 12px;
  }

  .book-search__seg {
    border-radius: 8px;

    &--category,
    &--status {
      flex: 1 1 auto;
    }

    &--input {
      flex: 1 1 auto;
    }
  }

  .book-search__submit {
    width: 100%;
    height: 44px;
    border-radius: 8px !important;
  }

  .book-grid {
    grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
    gap: 14px;
  }

  .book-card__actions {
    gap: 6px;
  }

  .book-card__btn {
    font-size: 12px;
    padding: 7px 2px;
  }
}
</style>

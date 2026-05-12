<template>
  <div class="wanted-list">
    <header class="wanted-list__hero">
      <div class="wanted-list__hero-inner">
        <div class="wanted-list__hero-copy">
          <p class="wanted-list__eyebrow">求购专区 · 精准匹配</p>
          <h1 class="wanted-list__title">求购列表</h1>
          <p class="wanted-list__subtitle">筛选心仪求购，点击查看详情或联系求购方</p>
        </div>
        <el-button type="primary" round size="large" class="wanted-list__publish" @click="goPublish">
          <el-icon class="el-icon--left"><Plus /></el-icon>
          发布求购
        </el-button>
      </div>
    </header>

    <section class="wanted-list__panel">
      <div class="wanted-search">
        <p class="wanted-search__tip">书名支持<strong>模糊匹配</strong>；选好状态后点击搜索</p>
        <div class="wanted-search__bar">
          <div class="wanted-search__seg wanted-search__seg--status">
            <el-select
              v-model="queryForm.status"
              placeholder="全部状态"
              clearable
              class="wanted-search__select"
              @keyup.enter="submitSearch"
            >
              <el-option label="求购中" :value="1" />
              <el-option label="已找到" :value="2" />
              <el-option label="已关闭" :value="0" />
            </el-select>
          </div>
          <div class="wanted-search__seg wanted-search__seg--input">
            <el-input
              v-model="queryForm.bookTitle"
              placeholder="输入书名关键词，模糊搜索"
              clearable
              class="wanted-search__input"
              @keyup.enter="submitSearch"
            >
              <template #prefix>
                <el-icon class="wanted-search__input-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button type="primary" class="wanted-search__submit" @click="submitSearch">搜索</el-button>
        </div>
        <div class="wanted-search__actions">
          <el-button link type="primary" @click="handleReset">重置条件</el-button>
        </div>
      </div>

      <div v-loading="loading" class="wanted-list__grid-wrap">
        <div v-if="tableData.length > 0" class="wanted-grid">
          <article
            v-for="row in tableData"
            :key="row.wantedId"
            class="wanted-card"
            role="link"
            tabindex="0"
            @click="goDetail(row)"
            @keydown.enter.prevent="goDetail(row)"
            @keydown.space.prevent="goDetail(row)"
          >
            <div class="wanted-card__media">
              <div class="wanted-card__media-inner">
                <el-image
                  v-if="coverSrc(row)"
                  :src="coverSrc(row)"
                  fit="cover"
                  class="wanted-card__img"
                  lazy
                >
                  <template #error>
                    <div class="wanted-card__placeholder">
                      <el-icon :size="36"><Picture /></el-icon>
                      <span>封面加载失败</span>
                    </div>
                  </template>
                </el-image>
                <div v-else class="wanted-card__placeholder">
                  <el-icon :size="40"><Reading /></el-icon>
                  <span>暂无封面</span>
                </div>
              </div>
              <span
                class="wanted-card__status"
                :class="{
                  'wanted-card__status--active': row.status === 1,
                  'wanted-card__status--found': row.status === 2,
                  'wanted-card__status--closed': row.status !== 1 && row.status !== 2
                }"
              >
                {{ statusText(row.status) }}
              </span>
              <span v-if="isOwnWanted(row)" class="wanted-card__own-badge">我的发布</span>
            </div>

            <div class="wanted-card__body">
              <div class="wanted-card__title-row">
                <span class="wanted-card__badge">求购</span>
                <h3 class="wanted-card__name" :title="row.bookTitle">{{ row.bookTitle }}</h3>
              </div>

              <div class="wanted-card__price-row">
                <span class="wanted-card__label">期望</span>
                <span class="wanted-card__yen">¥</span>
                <span class="wanted-card__price">{{ formatPrice(row.maxPrice) }}</span>
                <span class="wanted-card__price-hint">以内</span>
              </div>

              <div class="wanted-card__meta">
                <span class="wanted-card__buyer">
                  <el-icon><User /></el-icon>
                  {{ row.username || '匿名求购者' }}
                </span>
                <span class="wanted-card__views">
                  <el-icon><View /></el-icon>
                  {{ row.viewCount ?? 0 }}
                </span>
              </div>

              <p class="wanted-card__chips">{{ chipSummary(row) }}</p>

              <div class="wanted-card__actions">
                <button type="button" class="wanted-card__btn" @click.stop="goDetail(row)">详情</button>
                <button
                  type="button"
                  class="wanted-card__btn wanted-card__btn--primary"
                  :disabled="isOwnWanted(row) || row.status !== 1"
                  @click.stop="handleContact(row)"
                >
                  联系求购方
                </button>
              </div>
              <p v-if="isOwnWanted(row)" class="wanted-card__own-hint">当前为您的求购信息</p>
            </div>
          </article>
        </div>
        <el-empty v-else-if="!loading" description="没有找到符合条件的求购信息" class="wanted-list__empty" />
      </div>

      <div class="wanted-list__pagination">
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
import { ElMessage } from 'element-plus'
import { Picture, Plus, Reading, Search, User, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { BookWantedApi } from '@/api/OtherApi'

const router = useRouter()
const userStore = useUserStore()

const myUserId = computed(() => {
  const id = userStore.userInfo?.userId
  if (id === undefined || id === null || id === '') return null
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})

const getWantedUserId = (row) => row?.userId ?? row?.user_id

const isOwnWanted = (row) => {
  if (!row) return false
  const uid = getWantedUserId(row)
  if (uid !== undefined && uid !== null && uid !== '' && myUserId.value != null) {
    return Number(uid) === myUserId.value
  }
  const me = userStore.userInfo?.username
  if (me && row.username) return String(row.username) === String(me)
  return false
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  bookTitle: '',
  status: 1,
  page: 1,
  size: 12
})

function submitSearch() {
  if (typeof queryForm.bookTitle === 'string') {
    queryForm.bookTitle = queryForm.bookTitle.trim()
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
  if (status === 1) return '求购中'
  if (status === 2) return '已找到'
  if (status === 0) return '已关闭'
  return '未知'
}

function coverSrc(row) {
  const u = row?.coverImage ?? row?.cover_image
  return typeof u === 'string' && u.trim() ? u.trim() : ''
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

function chipSummary(row) {
  const parts = [row.author || '作者未定']
  if (row.isbn) parts.push(`ISBN ${row.isbn}`)
  parts.push(formatTime(row.createTime) || '时间未知')
  return parts.join(' · ')
}

function goDetail(row) {
  if (!row?.wantedId) return
  router.push({ name: 'WantedDetail', params: { wantedId: String(row.wantedId) } })
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await BookWantedApi.page(queryForm)
    if (res.status === 0) {
      tableData.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      total.value = res.recordsTotal ? parseInt(res.recordsTotal) : (res.data?.total || 0)
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.bookTitle = ''
  queryForm.status = 1
  queryForm.page = 1
  handleQuery()
}

const goPublish = () => {
  router.push('/dashboard/wanted/publish')
}

const handleContact = async (row) => {
  if (isOwnWanted(row)) {
    ElMessage.warning('无需联系自己')
    return
  }
  if (row.status !== 1) {
    ElMessage.warning('当前求购信息已关闭')
    return
  }
  
  let userId = row.userId || row.user_id
  if (!userId && row.wantedId) {
    try {
      const res = await BookWantedApi.detail(row.wantedId)
      if (res.status === 0 && res.data) {
        userId = res.data.userId
      }
    } catch (error) {
      ElMessage.error('获取用户信息失败')
      return
    }
  }
  
  if (!userId) {
    ElMessage.warning('用户信息不存在')
    return
  }
  
  router.push({
    path: '/dashboard/message/chat',
    query: {
      userId: userId,
      userName: row.username || '求购方'
    }
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.wanted-list {
  min-height: 100%;
  padding: 20px 20px 32px;
  box-sizing: border-box;
  background: var(--market-gradient-page);
}

.wanted-list__hero {
  margin-bottom: 20px;
}

.wanted-list__hero-inner {
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

.wanted-list__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9a2f3c;
}

.wanted-list__title {
  margin: 0 0 8px;
  font-size: clamp(1.5rem, 3vw, 1.85rem);
  font-weight: 800;
  color: #1c1917;
  letter-spacing: -0.03em;
}

.wanted-list__subtitle {
  margin: 0;
  font-size: 14px;
  color: #78716c;
  max-width: 420px;
  line-height: 1.5;
}

.wanted-list__publish {
  font-weight: 700 !important;
  padding: 12px 22px !important;
  border: none !important;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a) !important;
  box-shadow: 0 10px 24px rgba(123, 31, 42, 0.28);

  &:hover {
    filter: brightness(1.06);
  }
}

.wanted-list__panel {
  background: #fffefb;
  border-radius: 20px;
  padding: 20px 22px 24px;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 12px 40px rgba(44, 24, 16, 0.08);
}

.wanted-search {
  margin-bottom: 8px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(44, 24, 16, 0.08);
}

.wanted-search__tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #78716c;

  strong {
    color: #7b1f2a;
    font-weight: 600;
  }
}

.wanted-search__bar {
  display: flex;
  align-items: stretch;
  flex-wrap: nowrap;
  background: #fff;
  border: 2px solid #ff6a00;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 14px rgba(255, 106, 0, 0.12);
}

.wanted-search__seg {
  display: flex;
  align-items: center;
  border-right: 1px solid rgba(44, 24, 16, 0.08);
  background: #faf8f5;

  &--status {
    flex: 0 0 140px;
    min-width: 0;
  }

  &--input {
    flex: 1;
    min-width: 160px;
    background: #fff;
  }
}

.wanted-search__select {
  width: 100%;
}

.wanted-search__input {
  width: 100%;
}

.wanted-search__submit {
  flex-shrink: 0;
  min-width: 100px;
  height: auto !important;
  margin: 0 !important;
  border-radius: 0 !important;
  font-weight: 700 !important;
  font-size: 15px !important;
  padding: 0 22px !important;
  border: none !important;
  background: linear-gradient(180deg, #ff7a1a, #e85d00) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.15);

  &:hover {
    filter: brightness(1.06);
  }
}

.wanted-search__input-icon {
  color: #a8a29e;
  font-size: 18px;
}

.wanted-search__actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.wanted-search__bar {
  :deep(.wanted-search__seg .el-select .el-input__wrapper),
  :deep(.wanted-search__seg--input .el-input__wrapper) {
    box-shadow: none !important;
    border-radius: 0 !important;
    border: none !important;
    background: transparent !important;
    min-height: 48px;
  }

  :deep(.wanted-search__seg .el-select:hover:not(.is-disabled) .el-input__wrapper) {
    box-shadow: none !important;
  }

  :deep(.wanted-search__seg--input .el-input__inner::placeholder) {
    color: #a8a29e;
  }
}

.wanted-list__grid-wrap {
  min-height: 220px;
  padding-top: 8px;
}

.wanted-grid {
  display: grid;
  gap: 16px;
  /* 使用固定列数确保对称布局，3列或2列取决于可用宽度 */
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

.wanted-card {
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  cursor: pointer;
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

  &:focus-visible {
    outline: 2px solid rgba(255, 106, 0, 0.65);
    outline-offset: 2px;
  }
}

.wanted-card__media {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(145deg, #f3ece4, #e8dfd4);
  overflow: hidden;
}

.wanted-card__media-inner {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.wanted-card__img {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.35s ease;

  .wanted-card:hover & {
    transform: scale(1.04);
  }
}

.wanted-card__placeholder {
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

.wanted-card__status {
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

.wanted-card__status--active {
  background: rgba(34, 197, 94, 0.92);
  color: #fff;
}

.wanted-card__status--found {
  background: rgba(245, 158, 11, 0.94);
  color: #fff;
}

.wanted-card__status--closed {
  background: rgba(100, 116, 139, 0.9);
  color: #fff;
}

.wanted-card__own-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  font-size: 10px;
  font-weight: 600;
  border-radius: 6px;
  color: #7c3aed;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(124, 58, 237, 0.22);
  backdrop-filter: blur(6px);
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

.wanted-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 14px 14px 16px;
  gap: 8px;
}

.wanted-card__title-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 44px;
}

.wanted-card__badge {
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

.wanted-card__name {
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

.wanted-card__price-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 4px;
}

.wanted-card__label {
  font-size: 12px;
  color: #a8a29e;
}

.wanted-card__yen {
  font-size: 14px;
  font-weight: 800;
  color: #d4380d;
}

.wanted-card__price {
  font-size: 22px;
  font-weight: 800;
  color: #d4380d;
  letter-spacing: -0.03em;
  line-height: 1;
}

.wanted-card__price-hint {
  font-size: 12px;
  color: #a8a29e;
}

.wanted-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #78716c;
}

.wanted-card__buyer,
.wanted-card__views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;

  .el-icon {
    font-size: 14px;
    color: #a8a29e;
  }
}

.wanted-card__buyer {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wanted-card__chips {
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

.wanted-card__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: auto;
  padding-top: 12px;
}

.wanted-card__btn {
  border: 1px solid rgba(44, 24, 16, 0.1);
  background: #faf8f5;
  color: #44403c;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 6px;
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

.wanted-card__btn--primary {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border-color: rgba(251, 146, 60, 0.55);
  color: #c2410c;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #ffedd5, #fed7aa);
    border-color: #fb923c;
    color: #9a3412;
  }
}

.wanted-card__own-hint {
  margin: 0;
  font-size: 11px;
  color: #9a2f3c;
  text-align: center;
}

.wanted-list__empty {
  padding: 48px 0;
}

.wanted-list__pagination {
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
  .wanted-list__hero-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .wanted-list__publish {
    width: 100%;
  }

  .wanted-search__bar {
    flex-wrap: wrap;
    border-radius: 12px;
  }

  .wanted-search__seg {
    border-right: none;
    border-bottom: 1px solid rgba(44, 24, 16, 0.08);
    flex: 1 1 100% !important;
    background: #faf8f5;

    &--input {
      background: #fff;
    }
  }

  .wanted-search__submit {
    flex: 1 1 100%;
    min-height: 48px;
    border-radius: 0 !important;
  }

  .wanted-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
</style>
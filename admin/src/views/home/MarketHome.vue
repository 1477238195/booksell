<template>
  <div class="bazaar">
    <!-- 背景装饰 -->
    <div class="bg-pattern"></div>
    <div class="bg-glow bg-glow--1"></div>
    <div class="bg-glow bg-glow--2"></div>
    <div class="bg-glow bg-glow--3"></div>

    <!-- 顶部 Hero 区域 -->
    <header class="hero-section">
      <div class="hero-inner">
        <div class="hero-content">
          <div class="hero-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            </svg>
            二手书市集
          </div>
          <h1 class="hero-title">发现你的下一本好书</h1>
          <p class="hero-desc">在这里，每一本旧书都可能成为你的宝藏。买卖二手书，遇见志同道合的书友</p>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">{{ bookTotal }}</span>
            <span class="stat-label">在售书籍</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">{{ wantedTotal }}</span>
            <span class="stat-label">求购需求</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">今日更新</span>
          </div>
        </div>

        <div class="hero-actions">
          <button type="button" class="action-btn action-btn--primary" id="publish-btn" @click="goPublish">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            发布书籍
          </button>
          <button class="action-btn action-btn--secondary" @click="goPublishWanted">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
            发布求购
          </button>
        </div>
      </div>
    </header>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <div class="toolbar-inner">
        <div class="scope-switch">
          <button
            v-for="s in scopes"
            :key="s.value"
            class="scope-btn"
            :class="{ 'scope-btn--active': filter.scope === s.value }"
            @click="filter.scope = s.value; onScopeApply()"
          >
            <span class="scope-icon">
              <svg v-if="s.icon === 'grid'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
              <svg v-else-if="s.icon === 'book'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
            </span>
            {{ s.label }}
          </button>
        </div>

        <div class="filter-bar">
          <div class="filter-group">
            <el-select
              v-model="filter.bookCategoryId"
              placeholder="全部分类"
              clearable
              filterable
              size="small"
              :disabled="filter.scope === 'wanted'"
              class="filter-select"
            >
              <el-option v-for="c in categories" :key="c.categoryId" :label="c.name" :value="c.categoryId" />
            </el-select>
            <el-select
              v-model="filter.bookStatus"
              placeholder="书籍状态"
              clearable
              size="small"
              :disabled="filter.scope === 'wanted'"
              class="filter-select"
            >
              <el-option label="在售" :value="1" />
              <el-option label="已下架" :value="0" />
              <el-option label="已售出" :value="2" />
            </el-select>
            <el-select
              v-model="filter.wantedStatus"
              placeholder="求购状态"
              clearable
              size="small"
              :disabled="filter.scope === 'books'"
              class="filter-select"
            >
              <el-option label="求购中" :value="1" />
              <el-option label="已找到" :value="2" />
              <el-option label="已关闭" :value="0" />
            </el-select>
          </div>

          <div class="search-group">
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <path d="M21 21l-4.35-4.35"/>
              </svg>
              <el-input
                v-model="filter.keyword"
                placeholder="搜索书名..."
                clearable
                size="small"
                class="search-input"
                @keyup.enter="submitSearch"
              />
            </div>
            <button class="search-btn" @click="submitSearch">搜索</button>
            <button class="reset-btn" @click="resetFilters">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                <path d="M3 3v5h5"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="content-wrapper">
      <div class="split-layout">
        <!-- 左侧：书籍面板 -->
        <section v-show="showBooks" class="panel panel--books">
          <div class="panel-header">
            <div class="panel-title">
              <div class="panel-icon panel-icon--books">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
              </div>
              <div class="panel-text">
                <h2>在售书籍</h2>
                <span class="panel-count">{{ bookTotal }} 本</span>
              </div>
            </div>
            <router-link class="panel-more" to="/dashboard/book/list">
              查看全部
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </router-link>
          </div>

          <div v-loading="bookLoading" class="panel-body panel-body--books">
            <div v-if="books.length" class="book-grid">
              <article
                v-for="(book, index) in books"
                :key="book.bookId"
                class="book-card"
                :style="{ '--delay': `${index * 0.03}s` }"
                @click="goBook(book)"
              >
                <div class="book-cover">
                  <el-image
                    v-if="book.coverImage"
                    :src="book.coverImage"
                    fit="cover"
                    class="cover-img"
                    lazy
                    referrerpolicy="no-referrer"
                  >
                    <template #error>
                      <div class="cover-placeholder">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                        </svg>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="cover-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                    </svg>
                  </div>
                  <span class="book-status" :class="statusClass(book.status, 'book')">
                    {{ bookStatusText(book.status) }}
                  </span>
                </div>
                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <div class="book-price-row">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ formatPrice(book.price) }}</span>
                  </div>
                  <div class="book-meta-row">
                    <span v-if="getConditionText(book.condition)" class="meta-tag">{{ getConditionText(book.condition) }}</span>
                    <span class="meta-views">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                      {{ book.viewCount ?? 0 }}
                    </span>
                  </div>
                </div>
              </article>
            </div>
            <div v-else-if="!bookLoading" class="empty-state">
              <div class="empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
              </div>
              <h3>暂无书籍</h3>
              <p>还没有符合条件的书籍</p>
            </div>
          </div>
        </section>

        <!-- 右侧：求购面板 -->
        <section v-show="showWanted" class="panel panel--wanted">
          <div class="panel-header">
            <div class="panel-title">
              <div class="panel-icon panel-icon--wanted">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="M21 21l-4.35-4.35"/>
                </svg>
              </div>
              <div class="panel-text">
                <h2>求购信息</h2>
                <span class="panel-count">{{ wantedTotal }} 条</span>
              </div>
            </div>
            <router-link class="panel-more" to="/dashboard/wanted/list">
              查看全部
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </router-link>
          </div>

          <div v-loading="wantedLoading" class="panel-body panel-body--wanted">
            <div v-if="wanted.length" class="wanted-list">
              <article
                v-for="(w, index) in wanted"
                :key="w.wantedId"
                class="wanted-item"
                :style="{ '--delay': `${index * 0.04}s` }"
                @click="goWanted(w)"
              >
                <div class="wanted-cover">
                  <el-image
                    v-if="w.coverImage"
                    :src="w.coverImage"
                    fit="cover"
                    class="cover-img"
                    lazy
                    referrerpolicy="no-referrer"
                  >
                    <template #error>
                      <div class="cover-placeholder">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                          <circle cx="11" cy="11" r="8"/>
                          <path d="M21 21l-4.35-4.35"/>
                        </svg>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="cover-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <circle cx="11" cy="11" r="8"/>
                      <path d="M21 21l-4.35-4.35"/>
                    </svg>
                  </div>
                  <span class="wanted-status" :class="statusClass(w.status, 'wanted')">
                    {{ wantedStatusText(w.status) }}
                  </span>
                </div>
                <div class="wanted-info">
                  <div class="wanted-text">
                    <div class="wanted-title">{{ w.bookTitle }}</div>
                    <div v-if="w.author" class="wanted-author">{{ w.author }}</div>
                  </div>
                  <div class="wanted-right">
                    <div class="wanted-price">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{ formatPrice(w.maxPrice) }}</span>
                      <span class="price-hint">以内</span>
                    </div>
                    <span class="wanted-views">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                      {{ w.viewCount ?? 0 }}
                    </span>
                  </div>
                </div>
              </article>
            </div>
            <div v-else-if="!wantedLoading" class="empty-state">
              <div class="empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="M21 21l-4.35-4.35"/>
                </svg>
              </div>
              <h3>暂无求购</h3>
              <p>还没有符合条件的求购需求</p>
            </div>
          </div>
        </section>
      </div>

      <!-- 统一分页行 -->
      <div class="pagination-row">
        <div class="pager-box" v-if="bookTotal > bookPager.size">
          <button class="pager-btn" :disabled="bookPager.page <= 1" @click="bookPager.page--; loadBooks()">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
            上一页
          </button>
          <span class="pager-info">{{ bookPager.page }} / {{ Math.max(1, Math.ceil(bookTotal / bookPager.size)) }}</span>
          <button class="pager-btn" :disabled="bookPager.page >= Math.ceil(bookTotal / bookPager.size)" @click="bookPager.page++; loadBooks()">
            下一页
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6"/>
            </svg>
          </button>
        </div>
        <div class="pager-box" v-if="wantedTotal > wantedPager.size">
          <button class="pager-btn" :disabled="wantedPager.page <= 1" @click="wantedPager.page--; loadWanted()">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
            上一页
          </button>
          <span class="pager-info">{{ wantedPager.page }} / {{ Math.max(1, Math.ceil(wantedTotal / wantedPager.size)) }}</span>
          <button class="pager-btn" :disabled="wantedPager.page >= Math.ceil(wantedTotal / wantedPager.size)" @click="wantedPager.page++; loadWanted()">
            下一页
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { BookCategoryApi } from '@/api/BookApi'
import RecommendApi from '@/api/RecommendApi'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([])
const books = ref([])
const wanted = ref([])
const bookTotal = ref(0)
const wantedTotal = ref(0)
const bookLoading = ref(false)
const wantedLoading = ref(false)

const scopes = [
  { label: '全部', value: 'all', icon: 'grid' },
  { label: '仅书籍', value: 'books', icon: 'book' },
  { label: '仅求购', value: 'wanted', icon: 'search' }
]

const filter = reactive({
  scope: 'all',
  keyword: '',
  bookCategoryId: null,
  bookStatus: 1,
  wantedStatus: 1
})

const bookPager = reactive({ page: 1, size: 15 })
const wantedPager = reactive({ page: 1, size: 9 })

const showBooks = computed(() => filter.scope === 'all' || filter.scope === 'books')
const showWanted = computed(() => filter.scope === 'all' || filter.scope === 'wanted')

function onScopeApply() {
  bookPager.page = 1
  wantedPager.page = 1
  loadBoth()
}

function formatPrice(val) {
  if (val === undefined || val === null || val === '') return '0'
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return String(parseFloat(n.toFixed(2)))
}

function getConditionText(c) {
  const m = { 1: '全新', 2: '几乎全新', 3: '轻微痕迹', 4: '明显痕迹' }
  return m[c] || ''
}

function bookStatusText(s) { return s === 1 ? '在售' : s === 2 ? '已售' : s === 0 ? '下架' : '' }
function wantedStatusText(s) { return s === 1 ? '求购中' : s === 2 ? '已找到' : s === 0 ? '已关闭' : '' }

function statusClass(s, type) {
  if (type === 'book') return s === 1 ? 'tag-sale' : s === 2 ? 'tag-done' : 'tag-off'
  return s === 1 ? 'tag-wanted' : s === 2 ? 'tag-done' : 'tag-off'
}

async function loadCategories() {
  try {
    const res = await BookCategoryApi.list()
    if (res.status === 0) categories.value = res.data || []
  } catch { /* */ }
}

async function loadBooks() {
  if (!showBooks.value) { books.value = []; bookTotal.value = 0; return }
  bookLoading.value = true
  try {
    const q = { page: bookPager.page, size: bookPager.size, categoryId: filter.bookCategoryId, status: filter.bookStatus }
    const t = (filter.keyword || '').trim()
    if (t) q.title = t
    const res = await RecommendApi.homeBooks(q)
    if (res.status === 0) {
      books.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      bookTotal.value = res.recordsTotal ? parseInt(res.recordsTotal, 10) : res.data?.total || 0
    }
  } catch { /* */ } finally { bookLoading.value = false }
}

async function loadWanted() {
  if (!showWanted.value) { wanted.value = []; wantedTotal.value = 0; return }
  wantedLoading.value = true
  try {
    const q = { page: wantedPager.page, size: wantedPager.size, status: filter.wantedStatus }
    const t = (filter.keyword || '').trim()
    if (t) q.bookTitle = t
    const res = await RecommendApi.homeWanted(q)
    if (res.status === 0) {
      wanted.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      wantedTotal.value = res.recordsTotal ? parseInt(res.recordsTotal, 10) : res.data?.total || 0
    }
  } catch { /* */ } finally { wantedLoading.value = false }
}

async function loadBoth() { await Promise.all([loadBooks(), loadWanted()]) }

function submitSearch() {
  if (typeof filter.keyword === 'string') filter.keyword = filter.keyword.trim()
  bookPager.page = 1; wantedPager.page = 1
  loadBoth()
}

function resetFilters() {
  filter.scope = 'all'; filter.keyword = ''; filter.bookCategoryId = null
  filter.bookStatus = 1; filter.wantedStatus = 1
  bookPager.page = 1; wantedPager.page = 1
  loadBoth()
}

function goBook(row) { if (row?.bookId) router.push({ name: 'BookDetail', params: { bookId: String(row.bookId) } }) }
function goWanted(row) { if (row?.wantedId) router.push({ name: 'WantedDetail', params: { wantedId: String(row.wantedId) } }) }
function goPublish() {
  router.push('/dashboard/book/publish')
}
function goPublishWanted() { router.push('/dashboard/wanted/publish') }

onMounted(() => {
  loadCategories(); loadBoth()
  console.log('[MarketHome] onMounted 触发')
})
</script>

<style lang="scss" scoped>
// 颜色变量
$primary: #7b1f2a;
$primary-light: #9a2f3c;
$accent: #d4a574;
$accent-light: #e8c9a8;
$bg-cream: #faf6f0;
$bg-warm: #f5efe6;
$text-dark: #2c1810;
$text-medium: #5c4a3d;
$text-light: #8b7355;
$white: #fffefb;
$success: #4d7c3b;
$wanted: #5b7bb5;

// 背景装饰
.bg-pattern {
  position: fixed;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%237b1f2a' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}

.bg-glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  z-index: 0;

  &--1 {
    width: 600px;
    height: 600px;
    top: -200px;
    right: -100px;
    background: radial-gradient(circle, rgba($primary, 0.1) 0%, transparent 70%);
  }

  &--2 {
    width: 500px;
    height: 500px;
    top: 50%;
    left: -150px;
    background: radial-gradient(circle, rgba($accent, 0.12) 0%, transparent 70%);
  }

  &--3 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    right: 10%;
    background: radial-gradient(circle, rgba($primary-light, 0.08) 0%, transparent 70%);
  }
}

.bazaar {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(160deg, $bg-cream 0%, $bg-warm 50%, darken($bg-cream, 3%) 100%);
}

// Hero 区域
.hero-section {
  position: relative;
  padding: 48px 32px 40px;
  margin-bottom: 24px;
  z-index: 2;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, $primary 0%, $primary-light 50%, darken($primary, 5%) 100%);
    clip-path: polygon(0 0, 100% 0, 100% 75%, 0 100%);
    z-index: -1;
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3z' fill='%23fff' fill-opacity='0.03' fill-rule='evenodd'/%3E%3C/svg%3E");
    z-index: -1;
  }
}

.hero-inner {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
}

.hero-content {
  flex: 1;
  min-width: 300px;
  color: #fff;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;

  svg { width: 16px; height: 16px; }
}

.hero-title {
  margin: 0 0 12px;
  font-size: 2.5rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.hero-desc {
  margin: 0;
  font-size: 16px;
  opacity: 0.85;
  max-width: 500px;
  line-height: 1.6;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .stat-number {
    font-size: 2.2rem;
    font-weight: 800;
    line-height: 1;
  }

  .stat-label {
    font-size: 12px;
    opacity: 0.7;
  }
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 28px;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  font-family: inherit;

  svg { width: 20px; height: 20px; }

  &--primary {
    background: linear-gradient(135deg, $accent, darken($accent, 10%));
    color: $text-dark;
    box-shadow: 0 8px 30px rgba($accent, 0.4);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 12px 40px rgba($accent, 0.5);
    }
  }

  &--secondary {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.2);

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateY(-2px);
    }
  }
}

// 工具栏
.toolbar {
  position: relative;
  padding: 0 40px;
  margin-bottom: 24px;
}

.toolbar-inner {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  background: $white;
  border-radius: 16px;
  box-shadow: 0 2px 20px rgba($text-dark, 0.06);
  border: 1px solid rgba($text-dark, 0.04);
  flex-wrap: nowrap;
}

.scope-switch {
  display: flex;
  gap: 3px;
  padding: 3px;
  background: $bg-cream;
  border-radius: 10px;
  flex-shrink: 0;
}

.scope-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: $text-light;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;

  .scope-icon {
    font-size: 13px;
    display: flex;
    align-items: center;

    svg { width: 13px; height: 13px; }
  }

  &:hover {
    color: $primary;
    background: rgba($primary, 0.06);
  }

  &--active {
    background: $white;
    color: $primary;
    box-shadow: 0 1px 6px rgba($text-dark, 0.08);
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.filter-select {
  flex-shrink: 0;
  width: 108px;

  :deep(.el-select__wrapper) {
    padding: 4px 10px;
    border-radius: 8px;
    border: 1px solid rgba($text-dark, 0.08);
    box-shadow: none;
    min-height: 30px;
    font-size: 12px;
    background: $bg-cream;
    transition: all 0.2s ease;

    &:hover { border-color: rgba($primary, 0.3); background: $white; }
    &.is-focused {
      border-color: $primary;
      box-shadow: 0 0 0 2px rgba($primary, 0.08);
      background: $white;
    }
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 140px;
  padding: 4px 12px;
  background: $bg-cream;
  border: 1px solid transparent;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:focus-within {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 2px rgba($primary, 0.08);
  }

  .search-icon {
    width: 14px;
    height: 14px;
    color: $text-light;
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      padding: 0;
      background: transparent;
      border: none;
      box-shadow: none;
      min-height: 22px;
    }

    :deep(.el-input__inner) {
      font-size: 13px;
      color: $text-dark;

      &::placeholder { color: $text-light; }
    }
  }
}

.search-btn {
  padding: 7px 16px;
  background: $primary;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  font-family: inherit;

  &:hover {
    background: $primary-light;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba($primary, 0.3);
  }
}

.reset-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-cream;
  border: none;
  border-radius: 8px;
  color: $text-light;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;

  svg { width: 14px; height: 14px; }

  &:hover {
    background: rgba($primary, 0.08);
    color: $primary;
  }
}

// 内容区域
.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px 60px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  @media (max-width: 1024px) {
    padding: 0 20px 40px;
  }
}

// 左右分栏布局 - 确保左右完全对称
.split-layout {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
  align-items: stretch;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}

// 面板样式 - 左右对称，高度自适应内容
.panel {
  background: $white;
  border-radius: 16px;
  border: 1px solid rgba($text-dark, 0.06);
  box-shadow: 0 2px 16px rgba($text-dark, 0.04);
  overflow: hidden;
  height: auto;
  display: flex;
  flex-direction: column;

  &--books {
    border-top: 3px solid $success;
  }

  &--wanted {
    border-top: 3px solid $wanted;
    height: 940px;
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid rgba($text-dark, 0.05);
  flex-shrink: 0;
  height: 50px;
  box-sizing: border-box;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;

  svg { width: 18px; height: 18px; }

  &--books {
    background: linear-gradient(135deg, rgba($success, 0.15), rgba($success, 0.25));
    color: $success;
  }

  &--wanted {
    background: linear-gradient(135deg, rgba($wanted, 0.15), rgba($wanted, 0.25));
    color: $wanted;
  }
}

.panel-text {
  h2 {
    margin: 0;
    font-size: 14px;
    font-weight: 700;
    color: $text-dark;
  }

  .panel-count {
    font-size: 11px;
    color: $text-light;
    margin-left: 6px;
  }
}

.panel-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: $primary;
  text-decoration: none;
  transition: all 0.2s ease;
  padding: 4px 8px;
  border-radius: 6px;

  svg { width: 12px; height: 12px; }

  &:hover {
    color: $primary-light;
    background: rgba($primary, 0.06);
  }
}

.panel-body {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.panel-footer {
  flex-shrink: 0;
  padding: 8px 14px;
  border-top: 1px solid rgba($text-dark, 0.05);
}

// ============ 书籍卡片网格 ============
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.panel-body--books {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  padding: 6px;

  @media (max-width: 1400px) {
    grid-template-columns: repeat(4, 1fr);
  }

  @media (max-width: 1100px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
    padding: 6px;
  }
}

.book-card {
  background: $white;
  border: 1px solid rgba($text-dark, 0.07);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.32, 0.72, 0, 1);
  animation: fadeIn 0.35s ease forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba($text-dark, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba($text-dark, 0.1);
    border-color: rgba($primary, 0.15);

    .book-cover .cover-img,
    .book-cover .cover-placeholder { transform: scale(1.05); }
  }
}

.book-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  background: linear-gradient(135deg, $bg-warm, darken($bg-warm, 4%));
  overflow: hidden;

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s cubic-bezier(0.32, 0.72, 0, 1);
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-light;
  opacity: 0.25;
  transition: transform 0.4s cubic-bezier(0.32, 0.72, 0, 1);

  svg { width: 28px; height: 28px; }
}

.book-status {
  position: absolute;
  top: 6px;
  right: 6px;
  padding: 2px 7px;
  border-radius: 5px;
  font-size: 9px;
  font-weight: 700;
  backdrop-filter: blur(4px);
  letter-spacing: 0.02em;
}

.tag-sale   { background: rgba($success, 0.85); color: #fff; }
.tag-done   { background: rgba($accent, 0.85); color: $text-dark; }
.tag-off    { background: rgba(0,0,0,0.5); color: #fff; }
.tag-wanted { background: rgba($wanted, 0.85); color: #fff; }

.book-info {
  padding: 5px 8px 7px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex-shrink: 0;
  background: $white;
}

.book-title {
  margin: 0;
  font-size: 12.5px;
  font-weight: 700;
  color: $text-dark;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 34px;
}

.book-price-row {
  display: flex;
  align-items: baseline;
  gap: 1px;

  .price-symbol {
    font-size: 11px;
    font-weight: 700;
    color: $primary;
  }

  .price-value {
    font-size: 15px;
    font-weight: 800;
    color: $primary;
    line-height: 1;
  }
}

.book-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;

  .meta-tag {
    padding: 2px 6px;
    background: rgba($primary, 0.07);
    border-radius: 4px;
    font-size: 9px;
    font-weight: 600;
    color: $primary;
    letter-spacing: 0.01em;
  }

  .meta-views {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 10px;
    color: $text-light;

    svg { width: 10px; height: 10px; }
  }
}

// ============ 求购列表 ============
.panel-body--wanted {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0;
}

.wanted-list {
  display: flex;
  flex-direction: column;
}

.wanted-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $white;
  border-bottom: 1px solid rgba($text-dark, 0.04);
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.32, 0.72, 0, 1);
  animation: fadeIn 0.35s ease forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba($wanted, 0.04);
    padding-left: 18px;

    .wanted-cover .cover-img,
    .wanted-cover .cover-placeholder { transform: scale(1.06); }
  }
}

.wanted-cover {
  position: relative;
  width: 52px;
  height: 68px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: linear-gradient(135deg, #ece6f0, #d8d2e8);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $wanted;
    opacity: 0.35;

    svg { width: 20px; height: 20px; }
  }
}

.wanted-status {
  position: absolute;
  top: 3px;
  right: 3px;
  padding: 1px 4px;
  border-radius: 4px;
  font-size: 8px;
  font-weight: 700;
  backdrop-filter: blur(4px);
  letter-spacing: 0.02em;
}

.wanted-info {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.wanted-text {
  flex: 1;
  min-width: 0;
}

.wanted-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: $text-dark;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.wanted-author {
  margin: 3px 0 0;
  font-size: 12px;
  color: $text-light;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.wanted-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
  flex-shrink: 0;
}

.wanted-price {
  display: flex;
  align-items: baseline;
  gap: 1px;

  .price-symbol {
    font-size: 11px;
    font-weight: 700;
    color: $primary;
  }

  .price-value {
    font-size: 14px;
    font-weight: 800;
    color: $primary;
    line-height: 1;
  }

  .price-hint {
    font-size: 10px;
    color: $text-light;
    margin-left: 2px;
  }
}

.wanted-views {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  color: $text-light;

  svg { width: 10px; height: 10px; }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
  text-align: center;

  .empty-icon {
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-cream;
    border-radius: 16px;
    color: $text-light;
    opacity: 0.45;
    margin-bottom: 14px;

    svg { width: 30px; height: 30px; }
  }

  h3 {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 700;
    color: $text-dark;
  }

  p {
    margin: 0;
    font-size: 12px;
    color: $text-light;
  }
}

// 分页
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.pager-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: $bg-cream;
  border: 1px solid rgba($text-dark, 0.08);
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: $text-medium;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;

  svg { width: 14px; height: 14px; }

  &:hover:not(:disabled) {
    background: $primary;
    color: #fff;
    border-color: $primary;
    box-shadow: 0 4px 12px rgba($primary, 0.25);
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.35;
    cursor: not-allowed;
  }
}

.pager-info {
  font-size: 13px;
  color: $text-medium;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  min-width: 60px;
  text-align: center;
}

// ============ 统一分页行 ============
.pagination-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 12px;
  align-items: stretch;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.pager-box {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  padding: 10px 16px;
  background: $white;
  border-radius: 12px;
  border: 1px solid rgba($text-dark, 0.06);
  box-shadow: 0 1px 6px rgba($text-dark, 0.04);
}

// 响应式
@media (max-width: 768px) {
  .hero-section {
    padding: 36px 20px 40px;

    .hero-title { font-size: 1.9rem; }
    .hero-desc { font-size: 13px; }
  }

  .hero-stats {
    padding: 16px 20px;
    gap: 16px;
    .stat-number { font-size: 1.6rem; }
    .stat-label { font-size: 10px; }
  }

  .toolbar { padding: 0 16px; }

  .toolbar-inner {
    padding: 8px 12px;
    gap: 8px;
    flex-wrap: wrap;
  }

  .filter-group { flex-wrap: wrap; gap: 5px; }
  .filter-select { width: 98px; }
  .search-group { width: 100%; }

  .split-layout {
    grid-template-columns: 1fr;
  }

  .panel {
    height: auto;
  }

  .panel-body--books,
  .panel-body--wanted {
    min-height: 300px;
  }

  .book-grid {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: auto;
  }
}
</style>

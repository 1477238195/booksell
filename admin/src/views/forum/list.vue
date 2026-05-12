<template>
  <div class="forum-page">
    <!-- 背景装饰 -->
    <div class="bg-pattern"></div>
    <div class="bg-glow bg-glow--1"></div>
    <div class="bg-glow bg-glow--2"></div>

    <!-- 顶部 Hero 区域 -->
    <header class="hero-section">
      <div class="hero-inner">
        <div class="hero-brand">
          <div class="hero-icon">
            <svg viewBox="0 0 48 48" fill="none">
              <path d="M8 12C8 9.79086 9.79086 8 12 8H36C38.2091 8 40 9.79086 40 12V36C40 38.2091 38.2091 40 36 40H12C9.79086 40 8 38.2091 8 36V12Z" stroke="currentColor" stroke-width="2"/>
              <path d="M16 18H32M16 24H28M16 30H24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="hero-text">
            <span class="hero-tag">📚 书友社区</span>
            <h1 class="hero-title">书友论坛</h1>
            <p class="hero-desc">分享阅读心得，交流书籍故事，发现志同道合的书友</p>
          </div>
        </div>

        <div class="hero-stats">
          <div class="stat-card">
            <span class="stat-number">{{ total }}</span>
            <span class="stat-label">帖子</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-card">
            <span class="stat-number">{{ boards.length }}</span>
            <span class="stat-label">版块</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-card">
            <span class="stat-number">0</span>
            <span class="stat-label">今日</span>
          </div>
        </div>

        <button class="hero-btn" @click="goPublish">
          <span class="btn-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14" stroke-linecap="round"/>
            </svg>
          </span>
          发布帖子
        </button>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="content-wrapper">
      <!-- 左侧边栏 -->
      <aside class="sidebar">
        <!-- 版块选择卡片 -->
        <div class="sidebar-card">
          <div class="card-header">
            <div class="card-title">
              <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/>
                <rect x="14" y="14" width="7" height="7" rx="1"/>
              </svg>
              热门版块
            </div>
          </div>

          <div class="board-list">
            <button
              class="board-item"
              :class="{ 'board-item--active': boardId === null }"
              @click="boardId = null; submitSearch()"
            >
              <span class="board-icon">📚</span>
              <span class="board-name">全部</span>
              <span class="board-arrow">→</span>
            </button>
            <button
              v-for="b in boards"
              :key="b.boardId"
              class="board-item"
              :class="{ 'board-item--active': boardId === b.boardId }"
              @click="boardId = b.boardId; submitSearch()"
            >
              <span class="board-icon">{{ getBoardIcon(b.name) }}</span>
              <span class="board-name">{{ b.name }}</span>
              <span class="board-arrow">→</span>
            </button>
          </div>
        </div>

        <!-- 欢迎卡片 -->
        <div class="sidebar-card sidebar-card--welcome">
          <div class="welcome-illustration">
            <svg viewBox="0 0 120 80" fill="none">
              <path d="M20 70V30L40 20L60 30V70H20Z" fill="currentColor" opacity="0.1"/>
              <path d="M40 20L60 30L80 20V70H40L60 30" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M60 30V70" stroke="currentColor" stroke-width="2"/>
              <path d="M40 20V70" stroke="currentColor" stroke-width="2"/>
              <path d="M80 20V70" stroke="currentColor" stroke-width="2"/>
              <path d="M20 30H40M60 30H80" stroke="currentColor" stroke-width="2"/>
              <path d="M20 40H40M60 40H80" stroke="currentColor" stroke-width="2"/>
              <path d="M20 50H40M60 50H80" stroke="currentColor" stroke-width="2"/>
              <path d="M20 60H40M60 60H80" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="welcome-title">欢迎来到论坛</h3>
          <p class="welcome-desc">在这里分享你的读书心得，与志同道合的书友交流讨论</p>
          <button class="welcome-btn" @click="goPublish">
            发起讨论
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <!-- 发帖须知 -->
        <div class="sidebar-card sidebar-card--tips">
          <div class="card-header">
            <div class="card-title">
              <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 16v-4M12 8h.01"/>
              </svg>
              发帖须知
            </div>
          </div>
          <ul class="tips-list">
            <li class="tip-item">
              <span class="tip-icon">✿</span>
              <span>尊重他人，友善交流</span>
            </li>
            <li class="tip-item">
              <span class="tip-icon">✿</span>
              <span>分享真实读书感受</span>
            </li>
            <li class="tip-item">
              <span class="tip-icon">✿</span>
              <span>可关联市集书籍</span>
            </li>
            <li class="tip-item">
              <span class="tip-icon">✿</span>
              <span>优质内容获得关注</span>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="sort-tabs">
            <button
              class="sort-tab"
              :class="{ 'sort-tab--active': sort === 'latest' }"
              @click="sort = 'latest'; submitSearch()"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 6v6l4 2"/>
              </svg>
              最新
            </button>
            <button
              class="sort-tab"
              :class="{ 'sort-tab--active': sort === 'hot' }"
              @click="sort = 'hot'; submitSearch()"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
              </svg>
              最热
            </button>
            <button
              class="sort-tab"
              :class="{ 'sort-tab--active': sort === 'featured' }"
              @click="sort = 'featured'; submitSearch()"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              精华
            </button>
          </div>

          <div class="search-area">
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <path d="M21 21l-4.35-4.35"/>
              </svg>
              <input
                v-model="keyword"
                class="search-input"
                placeholder="搜索帖子..."
                @keyup.enter="submitSearch"
              />
              <button v-if="keyword" class="search-clear" @click="keyword = ''; submitSearch()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <button class="search-btn" @click="submitSearch">
              搜索
            </button>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div v-loading="loading" class="topic-list">
          <article
            v-for="(topic, index) in topics"
            :key="topic.topicId"
            class="topic-card"
            :style="{ '--delay': `${index * 0.05}s` }"
            @click="openTopic(topic.topicId)"
          >
            <div class="topic-avatar">
              <el-avatar :size="52" :src="topic.authorAvatar" class="avatar-img">
                {{ (topic.authorName || '书')[0] }}
              </el-avatar>
              <div class="avatar-decoration"></div>
            </div>

            <div class="topic-body">
              <div class="topic-header">
                <div class="topic-badges">
                  <span v-if="topic.pinned === 1" class="badge badge--pin">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M12 2v20M17 7l-5-5-5 5"/>
                    </svg>
                    置顶
                  </span>
                  <span v-if="isHot(topic)" class="badge badge--hot">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M12 2l1.5 4.5h4.5l-3.5 2.5 1.5 4.5-4-3-4 3 1.5-4.5-3.5-2.5h4.5z"/>
                    </svg>
                    热门
                  </span>
                  <span v-if="topic.boardName" class="badge badge--board">{{ topic.boardName }}</span>
                </div>
                <h3 class="topic-title">{{ topic.title }}</h3>
              </div>

              <p class="topic-excerpt">{{ getExcerpt(topic.content) }}</p>

              <div class="topic-meta">
                <span class="topic-author">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  {{ topic.authorName || '书友' }}
                </span>
                <span class="topic-time">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 6v6l4 2"/>
                  </svg>
                  {{ fmtTime(topic.lastReplyAt || topic.createTime) }}
                </span>
              </div>

              <div v-if="topic.bookTitle" class="topic-book">
                <div class="book-cover">
                  <el-image
                    v-if="topic.bookCover"
                    :src="topic.bookCover"
                    fit="cover"
                    class="book-img"
                    referrerpolicy="no-referrer"
                  />
                  <div v-else class="book-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                    </svg>
                  </div>
                </div>
                <div class="book-info">
                  <span class="book-label">关联书籍</span>
                  <span class="book-title">《{{ topic.bookTitle }}》</span>
                </div>
              </div>
            </div>

            <div class="topic-stats">
              <div class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <span>{{ formatCount(topic.viewCount) }}</span>
              </div>
              <div class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ formatCount(topic.replyCount || 0) }}</span>
              </div>
            </div>
          </article>

          <!-- 空状态 -->
          <div v-if="!loading && !topics.length" class="empty-state">
            <div class="empty-illustration">
              <svg viewBox="0 0 200 160" fill="none">
                <rect x="40" y="30" width="120" height="100" rx="8" stroke="currentColor" stroke-width="2" opacity="0.3"/>
                <path d="M70 70h60M70 90h40M70 110h50" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
                <circle cx="100" cy="80" r="30" stroke="currentColor" stroke-width="2" stroke-dasharray="4 4" opacity="0.5"/>
                <path d="M115 95l20 20" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
                <path d="M135 95l-20 20" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
              </svg>
            </div>
            <h3 class="empty-title">暂无相关帖子</h3>
            <p class="empty-desc">成为第一个发帖的书友吧！</p>
            <button class="empty-btn" @click="goPublish">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              发布帖子
            </button>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > query.size" class="pagination">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, prev, pager, next"
            background
            @size-change="loadTopics"
            @current-change="loadTopics"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ForumApi } from '@/api/OtherApi'

const router = useRouter()
const route = useRoute()

const boards = ref([])
const topics = ref([])
const total = ref(0)
const loading = ref(false)
const boardId = ref(null)
const keyword = ref('')
const sort = ref('latest')

const query = reactive({
  page: 1,
  size: 15
})

function getBoardIcon(name) {
  const iconMap = {
    '综合讨论': '💭', '书评读后感': '📖', '求书换书': '🔄',
    '交易相关': '💰', '读书笔记': '📝', '书评': '⭐',
    '交换': '🔄', '推荐': '👍', '求助': '❓',
    '讨论': '💬', '分享': '🎁', '二手': '📚'
  }
  return iconMap[name] || '📚'
}

function fmtTime(t) {
  if (!t) return ''
  try {
    const d = new Date(t)
    const now = new Date()
    const diff = now - d
    const minute = 60 * 1000
    const hour = 60 * minute
    const day = 24 * hour

    if (diff < minute) return '刚刚'
    if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
    if (diff < day) return `${Math.floor(diff / hour)} 小时前`
    if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`

    return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  } catch {
    return String(t)
  }
}

function getExcerpt(content) {
  if (!content) return ''
  return content.length > 100 ? content.slice(0, 100) + '...' : content
}

function formatCount(count) {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return String(count)
}

function isHot(topic) {
  return (topic.replyCount || 0) >= 10 || (topic.viewCount || 0) >= 500
}

async function loadBoards() {
  try {
    const res = await ForumApi.boards()
    if (res.status === 0 && res.data) {
      boards.value = Array.isArray(res.data) ? res.data : []
    }
  } catch {
    boards.value = []
  }
}

async function loadTopics() {
  loading.value = true
  try {
    const res = await ForumApi.topicPage({
      boardId: boardId.value ?? undefined,
      keyword: keyword.value?.trim() || undefined,
      bookId: route.query.bookId ? Number(route.query.bookId) : undefined,
      page: query.page,
      size: query.size
    })
    if (res.status === 0) {
      const data = res.data
      if (Array.isArray(data)) {
        topics.value = data
        total.value = data.length
      } else if (data) {
        topics.value = data.list || data.records || []
        total.value = data.total || data.recordsTotal || data.count || 0
      } else {
        topics.value = []
        total.value = 0
      }
    }
  } catch (e) {
    console.error('加载帖子失败:', e)
    topics.value = []
  } finally {
    loading.value = false
  }
}

function submitSearch() {
  query.page = 1
  loadTopics()
}

function openTopic(id) {
  router.push({ name: 'ForumTopic', params: { topicId: String(id) } })
}

function goPublish() {
  const q = {}
  if (route.query.bookId) q.bookId = route.query.bookId
  router.push({ name: 'ForumPublish', query: q })
}

onMounted(async () => {
  await loadBoards()
  if (route.query.boardId) {
    boardId.value = Number(route.query.boardId) || null
  }
  await loadTopics()
})
</script>

<style lang="scss" scoped>
// 颜色变量
$primary: #7b1f2a;
$primary-light: #9a2f3c;
$primary-dark: #5a171f;
$accent: #d4a574;
$accent-light: #e8c9a8;
$bg-cream: #faf6f0;
$bg-warm: #f5efe6;
$text-dark: #2c1810;
$text-medium: #5c4a3d;
$text-light: #8b7355;
$white: #fffefb;

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
  filter: blur(100px);
  pointer-events: none;
  z-index: 0;

  &--1 {
    width: 600px;
    height: 600px;
    top: -200px;
    right: -100px;
    background: radial-gradient(circle, rgba(123, 31, 42, 0.08) 0%, transparent 70%);
  }

  &--2 {
    width: 500px;
    height: 500px;
    bottom: -100px;
    left: -150px;
    background: radial-gradient(circle, rgba(212, 165, 116, 0.1) 0%, transparent 70%);
  }
}

.forum-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(160deg, $bg-cream 0%, $bg-warm 50%, darken($bg-cream, 3%) 100%);
  z-index: 1;
}

// Hero 区域
.hero-section {
  position: relative;
  padding: 48px 24px 40px;
  margin-bottom: 32px;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, $primary 0%, $primary-light 50%, $primary-dark 100%);
    clip-path: polygon(0 0, 100% 0, 100% 70%, 0 100%);
    z-index: -1;
  }
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  min-width: 300px;
}

.hero-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);

  svg { width: 40px; height: 40px; }
}

.hero-text {
  color: #fff;

  .hero-tag {
    display: inline-block;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    opacity: 0.85;
    margin-bottom: 6px;
  }

  .hero-title {
    margin: 0 0 8px;
    font-size: 2.2rem;
    font-weight: 800;
    letter-spacing: 0.02em;
  }

  .hero-desc {
    margin: 0;
    font-size: 15px;
    opacity: 0.8;
    max-width: 400px;
  }
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .stat-number {
    font-size: 2rem;
    font-weight: 800;
    line-height: 1;
    color: #fff;
  }

  .stat-label {
    font-size: 12px;
    opacity: 0.7;
  }
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

.hero-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  background: linear-gradient(135deg, $accent 0%, darken($accent, 10%) 100%);
  color: $text-dark;
  border: none;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  box-shadow: 0 8px 30px rgba($accent, 0.4);

  .btn-icon {
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;

    svg { width: 100%; height: 100%; }
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 40px rgba($accent, 0.5);
  }

  &:active {
    transform: translateY(-1px);
  }
}

// 内容区域
.content-wrapper {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px 48px;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 28px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;

    .sidebar { display: none; }
  }
}

// 侧边栏
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: $white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba($text-dark, 0.06);
  border: 1px solid rgba($text-dark, 0.04);

  &--welcome {
    background: linear-gradient(135deg, rgba($primary, 0.05) 0%, rgba($accent, 0.08) 100%);
    border-color: rgba($primary, 0.1);
  }

  &--tips {
    .tips-list {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .tip-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      font-size: 13px;
      color: $text-medium;
      border-bottom: 1px solid rgba($text-dark, 0.04);

      &:last-child { border-bottom: none; }
    }

    .tip-icon {
      font-size: 10px;
      color: $primary;
    }
  }
}

.card-header {
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: $text-dark;

  .title-icon {
    width: 20px;
    height: 20px;
    color: $primary;
  }
}

.board-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.board-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: transparent;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: left;
  font-family: inherit;

  .board-icon { font-size: 18px; }
  .board-name { flex: 1; font-size: 14px; color: $text-medium; }
  .board-arrow { font-size: 12px; color: $text-light; opacity: 0; transform: translateX(-4px); transition: all 0.25s ease; }

  &:hover {
    background: rgba($primary, 0.06);

    .board-name { color: $primary; }
    .board-arrow { opacity: 1; transform: translateX(0); color: $primary; }
  }

  &--active {
    background: rgba($primary, 0.1);

    .board-name { color: $primary; font-weight: 600; }
    .board-arrow { opacity: 1; transform: translateX(0); color: $primary; }
  }
}

.welcome-illustration {
  margin-bottom: 16px;
  color: $primary;
  opacity: 0.6;

  svg { width: 100%; height: 60px; }
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: $text-dark;
}

.welcome-desc {
  margin: 0 0 16px;
  font-size: 13px;
  color: $text-light;
  line-height: 1.6;
}

.welcome-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: $primary;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  svg { width: 16px; height: 16px; }

  &:hover {
    background: $primary-light;
    transform: translateX(4px);
  }
}

// 主内容
.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: $white;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba($text-dark, 0.04);
  border: 1px solid rgba($text-dark, 0.04);
  flex-wrap: wrap;
}

.sort-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  background: $bg-cream;
  border-radius: 14px;
}

.sort-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: transparent;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: $text-light;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  svg { width: 14px; height: 14px; }

  &:hover {
    color: $primary;
    background: rgba($primary, 0.08);
  }

  &--active {
    background: $white;
    color: $primary;
    box-shadow: 0 2px 12px rgba($text-dark, 0.08);
  }
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: $bg-cream;
  border: 1px solid transparent;
  border-radius: 12px;
  transition: all 0.25s ease;

  &:focus-within {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }

  .search-icon {
    width: 18px;
    height: 18px;
    color: $text-light;
    flex-shrink: 0;
  }

  .search-input {
    border: none;
    background: transparent;
    font-size: 14px;
    color: $text-dark;
    outline: none;
    width: 200px;

    &::placeholder { color: $text-light; }
  }

  .search-clear {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    padding: 0;
    background: $text-light;
    border: none;
    border-radius: 50%;
    color: $white;
    cursor: pointer;
    opacity: 0.6;
    transition: all 0.2s ease;

    svg { width: 10px; height: 10px; }

    &:hover { opacity: 1; background: $primary; }
  }
}

.search-btn {
  padding: 10px 20px;
  background: $primary;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  &:hover {
    background: $primary-light;
    transform: translateY(-1px);
  }
}

// 帖子列表
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  padding: 24px;
  background: $white;
  border-radius: 20px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 4px 20px rgba($text-dark, 0.04);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  animation: fadeIn 0.5s ease forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(12px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  &:hover {
    border-color: rgba($primary, 0.2);
    box-shadow: 0 12px 40px rgba($primary, 0.1);
    transform: translateY(-4px);

    .avatar-decoration {
      opacity: 1;
      transform: scale(1.1);
    }

    .topic-title {
      color: $primary;
    }
  }
}

.topic-avatar {
  position: relative;

  .avatar-img {
    border: 3px solid rgba($primary, 0.1);
    background: linear-gradient(135deg, $accent-light, $accent);
    color: $text-dark;
    font-weight: 700;
    font-size: 18px;
  }

  .avatar-decoration {
    position: absolute;
    inset: -4px;
    border: 2px dashed rgba($primary, 0.3);
    border-radius: 50%;
    opacity: 0;
    transition: all 0.35s ease;
  }
}

.topic-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.topic-header {
  .topic-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 11px;
    font-weight: 700;

    svg { width: 12px; height: 12px; }

    &--pin {
      background: linear-gradient(135deg, $accent-light, $accent);
      color: $text-dark;
    }

    &--hot {
      background: linear-gradient(135deg, #fecaca, #f87171);
      color: #991b1b;
    }

    &--board {
      background: rgba($primary, 0.1);
      color: $primary;
    }
  }

  .topic-title {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: $text-dark;
    line-height: 1.4;
    transition: color 0.25s ease;
  }
}

.topic-excerpt {
  margin: 0;
  font-size: 14px;
  color: $text-light;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: $text-light;

  span {
    display: flex;
    align-items: center;
    gap: 5px;

    svg { width: 14px; height: 14px; }
  }
}

.topic-book {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba($accent-light, 0.3), rgba($accent, 0.2));
  border-radius: 12px;
  margin-top: 4px;

  .book-cover {
    width: 36px;
    height: 48px;
    border-radius: 4px;
    overflow: hidden;
    background: $white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .book-img { width: 100%; height: 100%; object-fit: cover; }

  .book-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-light;

    svg { width: 18px; height: 18px; }
  }

  .book-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .book-label {
    font-size: 10px;
    color: $text-light;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .book-title {
    font-size: 13px;
    font-weight: 600;
    color: $text-dark;
  }
}

.topic-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
  min-width: 70px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: $bg-cream;
    border-radius: 10px;
    font-size: 13px;
    color: $text-light;
    transition: all 0.25s ease;

    svg { width: 16px; height: 16px; }

    &:hover {
      background: rgba($primary, 0.08);
      color: $primary;
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  background: $white;
  border-radius: 24px;
  border: 1px solid rgba($text-dark, 0.04);
  text-align: center;

  .empty-illustration {
    margin-bottom: 24px;
    color: $text-light;
    opacity: 0.5;

    svg { width: 160px; height: 120px; }
  }

  .empty-title {
    margin: 0 0 8px;
    font-size: 20px;
    font-weight: 700;
    color: $text-dark;
  }

  .empty-desc {
    margin: 0 0 24px;
    font-size: 14px;
    color: $text-light;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 28px;
    background: $primary;
    color: #fff;
    border: none;
    border-radius: 14px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;

    svg { width: 18px; height: 18px; }

    &:hover {
      background: $primary-light;
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba($primary, 0.3);
    }
  }
}

// 分页
.pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: $white;
  border-radius: 16px;
  border: 1px solid rgba($text-dark, 0.04);
}
</style>

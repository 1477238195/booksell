<template>
  <div class="topic-page">
    <!-- 背景装饰 -->
    <div class="bg-glow bg-glow--1"></div>
    <div class="bg-glow bg-glow--2"></div>

    <div class="page-container">
      <!-- 顶部导航 -->
      <nav class="top-nav">
        <button class="nav-back" @click="$router.push('/dashboard/forum/list')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回论坛
        </button>
        <div class="nav-breadcrumb">
          <span>论坛</span>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
          <span v-if="topic?.boardName">{{ topic.boardName }}</span>
          <svg v-if="topic?.boardName" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
          <span>帖子详情</span>
        </div>
      </nav>

      <div v-loading="loading" class="content-grid">
        <!-- 左侧主内容 -->
        <main class="main-area">
          <template v-if="topic">
            <!-- 帖子卡片 -->
            <article class="topic-card">
              <!-- 头部 -->
              <header class="card-header">
                <div class="header-badges">
                  <span v-if="topic.pinned === 1" class="badge badge--pin">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M12 2v20M17 7l-5-5-5 5"/>
                    </svg>
                    置顶
                  </span>
                  <span v-if="topic.boardName" class="badge badge--board">{{ topic.boardName }}</span>
                </div>
                <h1 class="topic-title">{{ topic.title }}</h1>

                <div class="header-meta">
                  <div class="author-info">
                    <el-avatar :size="48" :src="topic.authorAvatar" class="author-avatar">
                      {{ (topic.authorName || '书')[0] }}
                    </el-avatar>
                    <div class="author-detail">
                      <span class="author-name">{{ topic.authorName || '书友' }}</span>
                      <span class="author-time">{{ fmtTime(topic.createTime) }}</span>
                    </div>
                  </div>
                  <div class="topic-stats">
                    <span class="stat-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                      {{ topic.viewCount || 0 }}
                    </span>
                    <span class="stat-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                      </svg>
                      {{ replyTotal }}
                    </span>
                  </div>
                </div>
              </header>

              <!-- 正文 -->
              <div class="card-body">
                <div class="topic-content">{{ topic.content }}</div>

                <!-- 关联书籍 -->
                <div v-if="topic.bookTitle" class="book-card" @click="goBook(topic.bookId)">
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
                  <svg class="book-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M5 12h14M12 5l7 7-7 7"/>
                  </svg>
                </div>
              </div>

              <!-- 底部操作 -->
              <footer class="card-footer">
                <div class="action-buttons">
                  <button class="action-btn" @click="scrollToReply">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    回复
                  </button>
                  <button class="action-btn" @click="shareTopic">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
                      <polyline points="16 6 12 2 8 6"/>
                      <line x1="12" y1="2" x2="12" y2="15"/>
                    </svg>
                    分享
                  </button>
                </div>
                <div class="manage-buttons" v-if="canEditTopic">
                  <el-button type="danger" size="small" plain @click="confirmDeleteTopic">
                    删除帖子
                  </el-button>
                </div>
              </footer>
            </article>

            <!-- 回复区域 -->
            <section class="reply-section">
              <h2 class="section-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                回复 ({{ replyTotal }})
              </h2>

              <!-- 回复列表 -->
              <div v-if="replies.length" class="reply-list">
                <div
                  v-for="(reply, index) in replies"
                  :key="reply.replyId"
                  class="reply-item"
                  :style="{ '--delay': `${index * 0.05}s` }"
                >
                  <el-avatar :size="40" :src="reply.authorAvatar" class="reply-avatar">
                    {{ (reply.authorName || '书')[0] }}
                  </el-avatar>
                  <div class="reply-content">
                    <div class="reply-header">
                      <div class="reply-author">
                        <span class="author-name">{{ reply.authorName || '书友' }}</span>
                        <span v-if="reply.userId === topic.userId" class="author-tag">楼主</span>
                      </div>
                      <span class="reply-time">{{ fmtTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-body">{{ reply.content }}</div>
                    <div class="reply-actions" v-if="canDeleteReply(reply)">
                      <button class="reply-action" @click="confirmDeleteReply(reply.replyId)">
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="reply-empty">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <p>暂无回复，快来抢沙发吧</p>
              </div>

              <!-- 回复分页 -->
              <div v-if="replyTotal > replyQuery.size" class="reply-pagination">
                <el-pagination
                  v-model:current-page="replyQuery.page"
                  :page-size="replyQuery.size"
                  :total="replyTotal"
                  layout="prev, pager, next"
                  background
                  small
                  @current-change="loadDetail"
                />
              </div>
            </section>

            <!-- 回复编辑器 -->
            <section id="reply-editor" class="editor-section">
              <template v-if="userStore.token">
                <div class="editor-card">
                  <div class="editor-header">
                    <el-avatar :size="36" :src="userStore.userInfo?.avatar" class="editor-avatar">
                      {{ (userStore.userInfo?.username || '书')[0] }}
                    </el-avatar>
                    <span class="editor-hint">发表你的看法</span>
                  </div>
                  <textarea
                    v-model="replyContent"
                    class="editor-textarea"
                    placeholder="友善讨论，分享你的观点..."
                    rows="5"
                    maxlength="5000"
                  ></textarea>
                  <div class="editor-footer">
                    <span class="char-count">{{ replyContent.length }}/5000</span>
                    <el-button type="primary" :loading="replySubmitting" @click="submitReply">
                      发送回复
                    </el-button>
                  </div>
                </div>
              </template>
              <div v-else class="login-prompt">
                <p>登录后即可参与讨论</p>
                <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
              </div>
            </section>
          </template>

          <!-- 加载失败 -->
          <div v-else-if="!loading" class="not-found">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 8v4M12 16h.01"/>
            </svg>
            <h3>帖子不存在或已被删除</h3>
            <el-button type="primary" @click="$router.push('/dashboard/forum/list')">返回论坛</el-button>
          </div>
        </main>

        <!-- 右侧边栏 -->
        <aside class="sidebar-area">
          <!-- 作者信息 -->
          <div class="sidebar-card">
            <h3 class="card-title">关于作者</h3>
            <div class="author-profile" v-if="topic">
              <el-avatar :size="64" :src="topic.authorAvatar" class="profile-avatar">
                {{ (topic.authorName || '书')[0] }}
              </el-avatar>
              <span class="profile-name">{{ topic.authorName || '书友' }}</span>
            </div>
          </div>

          <!-- 相关操作 -->
          <div class="sidebar-card">
            <h3 class="card-title">相关操作</h3>
            <div class="action-list">
              <button class="sidebar-action" @click="$router.push('/dashboard/forum/list')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                  <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                返回首页
              </button>
              <button class="sidebar-action" @click="goPublish">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14M5 12h14"/>
                </svg>
                发布新帖
              </button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ForumApi } from '@/api/OtherApi'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const topic = ref(null)
const replies = ref([])
const replyTotal = ref(0)
const replyContent = ref('')
const replySubmitting = ref(false)

const replyQuery = reactive({
  page: 1,
  size: 20
})

const myId = computed(() => {
  const id = userStore.userInfo?.userId
  if (id === undefined || id === null || id === '') return null
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})

const canEditTopic = computed(() => {
  if (!topic.value || myId.value == null) return false
  return Number(topic.value.userId) === myId.value || Number(userStore.userInfo?.role) === 1
})

function canDeleteReply(r) {
  if (!r || myId.value == null) return false
  return Number(r.userId) === myId.value || Number(userStore.userInfo?.role) === 1
}

function fmtTime(t) {
  if (!t) return ''
  try {
    const d = new Date(t)
    return d.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return String(t)
  }
}

async function loadDetail() {
  const topicId = route.params.topicId
  if (!topicId) return
  loading.value = true
  try {
    const res = await ForumApi.topicDetail({
      topicId,
      page: replyQuery.page,
      size: replyQuery.size
    })
    if (res.status === 0 && res.data) {
      topic.value = res.data.topic || null
      replies.value = res.data.replies || []
      replyTotal.value = Number(res.data.replyTotal) || 0
    } else {
      topic.value = null
    }
  } catch {
    topic.value = null
  } finally {
    loading.value = false
  }
}

function scrollToReply() {
  const el = document.getElementById('reply-editor')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

async function submitReply() {
  const text = replyContent.value?.trim()
  if (!text) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replySubmitting.value = true
  try {
    const res = await ForumApi.addReply({
      topicId: Number(route.params.topicId),
      content: text,
      parentReplyId: null
    })
    if (res.status === 0) {
      ElMessage.success('回复成功')
      replyContent.value = ''
      replyQuery.page = Math.ceil((replyTotal.value + 1) / replyQuery.size) || 1
      await loadDetail()
    }
  } catch {
    // handled
  } finally {
    replySubmitting.value = false
  }
}

function shareTopic() {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  }
}

function goBook(bookId) {
  if (bookId) router.push({ name: 'BookDetail', params: { bookId: String(bookId) } })
}

function goPublish() {
  router.push({ name: 'ForumPublish' })
}

async function confirmDeleteTopic() {
  try {
    await ElMessageBox.confirm('确定删除该帖子吗？', '确认删除', { type: 'warning' })
    const res = await ForumApi.deleteTopic(Number(route.params.topicId))
    if (res.status === 0) {
      ElMessage.success('已删除')
      router.push({ name: 'ForumList' })
    }
  } catch {}
}

async function confirmDeleteReply(replyId) {
  try {
    await ElMessageBox.confirm('确定删除该回复吗？', '确认删除', { type: 'warning' })
    const res = await ForumApi.deleteReply(replyId)
    if (res.status === 0) {
      ElMessage.success('已删除')
      await loadDetail()
    }
  } catch {}
}

watch(() => route.params.topicId, () => {
  replyQuery.page = 1
  loadDetail()
})

loadDetail()
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
.bg-glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  z-index: 0;

  &--1 {
    width: 500px;
    height: 500px;
    top: -100px;
    right: -100px;
    background: radial-gradient(circle, rgba($primary, 0.1) 0%, transparent 70%);
  }

  &--2 {
    width: 400px;
    height: 400px;
    bottom: 0;
    left: -100px;
    background: radial-gradient(circle, rgba($accent, 0.12) 0%, transparent 70%);
  }
}

.topic-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(160deg, $bg-cream 0%, $bg-warm 50%, darken($bg-cream, 3%) 100%);
  z-index: 1;
  padding: 24px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

// 顶部导航
.top-nav {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;

  .nav-back {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: $white;
    border: 1px solid rgba($text-dark, 0.08);
    border-radius: 14px;
    font-size: 14px;
    font-weight: 600;
    color: $text-dark;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;
    box-shadow: 0 4px 16px rgba($text-dark, 0.04);

    svg { width: 18px; height: 18px; }

    &:hover {
      background: $primary;
      color: #fff;
      border-color: $primary;
      transform: translateX(-4px);
    }
  }

  .nav-breadcrumb {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: $text-light;

    svg { width: 14px; height: 14px; }

    span:last-child { color: $text-dark; font-weight: 600; }
  }
}

// 内容网格
.content-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    .sidebar-area { display: none; }
  }
}

.main-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 帖子卡片
.topic-card {
  background: $white;
  border-radius: 24px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 8px 32px rgba($text-dark, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 28px 28px 24px;
  border-bottom: 1px solid rgba($text-dark, 0.06);

  .header-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 14px;
  }

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 5px 12px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 700;

    svg { width: 14px; height: 14px; }

    &--pin {
      background: linear-gradient(135deg, $accent-light, $accent);
      color: $text-dark;
    }

    &--board {
      background: rgba($primary, 0.1);
      color: $primary;
    }
  }

  .topic-title {
    margin: 0 0 20px;
    font-size: 1.75rem;
    font-weight: 800;
    color: $text-dark;
    line-height: 1.35;
  }

  .header-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 14px;

    .author-avatar {
      border: 3px solid rgba($primary, 0.15);
      background: linear-gradient(135deg, $accent-light, $accent);
      color: $text-dark;
      font-weight: 700;
      font-size: 16px;
    }

    .author-detail {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .author-name {
      font-size: 15px;
      font-weight: 700;
      color: $text-dark;
    }

    .author-time {
      font-size: 12px;
      color: $text-light;
    }
  }

  .topic-stats {
    display: flex;
    gap: 10px;

    .stat-chip {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: $bg-cream;
      border-radius: 20px;
      font-size: 13px;
      color: $text-light;

      svg { width: 16px; height: 16px; }
    }
  }
}

.card-body {
  padding: 28px;

  .topic-content {
    white-space: pre-wrap;
    word-break: break-word;
    font-size: 16px;
    line-height: 2;
    color: $text-dark;
  }
}

// 书籍卡片
.book-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 16px;
  background: linear-gradient(135deg, rgba($accent-light, 0.4), rgba($accent, 0.3));
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba($accent, 0.25);
  }

  .book-cover {
    width: 56px;
    height: 72px;
    border-radius: 8px;
    overflow: hidden;
    background: $white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .book-img { width: 100%; height: 100%; object-fit: cover; }

  .book-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-light;

    svg { width: 24px; height: 24px; }
  }

  .book-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .book-label {
    font-size: 11px;
    color: $text-light;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .book-title {
    font-size: 15px;
    font-weight: 700;
    color: $text-dark;
  }

  .book-arrow {
    width: 24px;
    height: 24px;
    color: $text-light;
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 28px 24px;

  .action-buttons {
    display: flex;
    gap: 10px;
  }

  .action-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: $bg-cream;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    color: $text-medium;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;

    svg { width: 18px; height: 18px; }

    &:hover {
      background: $primary;
      color: #fff;
    }
  }
}

// 回复区域
.reply-section {
  background: $white;
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 4px 20px rgba($text-dark, 0.04);

  .section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 700;
    color: $text-dark;

    svg { width: 22px; height: 22px; color: $primary; }
  }
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: $bg-cream;
  border-radius: 16px;
  animation: fadeIn 0.4s ease forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;

  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(8px); }
    to { opacity: 1; transform: translateY(0); }
  }

  .reply-avatar {
    flex-shrink: 0;
    border: 2px solid rgba($primary, 0.15);
    background: linear-gradient(135deg, $accent-light, $accent);
    color: $text-dark;
    font-weight: 700;
  }

  .reply-content {
    flex: 1;
    min-width: 0;
  }

  .reply-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .reply-author {
    display: flex;
    align-items: center;
    gap: 10px;

    .author-name {
      font-size: 14px;
      font-weight: 700;
      color: $text-dark;
    }

    .author-tag {
      padding: 3px 8px;
      background: $primary;
      color: #fff;
      font-size: 10px;
      font-weight: 700;
      border-radius: 6px;
    }
  }

  .reply-time {
    font-size: 12px;
    color: $text-light;
  }

  .reply-body {
    font-size: 14px;
    line-height: 1.75;
    color: $text-medium;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .reply-actions {
    margin-top: 10px;
  }

  .reply-action {
    padding: 4px 10px;
    background: transparent;
    border: none;
    border-radius: 6px;
    font-size: 12px;
    color: #dc2626;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover { background: rgba(#dc2626, 0.1); }
  }
}

.reply-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: $text-light;
  text-align: center;

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: 0.4;
  }

  p { margin: 0; font-size: 14px; }
}

.reply-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

// 回复编辑器
.editor-section {
  background: $white;
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 4px 20px rgba($text-dark, 0.04);
}

.editor-card {
  .editor-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 14px;
  }

  .editor-avatar {
    border: 2px solid rgba($primary, 0.15);
    background: linear-gradient(135deg, $accent-light, $accent);
    color: $text-dark;
    font-weight: 700;
  }

  .editor-hint {
    font-size: 14px;
    color: $text-light;
  }

  .editor-textarea {
    width: 100%;
    padding: 16px;
    border: 1px solid rgba($text-dark, 0.1);
    border-radius: 14px;
    font-size: 14px;
    line-height: 1.75;
    color: $text-dark;
    resize: vertical;
    min-height: 120px;
    transition: all 0.25s ease;
    font-family: inherit;

    &::placeholder { color: $text-light; }

    &:focus {
      outline: none;
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba($primary, 0.1);
    }
  }

  .editor-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 14px;

    .char-count {
      font-size: 12px;
      color: $text-light;
    }
  }
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px;
  background: $bg-cream;
  border-radius: 16px;
  text-align: center;

  p { margin: 0 0 16px; font-size: 14px; color: $text-medium; }
}

// 侧边栏
.sidebar-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: $white;
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 4px 20px rgba($text-dark, 0.04);

  .card-title {
    margin: 0 0 16px;
    font-size: 14px;
    font-weight: 700;
    color: $text-dark;
  }
}

.author-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;

  .profile-avatar {
    width: 64px;
    height: 64px;
    border: 3px solid rgba($primary, 0.15);
    background: linear-gradient(135deg, $accent-light, $accent);
    color: $text-dark;
    font-weight: 700;
    font-size: 20px;
  }

  .profile-name {
    font-size: 15px;
    font-weight: 700;
    color: $text-dark;
  }
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-action {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: $bg-cream;
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  color: $text-medium;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  svg { width: 18px; height: 18px; }

  &:hover {
    background: $primary;
    color: #fff;
    transform: translateX(4px);
  }
}

// 404 状态
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 40px;
  background: $white;
  border-radius: 24px;
  text-align: center;

  svg {
    width: 64px;
    height: 64px;
    color: $text-light;
    opacity: 0.5;
    margin-bottom: 16px;
  }

  h3 {
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 700;
    color: $text-dark;
  }
}
</style>

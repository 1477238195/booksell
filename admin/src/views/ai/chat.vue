<template>
  <div class="study">
    <!-- ====== 左侧边栏：主题 & 历史 ====== -->
    <aside class="sidebar" :class="{ 'sidebar--collapsed': sidebarCollapsed }">
      <div class="sidebar__inner">
        <!-- 品牌 -->
        <div class="sidebar__brand">
          <div class="sidebar__logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <line x1="8" y1="7" x2="16" y2="7"/>
              <line x1="8" y1="11" x2="13" y2="11"/>
            </svg>
          </div>
          <div class="sidebar__brand-text">
            <span class="sidebar__title">阅谈</span>
            <span class="sidebar__subtitle">AI 书伴</span>
          </div>
        </div>

        <!-- 话题分类 -->
        <section class="sidebar__section">
          <h3 class="sidebar__label">探索话题</h3>
          <div class="topic-list">
            <button
              v-for="t in topics"
              :key="t.label"
              class="topic-chip"
              :class="{ 'topic-chip--active': activeTopic === t.label }"
              @click="selectTopic(t)"
            >
              <span class="topic-chip__icon">{{ t.icon }}</span>
              <span class="topic-chip__text">{{ t.label }}</span>
              <span class="topic-chip__count">{{ t.count }}</span>
            </button>
          </div>
        </section>

        <!-- 快捷操作 -->
        <section class="sidebar__section">
          <h3 class="sidebar__label">快捷操作</h3>
          <div class="action-list">
            <button class="action-btn" @click="handleAction('recommend')">
              <span class="action-btn__icon">📚</span>
              <span>推荐好书</span>
            </button>
            <button class="action-btn" @click="handleAction('discuss')">
              <span class="action-btn__icon">💬</span>
              <span>深度讨论</span>
            </button>
            <button class="action-btn" @click="handleAction('summary')">
              <span class="action-btn__icon">📝</span>
              <span>书籍摘要</span>
            </button>
          </div>
        </section>

        <!-- 历史会话 -->
        <section class="sidebar__section">
          <h3 class="sidebar__label">历史记录</h3>
          <div class="session-list">
            <div v-if="sessionHistory.length === 0" class="session-empty">暂无记录</div>
            <div
              v-for="s in sessionHistory"
              :key="s.id"
              class="session-item"
              :class="{ 'session-item--active': s.id === currentSessionId }"
              @click="switchSession(s.id)"
            >
              <span class="session-item__preview">{{ decodeUrlTitle(s.title) || '新对话' }}</span>
              <span class="session-item__time">{{ formatSessionTime(s.updatedAt) }}</span>
            </div>
          </div>
        </section>

        <!-- 底部用户区 -->
        <div class="sidebar__footer">
          <div class="sidebar__user">
            <span class="sidebar__user-avatar">U</span>
            <span class="sidebar__user-name">读者</span>
          </div>
          <button class="sidebar__toggle" @click="sidebarCollapsed = !sidebarCollapsed" title="收起侧栏">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline :points="sidebarCollapsed ? '9 18 15 12 9 6' : '15 18 9 12 15 6'"/>
            </svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- ====== 主对话区 ====== -->
    <main class="main">
      <!-- 顶栏 -->
      <div class="main__topbar">
        <div class="main__status">
          <button class="topbar-btn" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开侧栏' : '收起侧栏'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="3" x2="9" y2="21"/>
            </svg>
          </button>
          <span class="main__status-dot"></span>
          <span>AI 书伴在线</span>
        </div>
        <div class="main__actions">
          <button class="topbar-btn" @click="startNewChat" title="新对话">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          </button>
          <button class="topbar-btn" @click="clearMessages" :disabled="!messages.length" title="清空对话">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </div>

      <!-- 消息流 -->
      <div class="messages" ref="messagesContainer">
        <!-- 空态 -->
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-state__ornament">❦</div>
          <h2 class="empty-state__title">今天想探索什么？</h2>
          <p class="empty-state__desc">
            我是你的阅读伙伴 — 可以推荐好书、陪你讨论情节、解读深意。<br>
            如同坐在旧书房里聊一本书，随意、深入、有趣。
          </p>
          <div class="empty-state__grid">
            <button
              v-for="s in seedQuestions"
              :key="s.text"
              class="seed-card"
              @click="sendSeed(s)"
            >
              <span class="seed-card__icon">{{ s.icon }}</span>
              <span class="seed-card__text">{{ s.text }}</span>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="msg"
          :class="msg.isUser ? 'msg--user' : 'msg--ai'"
        >
          <div class="msg__gutter">
            <span v-if="!msg.isUser" class="msg__badge">AI</span>
            <span v-else class="msg__badge msg__badge--user">You</span>
          </div>
          <div class="msg__content">
            <div class="msg__body">
              <div v-if="!msg.isUser" class="msg__markdown" v-html="renderMarkdown(msg.content)"></div>
              <template v-else>{{ msg.content }}</template>
            </div>
            <div class="msg__meta">
              <span class="msg__time">{{ msg.time }}</span>
              <button v-if="!msg.isUser" class="msg__copy" @click="copyText(msg.content)">复制</button>
              <button v-if="msg.error" class="msg__retry" @click="retryMessage(index)">重试</button>
            </div>
          </div>
        </div>

        <!-- 加载 -->
        <div v-if="loading" class="msg msg--ai">
          <div class="msg__gutter"><span class="msg__badge msg__badge--thinking">...</span></div>
          <div class="msg__content">
            <div class="msg__body msg__body--loading">
              <span class="loading-cursor"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <div class="input-area__inner">
          <textarea
            ref="inputRef"
            v-model="inputText"
            class="input-area__field"
            placeholder="说点什么..."
            maxlength="1000"
            :disabled="loading"
            @keydown.enter.exact.prevent="handleSend"
            @input="autoResize"
            rows="1"
          ></textarea>
          <div class="input-area__tools">
            <span class="input-area__count">{{ inputText.length }}</span>
            <button
              class="input-area__send"
              :class="{ 'input-area__send--ready': inputText.trim() && !loading }"
              :disabled="!inputText.trim() || loading"
              @click="handleSend"
            >
              发送
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- ====== 右侧上下文面板 ====== -->
    <aside class="context" :class="{ 'context--collapsed': contextCollapsed }">
      <div class="context__inner">
        <div class="context__header">
          <h3>阅读灵感</h3>
          <button class="context__toggle" @click="contextCollapsed = !contextCollapsed" title="收起面板">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline :points="contextCollapsed ? '6 9 12 15 18 9' : '18 15 12 9 6 15'"/>
            </svg>
          </button>
        </div>

        <!-- 推荐书籍卡片 -->
        <div class="context__cards">
          <div v-for="book in recommendedBooks" :key="book.title" class="book-card">
            <div class="book-card__cover">
              <span class="book-card__cover-text">{{ book.title.charAt(0) }}</span>
            </div>
            <div class="book-card__info">
              <h4 class="book-card__title">{{ book.title }}</h4>
              <p class="book-card__author">{{ book.author }}</p>
              <p class="book-card__note">{{ book.note }}</p>
            </div>
          </div>
        </div>

        <!-- 快捷提问 -->
        <div class="quick-prompts">
          <h4 class="context__label">试试问</h4>
          <button
            v-for="q in quickQuestions"
            :key="q.text"
            class="prompt-chip"
            @click="handleQuick(q)"
            :disabled="loading"
          >
            <span class="prompt-chip__icon">{{ q.icon }}</span>
            {{ q.text }}
          </button>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ChatApi from '@/api/ChatApi'

/* ---- state ---- */
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)
const sidebarCollapsed = ref(false)
const contextCollapsed = ref(false)
const activeTopic = ref(null)
const currentSessionId = ref(null)
const sessionList = ref([])

/* ---- 会话管理 ---- */
async function loadSessions() {
  const res = await ChatApi.getSessions()
  if (res.status === 0) {
    sessionList.value = res.data || []
  }
}

async function createSession() {
  const res = await ChatApi.createSession('新对话')
  if (res.status === 0 && res.data) {
    currentSessionId.value = res.data.id
    messages.value = []
    await loadSessions()
    return res.data
  }
  return null
}

async function loadSession(sessionId) {
  const res = await ChatApi.getSession(sessionId)
  if (res.status === 0 && res.data) {
    messages.value = (res.data.messages || []).map(m => ({
      isUser: m.isUser,
      content: m.content,
      time: new Date(m.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }))
  }
}

async function startNewChat() {
  currentSessionId.value = null
  messages.value = []
}

async function switchSession(id) {
  if (currentSessionId.value === id) return
  currentSessionId.value = id
  await loadSession(id)
}

async function clearMessages() {
  if (currentSessionId.value) {
    await ChatApi.deleteSession(currentSessionId.value)
    currentSessionId.value = null
    messages.value = []
    await loadSessions()
  }
}

const sessionHistory = computed(() => sessionList.value)

/* ---- 数据 ---- */
const topics = [
  { label: '文学小说', icon: '📖', count: 24, prompt: '推荐几本引人深思的文学小说' },
  { label: '科幻奇幻', icon: '🚀', count: 18, prompt: '推荐经典的科幻或奇幻作品' },
  { label: '历史人文', icon: '🏛️', count: 15, prompt: '推荐好看的历史或人文书籍' },
  { label: '科学自然', icon: '🔬', count: 12, prompt: '推荐科学或自然主题的好书' },
  { label: '哲学思想', icon: '💭', count: 9, prompt: '推荐哲学入门读物' },
  { label: '商业经济', icon: '📊', count: 14, prompt: '推荐商业或经济类好书' }
]

const seedQuestions = [
  { icon: '📚', text: '推荐几本让我欲罢不能的悬疑小说' },
  { icon: '🌍', text: '聊聊《百年孤独》为什么是经典' },
  { icon: '💡', text: '适合零基础学 Python 的书有哪些' },
  { icon: '🔍', text: '分析一下《活着》中福贵这个人物' },
  { icon: '🎨', text: '推荐能提升写作能力的书' },
  { icon: '🔥', text: '2024年最值得读的新书有哪些' }
]

const quickQuestions = [
  { text: '推荐科幻', icon: '🚀', action: 'recommend', params: '推荐经典科幻小说' },
  { text: '聊聊三体', icon: '🌌', action: 'discuss', params: { bookName: '三体', topic: '黑暗森林法则' } },
  { text: '编程入门', icon: '💻', action: 'recommend', params: '适合编程入门的好书' },
  { text: '解读活着', icon: '📖', action: 'discuss', params: { bookName: '活着', topic: '福贵的人生哲学' } }
]

const recommendedBooks = [
  { title: '三体', author: '刘慈欣', note: '科幻迷绕不开的史诗' },
  { title: '百年孤独', author: '马尔克斯', note: '魔幻现实主义的巅峰' },
  { title: '苏菲的世界', author: '乔斯坦·贾德', note: '哲学入门的最好方式' }
]

/* ---- helpers ---- */
function formatTime(d) {
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function decodeUrlTitle(title) {
  if (!title) return title
  try { return decodeURIComponent(title) } catch { return title }
}

function formatSessionTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 140) + 'px'
}

async function scrollToBottom() {
  await nextTick()
  const el = messagesContainer.value
  if (el) el.scrollTop = el.scrollHeight
}

/* ---- actions ---- */
function selectTopic(t) {
  activeTopic.value = t.label
  inputText.value = t.prompt
  handleSend()
}

function sendSeed(s) {
  inputText.value = s.text
  handleSend()
}

async function handleAction(type) {
  if (loading.value) return
  if (type === 'recommend') {
    inputText.value = '推荐几本适合我的好书'
  } else if (type === 'discuss') {
    inputText.value = '我想深入讨论最近读过的一本书'
  } else {
    inputText.value = '帮我总结一下《三体》的核心思想'
  }
  await handleSend()
}

async function handleSend() {
  const content = inputText.value.trim()
  if (!content || loading.value) return
  
  // 确保有会话（首次发消息时用消息内容作为标题）
  if (!currentSessionId.value) {
    const res = await ChatApi.createSession(content)
    if (res.status !== 0 || !res.data) {
      ElMessage.error('创建会话失败')
      return
    }
    currentSessionId.value = res.data.id
    await loadSessions()
  }
  
  messages.value.push({ isUser: true, content, time: formatTime(new Date()) })
  inputText.value = ''
  loading.value = true
  if (inputRef.value) inputRef.value.style.height = 'auto'
  await scrollToBottom()
  try {
    const res = await ChatApi.sendMessage(currentSessionId.value, content)
    if (res.status === 0 && res.data) {
      messages.value.push({ isUser: false, content: res.data.content, time: formatTime(new Date(res.data.createdAt)) })
      await loadSessions()
    } else {
      messages.value.push({ isUser: false, content: res.msg || '请求失败', time: formatTime(new Date()), error: true })
    }
  } catch {
    messages.value.push({ isUser: false, content: '请求失败，请重试。', time: formatTime(new Date()), error: true })
  } finally {
    loading.value = false
    await scrollToBottom()
    inputRef.value?.focus()
  }
}

async function handleQuick(q) {
  if (loading.value) return
  // 首次发消息时用消息内容作为标题
  if (!currentSessionId.value) {
    const res = await ChatApi.createSession(q.text)
    if (res.status !== 0 || !res.data) {
      ElMessage.error('创建会话失败')
      return
    }
    currentSessionId.value = res.data.id
    await loadSessions()
  }
  messages.value.push({ isUser: true, content: q.text, time: formatTime(new Date()) })
  loading.value = true
  await scrollToBottom()
  try {
    let res
    if (q.action === 'recommend') res = await ChatApi.sendMessage(currentSessionId.value, q.params)
    else if (q.action === 'discuss') res = await ChatApi.sendMessage(currentSessionId.value, `讨论《${q.params.bookName}》：${q.params.topic}`)
    else res = await ChatApi.sendMessage(currentSessionId.value, q.params)
    if (res.status === 0 && res.data) {
      messages.value.push({ isUser: false, content: res.data.content, time: formatTime(new Date(res.data.createdAt)) })
      await loadSessions()
    } else {
      messages.value.push({ isUser: false, content: res.msg || '请求失败', time: formatTime(new Date()), error: true })
    }
  } catch {
    messages.value.push({ isUser: false, content: '请求失败，请重试。', time: formatTime(new Date()), error: true })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function retryMessage(index) {
  let userContent = ''
  for (let i = index - 1; i >= 0; i--) {
    if (messages.value[i].isUser) { userContent = messages.value[i].content; break }
  }
  if (!userContent) return
  messages.value.splice(index, 1)
  loading.value = true
  await scrollToBottom()
  try {
    const res = await DeepSeekApi.chat(userContent)
    messages.value.push({ isUser: false, content: res.data ?? res.msg ?? '…', time: formatTime(new Date()) })
  } catch {
    messages.value.push({ isUser: false, content: '请求失败，请重试。', time: formatTime(new Date()), error: true })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

/* ---- markdown ---- */
const ESC = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }
function renderMarkdown(raw) {
  let h = raw.replace(/[&<>"']/g, c => ESC[c])
  h = h.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => `<pre><code>${code.trim()}</code></pre>`)
  h = h.replace(/`([^`]+)`/g, '<code>$1</code>')
  h = h.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  h = h.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  h = h.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  h = h.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  h = h.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  h = h.replace(/^[•-] (.+)$/gm, '<li>$1</li>')
  h = h.replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>')
  h = h.replace(/((?:<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>')
  h = h.replace(/^---$/gm, '<hr>')
  h = h.replace(/\n\n/g, '</p><p>')
  h = h.replace(/\n/g, '<br>')
  return '<p>' + h + '</p>'
}

/* ---- 复制 ---- */
async function copyText(text) {
  try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') }
  catch {
    const ta = document.createElement('textarea')
    ta.value = text; ta.style.cssText = 'position:fixed;opacity:0'; document.body.appendChild(ta)
    ta.select(); document.execCommand('copy'); document.body.removeChild(ta)
    ElMessage.success('已复制')
  }
}

/* ---- init ---- */
onMounted(async () => {
  await loadSessions()
  inputRef.value?.focus()
})
</script>

<style>
/* 全局样式重置，确保页面不产生多余滚动 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden !important;
  box-sizing: border-box;
}

* {
  box-sizing: inherit;
}

:root {
  --ink: #2c1810;
  --ink-dim: #6b5e4f;
  --ink-muted: #a89880;
  --bg-root: #faf7f2;
  --bg-panel: #fffefb;
  --bg-surface: #f5f0e8;
  --bg-elevated: #ede6da;
  --border: #e8e0d5;
  --border-light: #ddd4c6;
  --gold: #b45309;
  --gold-dim: #d97706;
  --gold-glow: rgba(180, 83, 9, 0.08);
  --user-accent: #f0e4d0;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
}
</style>

<style scoped>
/* ==========================================
   Design Tokens — Dark Academia
   ========================================== */
.study {
  --ink: #2c1810;
  --ink-dim: #6b5e4f;
  --ink-muted: #a89880;
  --bg-root: #faf7f2;
  --bg-panel: #fffefb;
  --bg-surface: #f5f0e8;
  --bg-elevated: #ede6da;
  --border: #e8e0d5;
  --border-light: #ddd4c6;
  --gold: #b45309;
  --gold-dim: #d97706;
  --gold-glow: rgba(180, 83, 9, 0.08);
  --user-accent: #f0e4d0;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;

  display: grid;
  grid-template-columns: 220px 1fr 240px;
  height: calc(100vh - 56px);
  min-height: calc(100vh - 56px);
  max-height: calc(100vh - 56px);
  overflow: hidden;
  background: var(--bg-root);
  color: var(--ink);
  font-family: 'PingFang SC', 'Noto Serif SC', 'Noto Sans SC', 'Microsoft YaHei', serif;
  -webkit-font-smoothing: antialiased;
  box-sizing: border-box;
  position: relative;
  margin: 0;
  padding: 0;
}

/* ==========================================
   左侧边栏
   ========================================== */
.sidebar {
  background: var(--bg-panel);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  flex-shrink: 0;
}

.sidebar--collapsed {
  width: 0;
  min-width: 0;
  opacity: 0;
  pointer-events: none;
  border-right: none;
}

.sidebar__inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px 14px;
  overflow-y: auto;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.sidebar__logo {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: linear-gradient(140deg, var(--gold-dim), var(--gold));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gold);
  flex-shrink: 0;
}

.sidebar__logo svg { width: 18px; height: 18px; }

.sidebar__brand-text {
  display: flex;
  flex-direction: column;
}

.sidebar__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: 1px;
}

.sidebar__subtitle {
  font-size: 12px;
  color: var(--ink-muted);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.sidebar__section {
  margin-bottom: 16px;
}

.sidebar__label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: var(--ink-muted);
  margin: 0 0 12px;
  font-weight: 600;
}

/* 话题列表 */
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.topic-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--ink-dim);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  font-family: inherit;
}

.topic-chip:hover {
  background: var(--bg-elevated);
  border-color: var(--border-light);
  color: var(--ink);
}

.topic-chip--active {
  background: linear-gradient(140deg, rgba(201, 169, 110, 0.15), rgba(201, 169, 110, 0.08));
  border-color: var(--gold-dim);
  color: var(--gold);
  box-shadow: 0 2px 8px rgba(201, 169, 110, 0.12);
}

.topic-chip__icon { font-size: 16px; width: 22px; text-align: center; }
.topic-chip__text { flex: 1; text-align: left; }
.topic-chip__count {
  font-size: 11px;
  color: var(--ink-muted);
  background: var(--bg-surface);
  padding: 2px 8px;
  border-radius: 10px;
}

/* 快捷操作 */
.action-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--ink-dim);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  font-family: inherit;
}

.action-btn:hover {
  background: var(--bg-surface);
  border-color: var(--border-light);
  color: var(--ink);
}

.action-btn__icon { font-size: 16px; }

/* 历史会话列表 */
.session-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-empty {
  font-size: 12px;
  color: var(--ink-muted);
  text-align: center;
  padding: 16px 0;
}

.session-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--ink-dim);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}

.session-item:hover {
  background: var(--bg-elevated);
  border-color: var(--border-light);
  color: var(--ink);
}

.session-item--active {
  background: var(--gold-glow);
  border-color: var(--gold);
  color: var(--ink);
}

.session-item__preview {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item__time {
  font-size: 10px;
  color: var(--ink-muted);
}

.sidebar__footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar__user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--gold);
  font-weight: 600;
}

.sidebar__user-name { font-size: 13px; color: var(--ink-dim); }

.sidebar__toggle {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border);
  border-radius: 50%;
  background: transparent;
  color: var(--ink-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.sidebar__toggle:hover { color: var(--gold); border-color: var(--gold-dim); }

/* ==========================================
   主对话区
   ========================================== */
.main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background:
    radial-gradient(ellipse 60% 50% at 50% 40%, rgba(201, 169, 110, 0.03) 0%, transparent 60%),
    var(--bg-root);
  min-height: 0;
}

.main__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.main__status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-dim);
}

.main__status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #7d9a6e;
  box-shadow: 0 0 6px rgba(125, 154, 110, 0.4);
}

.topbar-btn {
  width: 34px;
  height: 34px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--ink-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.topbar-btn:hover:not(:disabled) { color: #c97a6e; border-color: #c97a6e; }
.topbar-btn:disabled { opacity: 0.3; cursor: not-allowed; }

/* 消息流 */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.messages::-webkit-scrollbar { width: 4px; }
.messages::-webkit-scrollbar-track { background: transparent; }
.messages::-webkit-scrollbar-thumb { background: var(--border-light); border-radius: 4px; }

/* 空态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 10px 10px;
  text-align: center;
  min-height: 0;
}

.empty-state__ornament {
  font-size: 28px;
  color: var(--gold-dim);
  margin-bottom: 10px;
  opacity: 0.6;
}

.empty-state__title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ink);
  margin: 0 0 6px;
  letter-spacing: 0.5px;
}

.empty-state__desc {
  font-size: 13px;
  color: var(--ink-dim);
  line-height: 1.6;
  max-width: 400px;
  margin: 0 0 16px;
}

.empty-state__grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  max-width: 560px;
  width: 100%;
}

.seed-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--ink-dim);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  font-family: inherit;
  text-align: left;
}

.seed-card:hover {
  background: var(--bg-elevated);
  border-color: var(--border-light);
  color: var(--ink);
  transform: translateY(-1px);
}

.seed-card__icon { font-size: 20px; flex-shrink: 0; }
.seed-card__text { line-height: 1.5; }

/* 消息条目 */
.msg {
  display: flex;
  gap: 16px;
  animation: msgIn 0.5s cubic-bezier(0.32, 0.72, 0, 1) both;
}

@keyframes msgIn {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0); }
}

.msg--ai { padding-right: 60px; }
.msg--user { flex-direction: row-reverse; padding-left: 80px; }

.msg__gutter { flex-shrink: 0; }

.msg__badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  background: var(--bg-elevated);
  color: var(--gold-dim);
  border: 1px solid var(--border);
}

.msg__badge--user {
  background: var(--user-accent);
  color: #92400e;
  border-color: rgba(180, 83, 9, 0.2);
}

.msg__badge--thinking {
  background: transparent;
  border: 1px dashed var(--border-light);
  color: var(--ink-muted);
  animation: badgeBlink 1.2s ease-in-out infinite;
}

@keyframes badgeBlink {
  0%, 100% { opacity: 0.4; }
  50%      { opacity: 1; }
}

.msg__content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.msg--user .msg__content { align-items: flex-end; }

.msg__body {
  font-size: 15px;
  line-height: 1.85;
  color: var(--ink);
}

.msg--user .msg__body {
  background: var(--user-accent);
  padding: 12px 18px;
  border-radius: var(--radius-md);
  color: var(--ink);
}

.msg__body--loading {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.loading-cursor {
  display: inline-block;
  width: 8px;
  height: 16px;
  background: var(--gold-dim);
  animation: cursorBlink 0.8s step-end infinite;
  border-radius: 1px;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0; }
}

.msg__meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.msg__time { font-size: 11px; color: var(--ink-muted); }

.msg__copy,
.msg__retry {
  font-size: 11px;
  color: var(--ink-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  font-family: inherit;
  transition: color 0.2s;
}

.msg__copy:hover { color: var(--gold); }
.msg__retry:hover { color: #c97a6e; }

/* Markdown */
.msg__markdown :deep(p) { margin: 0 0 10px; }
.msg__markdown :deep(p:last-child) { margin-bottom: 0; }
.msg__markdown :deep(strong) { color: var(--gold); font-weight: 700; }
.msg__markdown :deep(em) { color: var(--ink-dim); }
.msg__markdown :deep(code) {
  background: var(--bg-surface);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.88em;
  color: #c9a96e;
}
.msg__markdown :deep(pre) {
  background: var(--bg-surface);
  padding: 14px 18px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: 10px 0;
  border: 1px solid var(--border);
}
.msg__markdown :deep(pre code) { background: none; padding: 0; color: var(--ink-dim); }
.msg__markdown :deep(ul), .msg__markdown :deep(ol) { margin: 8px 0; padding-left: 20px; }
.msg__markdown :deep(li) { margin-bottom: 4px; }
.msg__markdown :deep(h2), .msg__markdown :deep(h3), .msg__markdown :deep(h4) {
  margin: 14px 0 8px;
  color: var(--ink);
  font-weight: 700;
}
.msg__markdown :deep(hr) { border: none; border-top: 1px solid var(--border); margin: 14px 0; }

/* ==========================================
   输入区
   ========================================== */
.input-area {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--border);
}

.input-area__inner {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 12px 14px 10px;
  transition: border-color 0.35s cubic-bezier(0.32, 0.72, 0, 1);
}

.input-area__inner:focus-within {
  border-color: var(--gold-dim);
  box-shadow: 0 0 0 3px var(--gold-glow);
}

.input-area__field {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--ink);
  font-size: 15px;
  line-height: 1.7;
  resize: none;
  padding: 0;
  font-family: inherit;
  min-height: 26px;
  max-height: 140px;
}

.input-area__field::placeholder { color: var(--ink-muted); }
.input-area__field:focus { outline: none; }
.input-area__field:disabled { opacity: 0.4; }

.input-area__tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.input-area__count {
  font-size: 11px;
  color: var(--ink-muted);
  font-variant-numeric: tabular-nums;
  margin-right: auto;
}

.input-area__send {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  color: var(--ink-muted);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  font-weight: 600;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
}

.input-area__send--ready {
  background: var(--gold-dim);
  border-color: var(--gold-dim);
  color: #fff;
}

.input-area__send--ready:hover {
  background: var(--gold);
  border-color: var(--gold);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px var(--gold-glow);
}

.input-area__send:disabled { opacity: 0.3; cursor: not-allowed; }

/* ==========================================
   右侧面板
   ========================================== */
.context {
  background: var(--bg-panel);
  border-left: 1px solid var(--border);
  overflow-y: auto;
  transition: all 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  flex-shrink: 0;
}

.context--collapsed {
  width: 0;
  min-width: 0;
  opacity: 0;
  pointer-events: none;
  border-left: none;
}

.context__inner {
  padding: 16px 14px;
}

.context__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.context__header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: 0.5px;
}

.context__toggle {
  width: 28px;
  height: 28px;
  border: 1px solid var(--border);
  border-radius: 50%;
  background: transparent;
  color: var(--ink-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.context__toggle:hover { color: var(--gold); border-color: var(--gold-dim); }

/* 推荐书卡 */
.context__cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.book-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(140deg, rgba(201, 169, 110, 0.06), rgba(201, 169, 110, 0.03));
  border: 1px solid rgba(201, 169, 110, 0.2);
  border-radius: var(--radius-md);
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}

.book-card:hover {
  border-color: var(--gold-dim);
  background: linear-gradient(140deg, rgba(201, 169, 110, 0.1), rgba(201, 169, 110, 0.05));
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(201, 169, 110, 0.1);
}

.book-card__cover {
  width: 44px;
  height: 60px;
  border-radius: 6px;
  background: linear-gradient(150deg, #8b7338 0%, #c9a96e 50%, #d4b878 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(201, 169, 110, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.book-card__cover-text {
  font-size: 18px;
  color: var(--gold-dim);
  font-weight: 700;
}

.book-card__info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.book-card__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  line-height: 1.4;
}

.book-card__author {
  margin: 2px 0;
  font-size: 12px;
  color: var(--ink-dim);
}

.book-card__note {
  margin: 0;
  font-size: 11px;
  color: var(--ink-muted);
  font-style: italic;
}

.context__label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: var(--ink-muted);
  margin: 0 0 12px;
}

.quick-prompts {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prompt-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--ink-dim);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}

.prompt-chip:hover:not(:disabled) {
  background: linear-gradient(140deg, rgba(139, 157, 195, 0.1), rgba(139, 157, 195, 0.05));
  border-color: var(--wanted);
  color: var(--wanted);
  box-shadow: 0 2px 8px rgba(139, 157, 195, 0.12);
}

.prompt-chip:disabled { opacity: 0.4; cursor: not-allowed; }
.prompt-chip__icon { font-size: 15px; }

/* ==========================================
   响应式 — 单栏
   ========================================== */
@media (max-width: 1024px) {
  .study {
    grid-template-columns: 1fr;
  }
  .sidebar,
  .context {
    display: none;
  }
}

@media (max-width: 640px) {
  .msg--ai { padding-right: 20px; }
  .msg--user { padding-left: 20px; }
  .messages { padding: 20px 16px; gap: 22px; }
  .input-area { padding: 12px 16px 16px; }
  .empty-state__grid { grid-template-columns: 1fr; }
}
</style>

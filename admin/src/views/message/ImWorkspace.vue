<template>
  <div class="im-wrap">
    <aside class="im-side">
      <div class="im-side-head">
        <span class="im-side-title">消息</span>
        <el-button text type="primary" size="small" @click="refreshSessions">刷新</el-button>
      </div>
      <p class="im-side-tip">与书友实时沟通；未读仅对「发给你的消息」计数</p>
      <div class="im-session-list" v-loading="sessionsLoading">
        <div
          v-for="s in sessions"
          :key="s.peerId"
          :class="['im-session', { active: selectedPeerId === s.peerId }]"
          @click="selectSession(s)"
        >
          <el-avatar
            :size="44"
            class="im-session-avatar"
            :src="sessionAvatarSrc(s)"
            @error="sessionAvatarFailed[s.peerId] = true"
          >
            {{ initialOf(s.peerName) }}
          </el-avatar>
          <div class="im-session-mid">
            <div class="im-session-row">
              <span class="im-session-name">{{ s.peerName }}</span>
              <span class="im-session-time">{{ formatShortTime(s.lastTime) }}</span>
            </div>
            <div class="im-session-preview">{{ s.lastContent }}</div>
          </div>
          <span v-if="s.unread > 0" class="im-unread-pill">{{ s.unread > 99 ? '99+' : s.unread }}</span>
        </div>
        <div v-if="!sessionsLoading && sessions.length === 0" class="im-empty-side">暂无会话，在书籍等页面发起联系后开始聊天</div>
      </div>
    </aside>

    <main class="im-main">
      <template v-if="selectedPeerId">
        <header class="im-main-head">
          <el-avatar
            :size="40"
            class="im-main-avatar"
            :src="mainHeadSrc"
            @error="mainHeadFailed = true"
          >
            {{ initialOf(selectedPeerName) }}
          </el-avatar>
          <span class="im-main-title">{{ selectedPeerName }}</span>
        </header>
        <div class="im-stream" ref="streamRef" v-loading="msgLoading">
          <div
            v-for="msg in messages"
            :key="msg.messageId"
            :class="['im-bubble-row', isMine(msg) ? 'im-bubble-row--mine' : 'im-bubble-row--peer']"
          >
            <el-avatar
              v-if="!isMine(msg)"
              :size="36"
              class="im-bubble-avatar"
              :src="peerBubbleSrc(msg)"
              @error="bubbleFailed[msg.messageId] = true"
            >
              {{ initialOf(msg.senderName) }}
            </el-avatar>
            <div class="im-bubble-col">
              <div :class="['im-bubble', isMine(msg) ? 'im-bubble--mine' : 'im-bubble--peer']">
                {{ msg.content }}
              </div>
              <div :class="['im-meta', isMine(msg) ? 'im-meta--mine' : 'im-meta--peer']">
                <span>{{ formatFullTime(msg.createTime) }}</span>
                <span v-if="isMine(msg) && Number(msg.isRead) === 1" class="im-read">已读</span>
              </div>
            </div>
            <el-avatar
              v-if="isMine(msg)"
              :size="36"
              class="im-bubble-avatar im-bubble-avatar--mine"
              :src="myBubbleSrc"
              @error="myBubbleFailed = true"
            >
              {{ initialOf(myName) }}
            </el-avatar>
          </div>
          <div v-if="!msgLoading && messages.length === 0" class="im-empty-main">暂无消息，在下方输入后发送</div>
        </div>
        <footer class="im-input-bar">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入消息，Ctrl+Enter 发送"
            @keydown.ctrl.enter.prevent="send"
          />
          <el-button type="primary" class="im-send-btn" :loading="sending" @click="send">发送</el-button>
        </footer>
      </template>
      <div v-else class="im-placeholder">
        <p>选择一个会话开始聊天</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { MessageApi } from '@/api/OtherApi'
import { resolveAvatarUrl } from '@/utils/avatar'

const route = useRoute()
const userStore = useUserStore()

const myId = computed(() => Number(userStore.userInfo?.userId))
const myName = computed(() => userStore.userInfo?.username || '我')

const sessions = ref([])
const sessionsLoading = ref(false)
const selectedPeerId = ref(null)
const selectedPeerName = ref('')
const selectedPeerAvatar = ref('')
const messages = ref([])
const msgLoading = ref(false)
const draft = ref('')
const sending = ref(false)
const streamRef = ref(null)

let pollTimer = null

const sessionAvatarFailed = reactive({})
const bubbleFailed = reactive({})
const mainHeadFailed = ref(false)
const myBubbleFailed = ref(false)

const isMine = msg => Number(msg.senderId) === myId.value

const initialOf = name => (name ? String(name).slice(0, 1).toUpperCase() : '?')

const sessionAvatarSrc = s => {
  if (sessionAvatarFailed[s.peerId]) return undefined
  const u = resolveAvatarUrl(s.peerAvatar)
  return u || undefined
}

watch(selectedPeerAvatar, () => {
  mainHeadFailed.value = false
})

const mainHeadSrc = computed(() => {
  if (mainHeadFailed.value) return undefined
  const u = resolveAvatarUrl(selectedPeerAvatar.value)
  return u || undefined
})

watch(
  () => `${userStore.userInfo?.avatar || ''}-${userStore.userInfo?.headUrl || ''}`,
  () => {
    myBubbleFailed.value = false
  }
)

const myBubbleSrc = computed(() => {
  if (myBubbleFailed.value) return undefined
  const u = resolveAvatarUrl(userStore.userInfo)
  return u || undefined
})

const peerBubbleSrc = msg => {
  if (bubbleFailed[msg.messageId]) return undefined
  const u = resolveAvatarUrl(msg.senderAvatar)
  return u || undefined
}

function syncPeerAvatarFromMessages() {
  const pid = selectedPeerId.value
  if (!pid) return
  const me = myId.value
  const row = messages.value.find(m => {
    const sid = Number(m.senderId)
    const rid = Number(m.receiverId)
    return (sid === pid && rid === me) || (rid === pid && sid === me)
  })
  if (!row) return
  const sid = Number(row.senderId)
  const av = sid === me ? row.receiverAvatar : row.senderAvatar
  if (av) selectedPeerAvatar.value = av
}

const formatShortTime = t => {
  if (!t) return ''
  const s = String(t).replace('T', ' ')
  return s.length > 16 ? s.slice(5, 16) : s
}

const formatFullTime = t => {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

function buildSessions(list, me) {
  const byPeer = new Map()
  for (const m of list) {
    const sid = Number(m.senderId)
    const rid = Number(m.receiverId)
    const peerId = sid === me ? rid : sid
    const peerName = sid === me ? m.receiverName : m.senderName
    const peerAvatar = sid === me ? m.receiverAvatar : m.senderAvatar
    if (!byPeer.has(peerId)) {
      byPeer.set(peerId, {
        peerId,
        peerName: peerName || `用户${peerId}`,
        peerAvatar: peerAvatar || '',
        lastContent: m.content,
        lastTime: m.createTime,
        unread: 0
      })
    } else if (peerAvatar && !byPeer.get(peerId).peerAvatar) {
      byPeer.get(peerId).peerAvatar = peerAvatar
    }
    if (rid === me && Number(m.isRead) === 0) {
      byPeer.get(peerId).unread += 1
    }
  }
  return [...byPeer.values()].sort((a, b) => String(b.lastTime).localeCompare(String(a.lastTime)))
}

const refreshSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await MessageApi.page({ page: 1, size: 200 })
    if (res.status !== 0) {
      ElMessage.error(res.msg || '加载会话失败')
      return
    }
    const list = Array.isArray(res.data) ? res.data : res.data?.list || []
    for (const k of Object.keys(sessionAvatarFailed)) delete sessionAvatarFailed[k]
    sessions.value = buildSessions(list, myId.value)
  } catch {
    ElMessage.error('加载会话失败')
  } finally {
    sessionsLoading.value = false
  }
}

const markIncomingRead = async () => {
  const unread = messages.value.filter(m => Number(m.receiverId) === myId.value && Number(m.isRead) === 0)
  for (const m of unread) {
    try {
      await MessageApi.markAsRead(m.messageId)
      m.isRead = 1
    } catch {
      /* 后端拒绝时忽略单条 */
    }
  }
  if (unread.length) {
    window.dispatchEvent(new Event('balance-updated'))
    refreshSessions()
  }
}

const loadConversation = async (opts = {}) => {
  const { silent = false } = opts
  if (!selectedPeerId.value) return
  if (!silent) msgLoading.value = true
  try {
    const res = await MessageApi.getConversation(selectedPeerId.value, { page: 1, size: 200 })
    if (res.status === 0) {
      if (!silent) {
        for (const k of Object.keys(bubbleFailed)) delete bubbleFailed[k]
      }
      messages.value = Array.isArray(res.data) ? res.data : res.data?.list || []
      syncPeerAvatarFromMessages()
      await nextTick()
      scrollBottom()
      await markIncomingRead()
    }
  } catch {
    if (!silent) ElMessage.error('加载对话失败')
  } finally {
    if (!silent) msgLoading.value = false
  }
}

const scrollBottom = () => {
  const el = streamRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const selectSession = s => {
  selectedPeerId.value = s.peerId
  selectedPeerName.value = s.peerName
  selectedPeerAvatar.value = s.peerAvatar || ''
  loadConversation()
}

const send = async () => {
  const text = draft.value.trim()
  if (!text) {
    ElMessage.warning('请输入内容')
    return
  }
  if (!selectedPeerId.value) return
  sending.value = true
  try {
    const res = await MessageApi.sendMessage({
      receiverId: selectedPeerId.value,
      content: text,
      type: 1
    })
    if (res.status === 0) {
      draft.value = ''
      await loadConversation()
      await refreshSessions()
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const openPeerFromRoute = () => {
  const uid = route.query.userId
  if (!uid) return
  const id = Number(uid)
  const uname = route.query.userName ? String(route.query.userName) : `用户${id}`
  const found = sessions.value.find(s => s.peerId === id)
  if (found) {
    selectSession(found)
  } else {
    selectedPeerId.value = id
    selectedPeerName.value = uname
    selectedPeerAvatar.value = ''
    loadConversation()
  }
}

watch(
  () => [route.query.userId, route.query.userName],
  () => {
    if (route.query.userId) openPeerFromRoute()
  }
)

watch(messages, () => nextTick(() => scrollBottom()), { deep: true })

onMounted(async () => {
  await refreshSessions()
  if (route.query.userId) openPeerFromRoute()
  pollTimer = setInterval(() => {
    if (selectedPeerId.value) loadConversation({ silent: true })
  }, 8000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style lang="scss" scoped>
.im-wrap {
  display: flex;
  height: calc(100vh - 72px);
  min-height: 420px;
  margin: -10px;
  background: #f5f5f7;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
}

.im-side {
  width: 300px;
  flex-shrink: 0;
  background: #ececec;
  border-right: 1px solid #dcdcdc;
  display: flex;
  flex-direction: column;
}

.im-side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 14px 8px;
  background: #f7f7f7;
  border-bottom: 1px solid #e0e0e0;
}

.im-side-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
}

.im-side-tip {
  margin: 0;
  padding: 8px 14px 10px;
  font-size: 11px;
  color: #888;
  line-height: 1.4;
  background: rgba(123, 31, 42, 0.06);
  border-bottom: 1px solid #dcecf5;
}

.im-session-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;
}

.im-session {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }

  &.active {
    background: #d4d4d4;
  }
}

.im-session-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-weight: 700;
}

.im-session-mid {
  flex: 1;
  min-width: 0;
}

.im-session-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}

.im-session-name {
  font-weight: 600;
  font-size: 14px;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.im-session-time {
  font-size: 11px;
  color: #999;
  flex-shrink: 0;
}

.im-session-preview {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.im-unread-pill {
  flex-shrink: 0;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: #ef4444;
  border-radius: 10px;
  text-align: center;
}

.im-empty-side,
.im-empty-main {
  padding: 24px 16px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.im-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #ededed;
}

.im-main-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f9f9f9;
  border-bottom: 1px solid #e5e5e5;
  flex-shrink: 0;
}

.im-main-avatar {
  background: linear-gradient(135deg, #e85d00, #9a2f3c);
  color: #fff;
  font-weight: 700;
}

.im-main-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.im-stream {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.im-bubble-row {
  display: flex;
  /* 头像与气泡顶部对齐（与聊天框上沿齐平） */
  align-items: flex-start;
  gap: 8px;
  max-width: 85%;

  &--peer {
    align-self: flex-start;
  }

  /* 自己：DOM 顺序为「气泡列 → 头像」，头像在消息右侧（勿用 row-reverse，否则会跑到气泡左边） */
  &--mine {
    align-self: flex-end;
    flex-direction: row;
  }
}

.im-bubble-avatar {
  flex-shrink: 0;
  background: #94a3b8;
  color: #fff;
  font-size: 13px;
}

.im-bubble-avatar--mine {
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
}

.im-bubble-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: calc(100% - 44px);
}

.im-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;

  &--peer {
    background: #fff;
    color: #1a1a1a;
    border: 1px solid #e8e8e8;
    border-top-left-radius: 4px;
  }

  &--mine {
    background: #95ec69;
    color: #000;
    border-top-right-radius: 4px;
  }
}

.im-meta {
  font-size: 11px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 8px;

  &--mine {
    justify-content: flex-end;
  }

  &--peer {
    justify-content: flex-start;
  }
}

.im-read {
  color: #64748b;
}

.im-input-bar {
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 12px 14px;
  background: #f9f9f9;
  border-top: 1px solid #e5e5e5;

  :deep(.el-textarea__inner) {
    border-radius: 8px;
  }
}

.im-send-btn {
  flex-shrink: 0;
  margin-bottom: 2px;
}

.im-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 15px;
}
</style>

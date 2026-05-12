<template>
  <div class="book-detail-page">
    <header class="book-detail-page__header">
      <el-button class="book-detail-page__back" round @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <h1 class="book-detail-page__heading">书籍详情</h1>
      <span class="book-detail-page__header-placeholder" />
    </header>

    <main v-loading="loading" class="book-detail-page__main">
      <template v-if="book">
        <div class="detail-shell">
          <article class="detail-hero">
            <div class="detail-hero__grid">
              <div class="detail-visual">
                <div class="detail-visual__blob" aria-hidden="true" />
                <div class="detail-cover-frame">
                  <el-image
                    v-if="book.coverImage"
                    :src="book.coverImage"
                    fit="contain"
                    :preview-src-list="[book.coverImage]"
                    preview-teleported
                    class="detail-cover-frame__img"
                  >
                    <template #error>
                      <div class="detail-cover-placeholder">暂无封面</div>
                    </template>
                  </el-image>
                  <div v-else class="detail-cover-placeholder">暂无封面</div>
                </div>
                <p class="detail-visual__hint">
                  <el-icon><ZoomIn /></el-icon>
                  点击封面可放大预览
                </p>
              </div>

              <div class="detail-intro">
                <div class="detail-intro__tags">
                  <span class="detail-pill detail-pill--accent">二手优选</span>
                  <el-tag v-if="book.status === 1" type="success" size="small" round effect="dark">在售</el-tag>
                  <el-tag v-else-if="book.status === 2" type="warning" size="small" round>已售出</el-tag>
                  <el-tag v-else type="info" size="small" round>已下架</el-tag>
                </div>

                <h2 class="detail-intro__title">{{ book.title }}</h2>
                <p class="detail-intro__subtitle">
                  <el-icon class="detail-intro__ico"><Reading /></el-icon>
                  {{ detailCell(book.author) || '作者未填' }}
                  <span class="detail-intro__dot">·</span>
                  <el-icon class="detail-intro__ico"><FolderOpened /></el-icon>
                  {{ detailCell(book.categoryName) || '未分类' }}
                </p>

                <div class="detail-metrics">
                  <div class="detail-metric">
                    <span class="detail-metric__label">成色</span>
                    <span class="detail-metric__val detail-metric__val--hot">{{ getConditionText(book.condition) }}</span>
                  </div>
                  <div class="detail-metric">
                    <span class="detail-metric__label">库存</span>
                    <span class="detail-metric__val">{{ book.stock != null && book.stock !== '' ? book.stock : '—' }}</span>
                  </div>
                  <div class="detail-metric">
                    <span class="detail-metric__label">浏览</span>
                    <span class="detail-metric__val">{{ book.viewCount ?? 0 }} 次</span>
                  </div>
                </div>

                <div class="detail-price-panel">
                  <div class="detail-price-panel__left">
                    <span class="detail-price-panel__yen">¥</span>
                    <span class="detail-price-panel__num">{{ formatPrice(book.price) }}</span>
                    <span v-if="hasOriginalHigher" class="detail-price-panel__orig">原价 ¥{{ formatPrice(book.originalPrice) }}</span>
                    <span v-if="saveHint" class="detail-price-panel__save">{{ saveHint }}</span>
                  </div>
                  <p class="detail-price-panel__tip">平台担保交易 · 下单后请在订单中心完成支付</p>
                </div>

                <div class="detail-actions">
                  <el-button
                    v-if="!isOwnListing(book)"
                    type="primary"
                    size="large"
                    class="detail-actions__buy"
                    round
                    :disabled="book.status !== 1"
                    @click="handleBuy(book)"
                  >
                    <el-icon class="el-icon--left"><ShoppingCart /></el-icon>
                    立即购买
                  </el-button>
                  <el-button
                    v-if="!isOwnListing(book) && book.sellerId"
                    size="large"
                    class="detail-actions__chat"
                    round
                    @click="handleContact(book)"
                  >
                    <el-icon class="el-icon--left"><ChatDotRound /></el-icon>
                    联系卖家
                  </el-button>
                  <div v-if="isOwnListing(book)" class="detail-actions detail-actions--owner">
                    <el-button
                      size="large"
                      round
                      class="detail-actions__edit"
                      @click="handleEdit"
                    >
                      <el-icon class="el-icon--left"><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button
                      size="large"
                      round
                      :type="book.status === 1 ? 'warning' : 'success'"
                      class="detail-actions__status"
                      @click="handleStatusChange"
                    >
                      {{ book.status === 1 ? '下架' : '上架' }}
                    </el-button>
                    <el-button
                      size="large"
                      round
                      type="danger"
                      class="detail-actions__delete"
                      @click="handleDelete"
                    >
                      <el-icon class="el-icon--left"><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>

                <div v-if="book && !isOwnListing(book)" class="detail-actions detail-actions--extra">
                  <el-button
                    v-if="book.status === 1"
                    size="large"
                    round
                    class="detail-actions__cart"
                    @click="handleAddToCart"
                  >
                    <el-icon class="el-icon--left"><CirclePlus /></el-icon>
                    加入购物车
                  </el-button>
                  <el-button
                    size="large"
                    round
                    plain
                    :type="isFavoriteBook ? 'warning' : 'default'"
                    class="detail-actions__fav"
                    @click="handleToggleFavorite"
                  >
                    <el-icon class="el-icon--left">
                      <StarFilled v-if="isFavoriteBook" />
                      <Star v-else />
                    </el-icon>
                    {{ isFavoriteBook ? '已收藏' : '收藏' }}
                  </el-button>
                </div>

                <div v-if="book?.bookId" class="detail-forum-row">
                  <router-link
                    class="detail-forum-row__link"
                    :to="{ name: 'ForumList', query: { bookId: String(book.bookId) } }"
                  >
                    书友圈 · 相关帖子
                  </router-link>
                  <router-link
                    class="detail-forum-row__link detail-forum-row__link--primary"
                    :to="{ name: 'ForumPublish', query: { bookId: String(book.bookId) } }"
                  >
                    发帖讨论这本书
                  </router-link>
                </div>

                <div v-if="detailCell(book.sellerName) || book.sellerId" class="detail-seller">
                  <div class="detail-seller__avatar">{{ sellerInitial }}</div>
                  <div class="detail-seller__meta">
                    <span class="detail-seller__name">{{ detailCell(book.sellerName) || '卖家' }}</span>
                    <span class="detail-seller__sub">平台认证用户 · 沟通请保持礼貌</span>
                  </div>
                </div>
              </div>
            </div>
          </article>

          <section class="detail-panel">
            <h3 class="detail-panel__title">
              <el-icon><Tickets /></el-icon>
              商品信息
            </h3>
            <ul class="detail-spec-list">
              <li v-for="row in specRows" :key="row.label" class="detail-spec-list__item">
                <span class="detail-spec-list__label">{{ row.label }}</span>
                <span class="detail-spec-list__value">{{ row.value }}</span>
              </li>
            </ul>
          </section>

          <section class="detail-panel detail-panel--desc">
            <h3 class="detail-panel__title">
              <el-icon><Document /></el-icon>
              商品描述
            </h3>
            <p class="detail-desc">{{ detailCell(book.description) || '卖家暂未填写描述。如有疑问，建议使用「联系卖家」沟通。' }}</p>
          </section>

          <!-- 评论板块 -->
          <section class="detail-panel detail-panel--comments">
            <h3 class="detail-panel__title">
              <el-icon><Message /></el-icon>
              用户评论
              <span class="detail-panel__count">{{ comments.length }}</span>
            </h3>
            
            <!-- 评论输入框 -->
            <div class="comment-input-wrap">
              <textarea 
                v-model="commentContent" 
                class="comment-input"
                placeholder="发表您的评论..."
                maxlength="500"
              ></textarea>
              <div class="comment-input__footer">
                <span class="comment-input__count">{{ commentContent.length }}/500</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleSubmitComment"
                  :disabled="!commentContent.trim()"
                >
                  发表评论
                </el-button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div class="comments-list">
              <div 
                v-for="comment in comments" 
                :key="comment.commentId" 
                class="comment-item"
              >
                <div class="comment-item__avatar">{{ getCommentAvatar(comment.username) }}</div>
                <div class="comment-item__content">
                  <div class="comment-item__header">
                    <span class="comment-item__name">{{ comment.username || '用户' }}</span>
                    <span class="comment-item__role" v-if="comment.role === 1">卖家</span>
                    <span class="comment-item__role comment-item__role--admin" v-if="comment.role === 2">管理员</span>
                    <span class="comment-item__time">{{ formatTime(comment.createTime) }}</span>
                    <el-button 
                      v-if="canDeleteComment(comment)"
                      link 
                      type="danger" 
                      size="small" 
                      @click="handleDeleteComment(comment.commentId)"
                    >
                      删除
                    </el-button>
                  </div>
                  <p class="comment-item__text">{{ comment.content }}</p>
                </div>
              </div>
              
              <div v-if="comments.length === 0" class="comments-empty">
                <p>暂无评论，快来发表第一条评论吧！</p>
              </div>
            </div>
          </section>

          <div class="detail-bottom-nav">
            <el-button text type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回书籍列表
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="书籍不存在或已删除" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ChatDotRound,
  CirclePlus,
  Document,
  FolderOpened,
  Reading,
  ShoppingCart,
  Star,
  StarFilled,
  Tickets,
  ZoomIn,
  Edit,
  Delete,
  Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useBookMarketStore } from '@/store/bookMarket'
import { BookApi, BookCategoryApi } from '@/api/BookApi'
import { OrderApi } from '@/api/OtherApi'
import { CommentApi } from '@/api/CommentApi'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const bookMarket = useBookMarketStore()
const { favorites } = storeToRefs(bookMarket)

onMounted(() => {
  bookMarket.loadFromStorage()
  loadCategories()
})

const loading = ref(false)
const book = ref(null)
const categories = ref([])

// 评论相关
const comments = ref([])
const commentContent = ref('')

const bookId = computed(() => {
  const id = route.params.bookId
  const n = Number(id)
  return Number.isFinite(n) ? n : NaN
})

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

const hasOriginalHigher = computed(() => {
  const b = book.value
  if (!b) return false
  const o = Number(b.originalPrice)
  const p = Number(b.price)
  return Number.isFinite(o) && o > 0 && Number.isFinite(p) && o > p
})

const saveHint = computed(() => {
  const b = book.value
  if (!b || !hasOriginalHigher.value) return ''
  const o = Number(b.originalPrice)
  const p = Number(b.price)
  const diff = parseFloat((o - p).toFixed(2))
  if (diff <= 0) return ''
  return `比原价省 ¥${diff}`
})

const specRows = computed(() => {
  const b = book.value
  if (!b) return []
  const dash = (v) => {
    const s = detailCell(v)
    return s || '—'
  }
  return [
    { label: '书名', value: dash(b.title) },
    { label: '作者', value: dash(b.author) },
    { label: 'ISBN', value: dash(b.isbn) },
    { label: '分类', value: dash(b.categoryName) },
    { label: '新旧程度', value: getConditionText(b.condition) },
    { label: '库存', value: b.stock != null && b.stock !== '' ? String(b.stock) : '—' },
    { label: '卖家', value: dash(b.sellerName) },
    { label: '浏览量', value: String(b.viewCount ?? 0) }
  ]
})

const sellerInitial = computed(() => {
  const name = detailCell(book.value?.sellerName)
  if (!name) return '卖'
  return name.slice(0, 1).toUpperCase()
})

const isFavoriteBook = computed(() => {
  const b = book.value
  if (!b) return false
  const id = Number(b.bookId)
  return favorites.value.some(f => Number(f.bookId) === id)
})

function detailCell(val) {
  if (val === undefined || val === null) return ''
  return String(val).trim()
}

function formatPrice(val) {
  if (val === undefined || val === null || val === '') return '0'
  const n = Number(val)
  if (!Number.isFinite(n)) return String(val)
  return String(parseFloat(n.toFixed(2)))
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

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/dashboard/book/list')
  }
}

async function loadBook() {
  if (!Number.isFinite(bookId.value)) {
    book.value = null
    return
  }
  loading.value = true
  book.value = null
  try {
    const res = await BookApi.detail(bookId.value)
    if (res.status === 0 && res.data) {
      book.value = res.data
    }
  } catch {
    /* request 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  if (!Number.isFinite(bookId.value)) {
    comments.value = []
    return
  }
  try {
    const res = await CommentApi.getComments(bookId.value)
    if (res.status === 0 && Array.isArray(res.data)) {
      comments.value = res.data
    }
  } catch (error) {
    comments.value = []
  }
}

watch(
  () => route.params.bookId,
  async () => {
    await loadBook()
    await loadComments()
  },
  { immediate: true }
)

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
        await loadBook()
      }
    } catch {
      /* 同上 */
    } finally {
      loading.value = false
    }
  } catch {
    /* 取消 */
  }
}

const handleAddToCart = () => {
  const b = book.value
  if (!b || isOwnListing(b)) return
  if (b.status !== 1) {
    ElMessage.warning('当前商品不可加入购物车')
    return
  }
  bookMarket.addToCart(b, 1)
  ElMessage.success('已加入购物车，可在订单中心「购物车」查看')
}

const handleToggleFavorite = () => {
  const b = book.value
  if (!b || isOwnListing(b)) return
  const was = isFavoriteBook.value
  bookMarket.toggleFavorite(b)
  ElMessage.success(was ? '已取消收藏' : '已加入收藏，可在订单中心「我的收藏」查看')
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

// 卖家操作方法
const handleEdit = () => {
  const b = book.value
  if (!b) return
  router.push(`/dashboard/book/edit/${b.bookId}`)
}

const handleStatusChange = async () => {
  const b = book.value
  if (!b) return
  
  const newStatus = b.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该书籍吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await BookApi.updateStatus(b.bookId, newStatus)
    if (res.status === 0) {
      ElMessage.success(`${text}成功`)
      await loadBook()
    } else {
      ElMessage.error(res.msg || `${text}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${text}失败：` + error.message)
    }
  }
}

const handleDelete = async () => {
  const b = book.value
  if (!b) return
  
  try {
    await ElMessageBox.confirm('确定要删除该书籍吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await BookApi.delete(b.bookId)
    if (res.status === 0) {
      ElMessage.success('删除成功')
      goBack()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 评论相关方法
const handleSubmitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    const res = await CommentApi.addComment(bookId.value, commentContent.value.trim())
    if (res.status === 0) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      await loadComments()
    } else {
      ElMessage.error(res.msg || '评论失败')
    }
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const b = book.value
    const res = await CommentApi.deleteComment(commentId, b?.sellerId)
    if (res.status === 0) {
      ElMessage.success('删除成功')
      await loadComments()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

const canDeleteComment = (comment) => {
  const userRole = userStore.userInfo?.role
  const userId = userStore.userInfo?.userId
  const b = book.value
  
  // 管理员可以删除任何评论
  if (userRole === 2) {
    return true
  }
  
  // 卖家可以删除自己书籍的评论
  if (userRole === 1 && userId && b?.sellerId && Number(userId) === Number(b.sellerId)) {
    return true
  }
  
  return false
}

const getCommentAvatar = (name) => {
  if (!name) return '评'
  return name.slice(0, 1).toUpperCase()
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 小于24小时
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 显示日期
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const loadCategories = async () => {
  try {
    const res = await BookCategoryApi.list()
    if (res.status === 0) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}
</script>

<style lang="scss" scoped>
.book-detail-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 72px);
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  background: var(--market-gradient-page);
}

.book-detail-page__header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  /* 不透明顶栏：避免 backdrop-filter + 半透明在滚动时与正文叠层产生重影、条状镂空（Chrome / Win 常见） */
  background: var(--market-card, #fffefb);
  border-bottom: 1px solid rgba(123, 31, 42, 0.1);
  position: sticky;
  top: 0;
  z-index: 20;
  box-shadow: 0 1px 0 rgba(44, 24, 16, 0.04);
}

.book-detail-page__back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.book-detail-page__heading {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #2c1810;
  letter-spacing: 0.02em;
}

.book-detail-page__header-placeholder {
  width: 88px;
}

.book-detail-page__main {
  flex: 1;
  padding: 24px 20px 40px;
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
  box-sizing: border-box;
}

.detail-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-hero {
  background: #fffefb;
  border-radius: 20px;
  padding: 28px 28px 32px;
  box-shadow:
    0 4px 6px -1px rgba(44, 24, 16, 0.06),
    0 24px 48px -12px rgba(123, 31, 42, 0.1);
  border: 1px solid rgba(44, 24, 16, 0.08);
  overflow: hidden;
}

.detail-hero__grid {
  display: grid;
  grid-template-columns: minmax(260px, 340px) 1fr;
  gap: 36px;
  align-items: start;
}

.detail-visual {
  position: relative;
  text-align: center;
}

.detail-visual__blob {
  position: absolute;
  inset: -12% -8% auto -8%;
  height: 72%;
  background: radial-gradient(ellipse at center, rgba(154, 47, 60, 0.22) 0%, transparent 68%);
  pointer-events: none;
  border-radius: 50%;
  filter: blur(2px);
}

.detail-cover-frame {
  position: relative;
  margin: 0 auto;
  max-width: 300px;
  aspect-ratio: 1;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(145deg, #faf8f5, #f3ece4);
  box-shadow:
    0 12px 28px rgba(44, 24, 16, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(44, 24, 16, 0.08);
}

.detail-cover-frame__img {
  width: 100%;
  height: 100%;
  cursor: zoom-in;
}

.detail-cover-placeholder {
  width: 100%;
  height: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a8a29e;
  font-size: 14px;
}

.detail-visual__hint {
  margin: 14px 0 0;
  font-size: 12px;
  color: #78716c;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-intro__tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-pill {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  letter-spacing: 0.02em;
}

.detail-pill--accent {
  color: #7b1f2a;
  background: rgba(123, 31, 42, 0.08);
  border: 1px solid rgba(123, 31, 42, 0.22);
}

.detail-intro__title {
  margin: 0 0 10px;
  font-size: clamp(1.35rem, 2.5vw, 1.75rem);
  font-weight: 800;
  color: #1c1917;
  line-height: 1.35;
  letter-spacing: -0.02em;
}

.detail-intro__subtitle {
  margin: 0 0 20px;
  font-size: 14px;
  color: #57534e;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.detail-intro__ico {
  font-size: 16px;
  color: #9a2f3c;
}

.detail-intro__dot {
  color: #d6d3d1;
  margin: 0 2px;
}

.detail-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}

.detail-metric {
  flex: 1;
  min-width: 88px;
  padding: 10px 14px;
  border-radius: 12px;
  background: #faf8f5;
  border: 1px solid rgba(44, 24, 16, 0.1);
}

.detail-metric__label {
  display: block;
  font-size: 11px;
  color: #78716c;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}

.detail-metric__val {
  font-size: 15px;
  font-weight: 700;
  color: #2c1810;
}

.detail-metric__val--hot {
  color: #d4380d;
}

.detail-price-panel {
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(110deg, #fff7ed 0%, #fff 55%, #fef3c7 100%);
  border: 1px solid rgba(251, 191, 36, 0.35);
  margin-bottom: 22px;
}

.detail-price-panel__left {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 14px;
}

.detail-price-panel__yen {
  font-size: 22px;
  font-weight: 800;
  color: #d4380d;
}

.detail-price-panel__num {
  font-size: 36px;
  font-weight: 800;
  color: #d4380d;
  letter-spacing: -0.03em;
  line-height: 1;
}

.detail-price-panel__orig {
  font-size: 13px;
  color: #a8a29e;
  text-decoration: line-through;
}

.detail-price-panel__save {
  font-size: 12px;
  font-weight: 600;
  color: #b45309;
  background: rgba(254, 243, 199, 0.9);
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(251, 191, 36, 0.5);
}

.detail-price-panel__tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: #78716c;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 22px;
}

.detail-actions__buy {
  min-width: 148px;
  height: 46px !important;
  font-weight: 700 !important;
  border: none !important;
  background: linear-gradient(180deg, #ff7a1a, #e85d00) !important;
  box-shadow: 0 8px 20px rgba(234, 88, 12, 0.35);

  &:hover:not(.is-disabled) {
    filter: brightness(1.05);
  }
}

.detail-actions__chat {
  min-width: 132px;
  height: 46px !important;
  font-weight: 600 !important;
  border-color: rgba(255, 106, 0, 0.45) !important;
  color: #7b1f2a !important;
  background: #fffefb !important;

  &:hover {
    border-color: #ff6a00 !important;
    background: rgba(123, 31, 42, 0.06) !important;
  }
}

.detail-actions--extra {
  margin-top: -10px;
  margin-bottom: 22px;
}

.detail-forum-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 18px;
  margin-top: -6px;
  margin-bottom: 20px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(123, 31, 42, 0.06);
  border: 1px solid rgba(123, 31, 42, 0.12);
}

.detail-forum-row__link {
  font-size: 14px;
  font-weight: 600;
  color: #57534e;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: color 0.15s ease, border-color 0.15s ease;

  &:hover {
    color: #7b1f2a;
    border-bottom-color: rgba(123, 31, 42, 0.35);
  }
}

.detail-forum-row__link--primary {
  color: #7b1f2a;

  &:hover {
    color: #5c1720;
    border-bottom-color: rgba(92, 23, 32, 0.45);
  }
}

.detail-actions__cart {
  min-width: 132px;
  height: 46px !important;
  font-weight: 600 !important;
  border-color: rgba(44, 24, 16, 0.18) !important;
  color: #44403c !important;
  background: #fffefb !important;

  &:hover {
    border-color: #ff6a00 !important;
    color: #b45309 !important;
    background: rgba(255, 106, 0, 0.08) !important;
  }
}

.detail-actions__fav {
  min-width: 120px;
  height: 46px !important;
  font-weight: 600 !important;
}

.detail-seller {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(90deg, #faf8f5, #f3ece4);
  border: 1px solid rgba(44, 24, 16, 0.1);
}

.detail-seller__avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-weight: 800;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-seller__name {
  display: block;
  font-weight: 700;
  color: #1c1917;
  font-size: 15px;
}

.detail-seller__sub {
  font-size: 12px;
  color: #78716c;
}

.detail-panel {
  background: #fffefb;
  border-radius: 18px;
  padding: 22px 24px;
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.06);
  border: 1px solid rgba(44, 24, 16, 0.08);
}

.detail-panel--desc {
  padding-bottom: 26px;
}

.detail-panel__title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 700;
  color: #2c1810;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: #9a2f3c;
    font-size: 18px;
  }
}

.detail-spec-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(44, 24, 16, 0.1);
}

.detail-spec-list__item {
  display: grid;
  grid-template-columns: 96px 1fr;
  align-items: stretch;
  border-bottom: 1px solid rgba(44, 24, 16, 0.1);

  &:nth-last-child(-n + 2) {
    border-bottom: none;
  }

  &:nth-child(odd) {
    border-right: 1px solid rgba(44, 24, 16, 0.1);
  }
}

.detail-spec-list__label {
  padding: 12px 14px;
  font-size: 13px;
  color: #78716c;
  background: #faf8f5;
  font-weight: 500;
}

.detail-spec-list__value {
  padding: 12px 14px;
  font-size: 14px;
  color: #1c1917;
  background: #fff;
  word-break: break-word;
}

.detail-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #57534e;
  padding: 16px 18px;
  border-radius: 12px;
  background: #faf8f5;
  border: 1px dashed rgba(44, 24, 16, 0.15);
}

.detail-bottom-nav {
  display: flex;
  justify-content: center;
  padding-top: 4px;
}

@media (max-width: 900px) {
  .detail-hero__grid {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .detail-cover-frame {
    max-width: 280px;
  }

  .detail-spec-list {
    grid-template-columns: 1fr;
  }

  .detail-spec-list__item {
    grid-template-columns: 88px 1fr;
    border-right: none !important;

    &:nth-child(odd) {
      border-right: none;
    }

    &:not(:last-child) {
      border-bottom: 1px solid rgba(44, 24, 16, 0.1);
    }
  }
}

@media (max-width: 480px) {
  .detail-hero {
    padding: 20px 18px;
  }

  .detail-price-panel__num {
    font-size: 30px;
  }

  .detail-actions {
    flex-direction: column;
  }

  .detail-actions__buy,
  .detail-actions__chat,
  .detail-actions__cart,
  .detail-actions__fav {
    width: 100%;
  }
}

/* 评论相关样式 */
.detail-panel--comments {
  padding-bottom: 26px;
}

.detail-panel__count {
  font-size: 13px;
  font-weight: 500;
  color: #78716c;
  margin-left: 4px;
}

.comment-input-wrap {
  margin-bottom: 24px;
  background: #faf8f5;
  border-radius: 12px;
  padding: 16px;
  border: 1px dashed rgba(44, 24, 16, 0.15);
}

.comment-input {
  width: 100%;
  min-height: 80px;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  line-height: 1.6;
  color: #2c1810;
  resize: none;
  box-sizing: border-box;
  
  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px rgba(123, 31, 42, 0.1);
  }
  
  &::placeholder {
    color: #a8a29e;
  }
}

.comment-input__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.comment-input__count {
  font-size: 12px;
  color: #a8a29e;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #faf8f5;
  border-radius: 12px;
}

.comment-item__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-item__content {
  flex: 1;
  min-width: 0;
}

.comment-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.comment-item__name {
  font-weight: 600;
  color: #2c1810;
  font-size: 14px;
}

.comment-item__role {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(5, 150, 105, 0.1);
  color: #059669;
  font-weight: 500;
  
  &--admin {
    background: rgba(249, 115, 22, 0.1);
    color: #f97316;
  }
}

.comment-item__time {
  font-size: 12px;
  color: #a8a29e;
  margin-left: auto;
}

.comment-item__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #44403c;
  word-break: break-word;
}

.comments-empty {
  text-align: center;
  padding: 32px;
  color: #a8a29e;
  font-size: 14px;
}
</style>

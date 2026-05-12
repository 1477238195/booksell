<template>
  <div class="wanted-detail-page">
    <header class="wanted-detail-page__header">
      <el-button class="wanted-detail-page__back" round @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <h1 class="wanted-detail-page__heading">求购详情</h1>
      <span class="wanted-detail-page__header-placeholder" />
    </header>

    <main v-loading="loading" class="wanted-detail-page__main">
      <template v-if="wanted">
        <div class="detail-shell">
          <article class="detail-hero">
            <div class="detail-hero__grid">
              <div class="detail-visual">
                <div class="detail-visual__blob" aria-hidden="true" />
                <div class="detail-cover-frame">
                  <el-image
                    v-if="wanted.coverImage"
                    :src="wanted.coverImage"
                    fit="contain"
                    :preview-src-list="[wanted.coverImage]"
                    preview-teleported
                    class="detail-cover-frame__img"
                  >
                    <template #error>
                      <div class="detail-cover-placeholder">暂无图片</div>
                    </template>
                  </el-image>
                  <div v-else class="detail-cover-placeholder">暂无图片</div>
                </div>
                <p class="detail-visual__hint">
                  <el-icon><ZoomIn /></el-icon>
                  点击图片可放大预览
                </p>
              </div>

              <div class="detail-intro">
                <div class="detail-intro__tags">
                  <span class="detail-pill detail-pill--accent">求购信息</span>
                  <el-tag v-if="wanted.status === 1" type="success" size="small" round effect="dark">求购中</el-tag>
                  <el-tag v-else-if="wanted.status === 2" type="warning" size="small" round>已找到</el-tag>
                  <el-tag v-else type="info" size="small" round>已关闭</el-tag>
                </div>

                <h2 class="detail-intro__title">{{ wanted.bookTitle }}</h2>
                <p class="detail-intro__subtitle">
                  <el-icon class="detail-intro__ico"><User /></el-icon>
                  {{ detailCell(wanted.author) || '作者未填' }}
                </p>

                <div class="detail-price-panel">
                  <div class="detail-price-panel__left">
                    <span class="detail-price-panel__label">期望价格</span>
                    <span class="detail-price-panel__yen">¥</span>
                    <span class="detail-price-panel__num">{{ formatPrice(wanted.maxPrice) }}</span>
                    <span class="detail-price-panel__hint">以内</span>
                  </div>
                  <p class="detail-price-panel__tip">找到合适书籍可联系求购方沟通</p>
                </div>

                <div class="detail-actions">
                  <el-button
                    v-if="!isOwnWanted"
                    type="primary"
                    size="large"
                    class="detail-actions__contact"
                    round
                    :disabled="wanted.status !== 1"
                    @click="handleContact"
                  >
                    <el-icon class="el-icon--left"><ChatDotRound /></el-icon>
                    联系求购方
                  </el-button>
                  <div v-if="isOwnWanted" class="detail-actions detail-actions--owner">
                    <el-button
                      size="large"
                      round
                      class="detail-actions__edit"
                      @click="goEdit"
                    >
                      <el-icon class="el-icon--left"><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button
                      size="large"
                      round
                      :type="wanted.status === 1 ? 'warning' : 'success'"
                      class="detail-actions__status"
                      @click="handleStatusChange"
                    >
                      {{ wanted.status === 1 ? '关闭求购' : '重新求购' }}
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

                <div class="detail-seller">
                  <div class="detail-seller__avatar">{{ wantedInitial }}</div>
                  <div class="detail-seller__meta">
                    <span class="detail-seller__name">{{ detailCell(wanted.username) || '求购方' }}</span>
                    <span class="detail-seller__sub">平台认证用户 · 沟通请保持礼貌</span>
                  </div>
                </div>
              </div>
            </div>
          </article>

          <section class="detail-panel">
            <h3 class="detail-panel__title">
              <el-icon><Tickets /></el-icon>
              求购信息
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
              求购描述
            </h3>
            <p class="detail-desc">{{ detailCell(wanted.description) || '求购方暂未填写描述。如有合适书籍，建议使用「联系求购方」沟通。' }}</p>
          </section>

          <div class="detail-bottom-nav">
            <el-button text type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回求购列表
            </el-button>
            <el-button text type="primary" @click="goPublish">
              <el-icon><Plus /></el-icon>
              发布求购
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="求购信息不存在或已删除" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ZoomIn, User, ChatDotRound, Delete, Plus, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { BookWantedApi } from '@/api/OtherApi'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const wanted = ref(null)

const wantedId = computed(() => {
  const id = route.params.wantedId
  const n = Number(id)
  return Number.isFinite(n) ? n : NaN
})

const myUserId = computed(() => {
  const id = userStore.userInfo?.userId
  if (id === undefined || id === null || id === '') return null
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})

const isOwnWanted = computed(() => {
  if (!wanted.value || myUserId.value == null) return false
  const uid = wanted.value.userId ?? wanted.value.user_id
  if (uid !== undefined && uid !== null && uid !== '') {
    return Number(uid) === myUserId.value
  }
  const me = userStore.userInfo?.username
  if (me && wanted.value.username) return String(wanted.value.username) === String(me)
  return false
})

const wantedInitial = computed(() => {
  const name = detailCell(wanted.value?.username)
  if (!name) return '求'
  return name.slice(0, 1).toUpperCase()
})

const specRows = computed(() => {
  const w = wanted.value
  if (!w) return []
  const dash = (v) => {
    const s = detailCell(v)
    return s || '—'
  }
  return [
    { label: '书籍名称', value: dash(w.bookTitle) },
    { label: '作者', value: dash(w.author) },
    { label: '期望价格', value: `¥${formatPrice(w.maxPrice)} 以内` },
    { label: '求购状态', value: getStatusText(w.status) },
    { label: '求购方', value: dash(w.username) },
    { label: '浏览量', value: String(w.viewCount ?? 0) }
  ]
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

function getStatusText(status) {
  const map = {
    1: '求购中',
    2: '已找到',
    0: '已关闭'
  }
  return map[status] || '未知'
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/dashboard/wanted/list')
  }
}

function goPublish() {
  router.push('/dashboard/wanted/publish')
}

function goEdit() {
  router.push(`/dashboard/wanted/publish?wantedId=${wanted.value.wantedId}`)
}

async function loadWanted() {
  if (!Number.isFinite(wantedId.value)) {
    wanted.value = null
    return
  }
  loading.value = true
  wanted.value = null
  try {
    const res = await BookWantedApi.detail(wantedId.value)
    if (res.status === 0 && res.data) {
      wanted.value = res.data
    }
  } catch {
    /* request 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function handleContact() {
  const w = wanted.value
  if (!w) return
  const uid = w.userId ?? w.user_id
  if (!uid) return
  router.push({
    path: '/dashboard/message/chat',
    query: { userId: uid, userName: w.username || '求购方' }
  })
}

const handleStatusChange = async () => {
  const w = wanted.value
  if (!w) return
  
  const newStatus = w.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '开启' : '关闭'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该求购吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await BookWantedApi.updateStatus(w.wantedId, newStatus)
    if (res.status === 0) {
      ElMessage.success(`${text}成功`)
      await loadWanted()
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
  const w = wanted.value
  if (!w) return
  
  try {
    await ElMessageBox.confirm('确定要删除该求购吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await BookWantedApi.delete(w.wantedId)
    if (res.status === 0) {
      ElMessage.success('删除成功')
      router.push('/dashboard/wanted/my')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

watch(
  () => route.params.wantedId,
  () => loadWanted(),
  { immediate: true }
)
</script>

<style lang="scss" scoped>
$paper: #f6f1e9;
$wine: #7b1f2a;
$wine-light: #9a2f3c;

.wanted-detail-page {
  min-height: calc(100vh - 72px);
  padding: 0 24px 24px;
  background: linear-gradient(180deg, #faf6ef 0%, $paper 42%, #efe8dc 100%);
}

.wanted-detail-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid rgba(123, 31, 42, 0.12);
  margin-bottom: 20px;
}

.wanted-detail-page__back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
}

.wanted-detail-page__heading {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #2c1810;
  letter-spacing: 0.06em;
  font-family: 'Source Han Serif SC', 'Songti SC', 'SimSun', serif;
}

.wanted-detail-page__header-placeholder {
  width: 100px;
}

.wanted-detail-page__main {
  max-width: 900px;
  margin: 0 auto;
}

.detail-shell {
  background: #fffefb;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(44, 24, 16, 0.06);
}

.detail-hero {
  margin-bottom: 24px;
}

.detail-hero__grid {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
}

.detail-visual {
  position: relative;
  
  &__blob {
    position: absolute;
    top: -20px;
    right: -20px;
    width: 160px;
    height: 160px;
    background: linear-gradient(135deg, rgba(123, 31, 42, 0.08), rgba(123, 31, 42, 0.02));
    border-radius: 50%;
  }
  
  &__hint {
    margin: 10px 0 0;
    font-size: 12px;
    color: #a8a29e;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.detail-cover-frame {
  position: relative;
  width: 100%;
  height: 320px;
  background: $paper;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-cover-frame__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.detail-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #a8a29e;
}

.detail-intro {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-intro__tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-pill {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  
  &--accent {
    background: rgba(123, 31, 42, 0.08);
    color: $wine;
  }
}

.detail-intro__title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1c1917;
  line-height: 1.35;
}

.detail-intro__subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b5344;
  display: flex;
  align-items: center;
  gap: 6px;
  
  &__dot {
    color: #a8a29e;
    margin: 0 4px;
  }
  
  &__ico {
    font-size: 14px;
    color: #9a8c7e;
  }
}

.detail-price-panel {
  background: linear-gradient(135deg, rgba(20, 184, 166, 0.08), rgba(20, 184, 166, 0.02));
  padding: 16px;
  border-radius: 12px;
  
  &__left {
    display: flex;
    align-items: baseline;
    gap: 6px;
    margin-bottom: 8px;
  }
  
  &__label {
    font-size: 14px;
    color: #6b5344;
  }
  
  &__yen {
    font-size: 20px;
    font-weight: 800;
    color: #059669;
  }
  
  &__num {
    font-size: 36px;
    font-weight: 900;
    color: #059669;
  }
  
  &__hint {
    font-size: 14px;
    color: #6b5344;
  }
  
  &__tip {
    margin: 0;
    font-size: 12px;
    color: #a8a29e;
  }
}

.detail-actions {
  display: flex;
  gap: 12px;
  
  &--owner {
    gap: 10px;
  }
  
  &__contact {
    background: #059669;
    
    &:hover {
      background: #047857;
    }
  }
  
  &__status {
    flex: 1;
  }
  
  &__delete {
    flex: 1;
  }
}

.detail-seller {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: $paper;
  border-radius: 10px;
  
  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, $wine-light, $wine);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 700;
    font-size: 16px;
  }
  
  &__meta {
    flex: 1;
  }
  
  &__name {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #2c1810;
  }
  
  &__sub {
    display: block;
    font-size: 12px;
    color: #a8a29e;
    margin-top: 2px;
  }
}

.detail-panel {
  margin-bottom: 20px;
  padding: 22px 24px;
  background: #fffefb;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.06);
  border: 1px solid rgba(44, 24, 16, 0.08);
  
  &--desc {
    padding-bottom: 26px;
    background: #fff;
    border: 1px solid rgba(44, 24, 16, 0.06);
  }
  
  &__title {
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

  &:nth-last-child(-n+2) {
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
}

.detail-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #44403c;
}

.detail-bottom-nav {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid rgba(44, 24, 16, 0.08);
}

@media (max-width: 768px) {
  .detail-hero__grid {
    grid-template-columns: 1fr;
  }
  
  .detail-cover-frame {
    height: 240px;
  }
}
</style>

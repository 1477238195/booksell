<template>
  <div class="profile-page">
    <div class="profile-bg" aria-hidden="true" />
    <div class="profile-inner">
      <header class="profile-hero">
        <p class="profile-kicker">账户中心</p>
        <h1 class="profile-title">个人信息</h1>
        <p class="profile-sub">查看与管理你的账户资料与二手币余额</p>
      </header>

      <section class="profile-card profile-card--hero">
        <div class="profile-avatar-wrap">
          <el-avatar
            :size="88"
            :src="heroAvatarSrc"
            class="profile-avatar-lg"
            @error="avatarBroken = true"
          >
            {{ avatarLetter }}
          </el-avatar>
          <div class="profile-hero-text">
            <h2 class="profile-name">{{ displayName }}</h2>
            <div class="profile-tags">
              <el-tag :type="roleTagType" effect="light" round>{{ roleName }}</el-tag>
              <span v-if="userInfo?.userId" class="profile-id">ID {{ userInfo.userId }}</span>
            </div>
          </div>
        </div>
        <div class="profile-balance" v-loading="balanceLoading">
          <span class="profile-balance-label">二手币余额</span>
          <strong class="profile-balance-num">{{ formatBalance(balance) }}</strong>
          <el-button text type="primary" size="small" @click="refreshBalance">刷新</el-button>
        </div>
      </section>

      <section class="profile-card">
        <div class="profile-section-head">
          <h3 class="profile-section-title">基本资料</h3>
          <el-button type="primary" plain round @click="goToEdit">
            <span class="btn-edit-inner">修改信息</span>
          </el-button>
        </div>
        <el-descriptions :column="1" border class="profile-desc">
          <el-descriptions-item label="用户名">{{ userInfo?.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ userInfo?.realName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo?.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo?.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag v-if="userInfo?.status === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else-if="userInfo?.status === 0" type="danger" size="small">已禁用</el-tag>
            <span v-else>—</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <div class="profile-actions">
        <el-button @click="router.push('/dashboard')">返回系统首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import AuthApi from '@/api/AuthApi'
import { UserApi } from '@/api/OtherApi'
import { resolveAvatarUrl } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户')
const avatarLetter = computed(() => {
  const n = userInfo.value?.username
  return n ? String(n).slice(0, 1).toUpperCase() : '用'
})
const avatarDisplayUrl = computed(() => resolveAvatarUrl(userInfo.value))
const avatarBroken = ref(false)
const heroAvatarSrc = computed(() => {
  if (avatarBroken.value || !avatarDisplayUrl.value) return undefined
  return avatarDisplayUrl.value
})

watch(avatarDisplayUrl, () => {
  avatarBroken.value = false
})

const roleName = computed(() => {
  const m = { 0: '普通用户', 1: '管理员' }
  return m[userInfo.value.role] ?? '访客'
})
const roleTagType = computed(() => (Number(userInfo.value.role) === 1 ? 'danger' : 'info'))

const balance = ref(null)
const balanceLoading = ref(false)

const formatBalance = v => {
  if (v === null || v === undefined) return '—'
  const num = typeof v === 'string' ? parseFloat(v) : v
  if (Number.isNaN(num)) return '—'
  return num.toFixed(2)
}

const loadBalance = async () => {
  balanceLoading.value = true
  try {
    const res = await UserApi.getBalance()
    if (res.status === 0) {
      balance.value = res.data
    }
  } catch {
    balance.value = null
  } finally {
    balanceLoading.value = false
  }
}

const refreshBalance = () => {
  loadBalance()
  window.dispatchEvent(new Event('balance-updated'))
}

const syncUserInfo = async () => {
  try {
    const res = await AuthApi.getCurrentUser()
    if (res.status === 0 && res.data) {
      userStore.setUserInfo({ userInfo: res.data })
    }
  } catch {
    /* 保持本地 cookie 信息 */
  }
}

const goToEdit = () => {
  router.push('/dashboard/profile/edit')
}

onMounted(async () => {
  await syncUserInfo()
  await loadBalance()
})
</script>

<style lang="scss" scoped>
.profile-page {
  position: relative;
  min-height: calc(100vh - 72px);
  margin: -10px;
  padding: 24px 16px 40px;
  overflow: auto;
}

.profile-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 480px at 20% 0%, rgba(154, 47, 60, 0.14), transparent 50%),
    radial-gradient(700px 400px at 100% 30%, rgba(255, 106, 0, 0.1), transparent 45%),
    var(--market-gradient-page);
  pointer-events: none;
}

.profile-inner {
  position: relative;
  z-index: 1;
  max-width: 560px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-hero {
  text-align: center;
  padding-top: 8px;
}

.profile-kicker {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: #9a2f3c;
  font-weight: 600;
  margin: 0 0 8px;
}

.profile-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #2c1810;
}

.profile-sub {
  margin: 0;
  font-size: 14px;
  color: #78716c;
}

.profile-card {
  background: rgba(255, 254, 251, 0.96);
  border-radius: 16px;
  padding: 20px 22px;
  border: 1px solid rgba(44, 24, 16, 0.08);
  box-shadow: 0 8px 30px -12px rgba(44, 24, 16, 0.1);
}

.profile-card--hero {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.profile-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.profile-avatar-lg {
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-hero-text {
  min-width: 0;
}

.profile-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #2c1810;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.profile-id {
  font-size: 13px;
  color: #a8a29e;
}

.profile-balance {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  min-width: 140px;
}

.profile-balance-label {
  font-size: 12px;
  color: #78716c;
}

.profile-balance-num {
  font-size: 24px;
  font-weight: 800;
  color: #7b1f2a;
  font-variant-numeric: tabular-nums;
}

.profile-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.profile-section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #44403c;
}

.btn-edit-inner {
  font-weight: 600;
}

.profile-desc {
  --el-descriptions-item-bordered-label-background: #faf8f5;
}

.profile-actions {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>

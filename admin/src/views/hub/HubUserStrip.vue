<template>
  <div class="hub-panel-top">
    <div
      class="hub-identity-link"
      title="查看个人信息"
      role="link"
      tabindex="0"
      @click="goProfile"
      @keydown.enter.prevent="goProfile"
    >
      <div class="hub-identity">
        <el-avatar
          :size="52"
          class="hub-identity-avatar"
          :src="hubAvatarSrc"
          @error="hubAvatarFailed = true"
        >
          {{ avatarLetter }}
        </el-avatar>
        <div class="hub-identity-text">
          <span class="hub-identity-name">{{ userInfo?.username || '用户' }}</span>
          <el-tag :type="roleTagType" size="small" effect="light" round>{{ roleName }}</el-tag>
        </div>
        <el-icon class="hub-identity-chevron"><ArrowRight /></el-icon>
      </div>
    </div>
    <div class="hub-identity-meta">
      <span>用户 ID</span>
      <strong>{{ userInfo?.userId ?? '—' }}</strong>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { resolveAvatarUrl } from '@/utils/avatar'
import { spaPush } from '@/utils/spaNavigate'

const userStore = useUserStore()

function goProfile() {
  spaPush('/dashboard/profile')
}
const userInfo = computed(() => userStore.userInfo || {})

const hubAvatarFailed = ref(false)
watch(
  () => `${userInfo.value?.avatar || ''}-${userInfo.value?.headUrl || ''}`,
  () => {
    hubAvatarFailed.value = false
  }
)
const hubAvatarSrc = computed(() => {
  if (hubAvatarFailed.value) return undefined
  const u = resolveAvatarUrl(userInfo.value)
  return u || undefined
})

const roleName = computed(() => {
  const roles = { 0: '普通用户', 1: '管理员' }
  return roles[userInfo.value.role] ?? '访客'
})

const roleTagType = computed(() => (userInfo.value.role === 1 ? 'danger' : 'info'))

const avatarLetter = computed(() => {
  const n = userInfo.value?.username
  return n ? String(n).slice(0, 1).toUpperCase() : '书'
})
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';

.hub-identity-link {
  flex: 1;
  min-width: 0;
  text-decoration: none;
  color: inherit;
  border-radius: 12px;
  margin: -6px;
  padding: 6px;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(123, 31, 42, 0.08);
  }

  &:focus-visible {
    outline: 2px solid rgba(123, 31, 42, 0.45);
    outline-offset: 2px;
  }
}

.hub-identity {
  position: relative;
  padding-right: 28px;
}

.hub-identity-chevron {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  color: #d6d3d1;
  font-size: 16px;
}

.hub-identity-link:hover .hub-identity-chevron {
  color: #e85d00;
}
</style>

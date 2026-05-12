<template>
  <div v-if="userInfo" class="user-container">
    <el-dropdown trigger="click">
      <div class="user-wrapper">
        <img
          :src="headerAvatarSrc"
          class="user-avatar"
          @error="headerAvatarFailed = true"
        />
        <div class="user-name">
          {{ userInfo.nickname || userInfo.username }}
        </div>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="goProfile">
            <span class="dropdown-item-main">个人信息</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { useRouter, useRoute } from 'vue-router'
import { resolveAvatarUrl } from '@/utils/avatar'

const defaultHeaderAvatar =
  'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif?imageView2/1/w/80/h/80'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const userInfo = computed(() => userStore.userInfo)

const headerAvatarFailed = ref(false)
watch(
  () => userInfo.value && `${userInfo.value.avatar || ''}-${userInfo.value.headUrl || ''}`,
  () => {
    headerAvatarFailed.value = false
  }
)
const headerAvatarSrc = computed(() => {
  if (headerAvatarFailed.value) return defaultHeaderAvatar
  const u = resolveAvatarUrl(userInfo.value)
  return u || defaultHeaderAvatar
})

const goProfile = () => {
  router.push('/dashboard/profile')
}

const logout = async () => {
  await userStore.logout()
  permissionStore.addRoutes = []
  router.push(`/login?redirect=${route.fullPath}`)
}
</script>

<style lang="scss" scoped>
.user-container {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 10px;
  transition: background 0.3s;
  gap: 15px;

  .user-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;

    .user-avatar {
      width: 25px;
      height: 25px;
      border-radius: 5px;
    }

    .user-name {
      padding-left: 5px;
      white-space: nowrap;
    }
  }

  &:hover {
    background: rgba(123, 31, 42, 0.06);
  }
}
</style>

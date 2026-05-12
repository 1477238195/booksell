<template>
  <div class="hub-layout">
    <header class="hub-header">
      <div class="hub-header-left">
        <router-link to="/dashboard/home" class="hub-logo">二手书交易平台</router-link>
        <router-link v-if="backLink" :to="backLink.to" class="hub-nav-back">{{ backLink.label }}</router-link>
      </div>
      <nav class="hub-header-nav" aria-label="快捷入口">
        <router-link to="/dashboard/book/publish" class="hub-nav-btn">发布书籍</router-link>
        <router-link to="/dashboard/wanted/publish" class="hub-nav-btn">发布求购</router-link>
        <router-link to="/dashboard/book/my" class="hub-nav-btn">我的书籍</router-link>
        <router-link to="/dashboard/wanted/my" class="hub-nav-btn">我的求购</router-link>
        <router-link to="/dashboard/order/my-orders" class="hub-nav-btn" :class="{ 'router-link-active': isOrderSectionActive }">订单</router-link>
        <router-link to="/dashboard/forum/list" class="hub-nav-btn">论坛</router-link>
        <router-link to="/dashboard/message/list" class="hub-nav-btn">消息</router-link>
        <router-link v-if="isAdmin" to="/dashboard/platform/index" class="hub-nav-btn hub-nav-btn--admin">平台管理</router-link>
      </nav>
      <div class="hub-header-right">
          <router-link to="/dashboard/ai/chat" class="hub-ai-btn" title="AI书籍助手">
          <span class="hub-ai-icon">🤖</span>
          <span class="hub-ai-text">AI助手</span>
        </router-link>
        <User />
      </div>
    </header>
    <AppMain class="hub-app-main-wrap" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { AppMain, User } from '@/layout/components/index'

const route = useRoute()
const userStore = useUserStore()

const isAdmin = computed(() => Number(userStore.userInfo?.role) === 1)

const isOrderSectionActive = computed(() => route.path.startsWith('/dashboard/order'))

const backLink = computed(() => {
  const p = route.path
  if (p === '/dashboard/home' || p === '/dashboard' || p === '/') return null
  if (p === '/dashboard/profile') return { to: '/dashboard/home', label: '市集首页' }
  if (p === '/dashboard/book/index') return { to: '/dashboard/home', label: '市集首页' }
  if (p.startsWith('/dashboard/book/')) return { to: '/dashboard/home', label: '市集首页' }
  if (p === '/dashboard/platform/index') return { to: '/dashboard/home', label: '市集首页' }
  if (p.startsWith('/dashboard/platform/')) return { to: '/dashboard/platform/index', label: '平台管理' }
  if (p.startsWith('/dashboard/order')) return { to: '/dashboard/home', label: '市集首页' }
  if (p === '/dashboard/wanted/index') return { to: '/dashboard/home', label: '市集首页' }
  if (p.startsWith('/dashboard/wanted/')) return { to: '/dashboard/home', label: '市集首页' }
  if (p.startsWith('/dashboard/forum')) return { to: '/dashboard/home', label: '市集首页' }
  if (p === '/dashboard/message/list') return { to: '/dashboard/home', label: '市集首页' }
  if (p === '/dashboard/message/chat') return { to: '/dashboard/message/list', label: '消息列表' }
  return { to: '/dashboard/home', label: '市集首页' }
})
</script>

<style lang="scss" scoped>
$wine: #7b1f2a;
$wine-soft: rgba(123, 31, 42, 0.08);
$paper: #f6f1e9;
$paper-2: #faf6ef;
$orange: #e85d00;

.hub-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(180deg, $paper-2 0%, $paper 42%, #efe8dc 100%);
}

.hub-header {
  flex-shrink: 0;
  min-height: 52px;
  display: grid;
  grid-template-columns: minmax(160px, auto) 1fr auto;
  align-items: center;
  gap: 12px 16px;
  padding: 8px 16px;
  background: rgba(255, 254, 251, 0.92);
  border-bottom: 1px solid rgba(123, 31, 42, 0.1);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.85) inset;
  z-index: 10;
}

.hub-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.hub-header-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 4px 10px;
}

.hub-nav-btn {
  font-size: 13px;
  font-weight: 500;
  color: #44403c;
  text-decoration: none;
  padding: 6px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  white-space: nowrap;
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease;
  display: inline-block;

  &:hover {
    color: $wine;
    background: $wine-soft;
  }

  &.router-link-active {
    color: $wine;
    background: rgba(123, 31, 42, 0.12);
    font-weight: 600;
  }
}

.hub-nav-btn--admin {
  color: #b45309;

  &:hover {
    color: #92400e;
    background: #fffbeb;
  }

  &.router-link-active {
    color: #92400e;
    background: #fef3c7;
  }
}

.hub-logo {
  font-size: 16px;
  font-weight: 800;
  color: $wine;
  text-decoration: none;
  white-space: nowrap;
  letter-spacing: 0.04em;
  font-family: 'Songti SC', 'SimSun', serif;

  &:hover {
    color: #9a2f3c;
  }
}

.hub-nav-back {
  font-size: 13px;
  color: #57534e;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(255, 254, 251, 0.9);
  border: 1px solid rgba(44, 24, 16, 0.08);
  transition: background 0.2s;

  &:hover {
    color: $wine;
    background: #fffefb;
    border-color: rgba(255, 106, 0, 0.35);
  }
}

.hub-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.hub-ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 4px;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.hub-ai-icon {
  font-size: 16px;
}

.hub-ai-text {
  white-space: nowrap;
}

.hub-app-main-wrap {
  flex: 1;
  min-height: 0;
}

:deep(.app-main) {
  height: calc(100vh - 52px) !important;
  padding: 10px !important;
  box-sizing: border-box;
  background: transparent;
}
</style>

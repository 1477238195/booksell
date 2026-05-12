<template>
  <div class="hub-page">
    <div class="hub-bg" aria-hidden="true" />
    <div class="hub-inner">
      <header class="hub-hero">
        <p class="hub-kicker">平台管理</p>
        <h1 class="hub-title">选择要进行的操作</h1>
        <p class="hub-sub">维护全站图书、注册用户、求购信息与分类（仅管理员）</p>
      </header>

      <section class="hub-panel">
        <HubUserStrip />
        <HubTileGrid :tiles="tiles" />
        <footer class="hub-panel-foot">使用顶部「书籍管理」或「系统首页」返回</footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Reading, User, Search, Menu } from '@element-plus/icons-vue'
import HubUserStrip from './HubUserStrip.vue'
import HubTileGrid from './HubTileGrid.vue'

const tiles = [
  {
    path: '/dashboard/platform/books',
    label: '图书管理',
    hint: '管理已上架书籍（上下架、删除等）',
    icon: Reading,
    tint: 'linear-gradient(135deg, #9a2f3c 0%, #7b1f2a 100%)'
  },
  {
    path: '/dashboard/platform/users',
    label: '用户管理',
    hint: '管理普通注册用户（不含管理员）',
    icon: User,
    tint: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)'
  },
  {
    path: '/dashboard/platform/wanted',
    label: '求购管理',
    hint: '管理已发布的求购信息',
    icon: Search,
    tint: 'linear-gradient(135deg, #f59e0b 0%, #eab308 100%)'
  },
  {
    path: '/dashboard/platform/category',
    label: '分类管理',
    hint: '维护书籍分类',
    icon: Menu,
    tint: 'linear-gradient(135deg, #10b981 0%, #14b8a6 100%)'
  }
]

onMounted(() => {
  void import('@/views/platform/admin-books.vue')
  void import('@/views/platform/admin-users.vue')
  void import('@/views/platform/admin-wanted.vue')
  void import('@/views/book/category.vue')
})
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';
</style>

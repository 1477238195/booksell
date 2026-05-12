<template>
  <div class="hub-page">
    <div class="hub-bg" aria-hidden="true" />
    <div class="hub-inner">
      <header class="hub-hero">
        <p class="hub-kicker">书籍管理</p>
        <h1 class="hub-title">选择要进行的操作</h1>
        <p class="hub-sub">{{ hubSubtitle }}</p>
      </header>

      <section class="hub-panel">
        <HubUserStrip />
        <HubTileGrid :tiles="visibleTiles" />
        <footer class="hub-panel-foot">使用顶部「系统首页」返回总导航</footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { List, Collection, EditPen, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import HubUserStrip from './HubUserStrip.vue'
import HubTileGrid from './HubTileGrid.vue'

const userStore = useUserStore()
/** 与后端一致：1 = 管理员，0 = 普通用户 */
const isAdmin = computed(() => Number(userStore.userInfo?.role) === 1)

const allTiles = [
  {
    path: '/dashboard/book/list',
    label: '书籍列表',
    hint: '浏览在售二手书',
    icon: List,
    tint: 'linear-gradient(135deg, #9a2f3c 0%, #7b1f2a 100%)'
  },
  {
    path: '/dashboard/book/my',
    label: '我的书籍',
    hint: '管理已发布书目',
    icon: Collection,
    tint: 'linear-gradient(135deg, #f59e0b 0%, #eab308 100%)'
  },
  {
    path: '/dashboard/book/publish',
    label: '发布书籍',
    hint: '上架闲置好书',
    icon: EditPen,
    tint: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)'
  },
  {
    path: '/dashboard/platform/index',
    label: '平台管理',
    hint: '图书、用户、求购与分类维护（仅管理员）',
    icon: Setting,
    tint: 'linear-gradient(135deg, #10b981 0%, #14b8a6 100%)',
    adminOnly: true
  }
]

const visibleTiles = computed(() => allTiles.filter(t => !t.adminOnly || isAdmin.value))

const hubSubtitle = computed(() =>
  isAdmin.value ? '列表浏览、上架新书；平台功能请进入「平台管理」' : '列表浏览、上架新书、管理我的书目'
)

onMounted(() => {
  void import('@/views/book/list.vue')
  void import('@/views/book/my-books.vue')
  void import('@/views/book/publish.vue')
  if (isAdmin.value) void import('@/views/hub/PlatformHub.vue')
})
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';
</style>

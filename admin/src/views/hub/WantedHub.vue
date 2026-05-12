<template>
  <div class="hub-page">
    <div class="hub-bg" aria-hidden="true" />
    <div class="hub-inner">
      <header class="hub-hero">
        <p class="hub-kicker">求购信息</p>
        <h1 class="hub-title">选择要进行的操作</h1>
        <p class="hub-sub">浏览求购、管理我的求购、发布新需求</p>
      </header>

      <section class="hub-panel">
        <HubUserStrip />
        <HubTileGrid :tiles="tiles" />
        <footer class="hub-panel-foot">使用顶部「系统首页」返回总导航</footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { List, Collection, Plus } from '@element-plus/icons-vue'
import HubUserStrip from './HubUserStrip.vue'
import HubTileGrid from './HubTileGrid.vue'

const tiles = [
  {
    path: '/dashboard/wanted/list',
    label: '求购列表',
    hint: '浏览所有求购',
    icon: List,
    tint: 'linear-gradient(135deg, #9a2f3c 0%, #7b1f2a 100%)'
  },
  {
    path: '/dashboard/wanted/my',
    label: '我的求购',
    hint: '管理我发布的求购',
    icon: Collection,
    tint: 'linear-gradient(135deg, #f59e0b 0%, #eab308 100%)'
  },
  {
    path: '/dashboard/wanted/publish',
    label: '发布求购',
    hint: '发布新的求购信息',
    icon: Plus,
    tint: 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)'
  }
]
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';
</style>

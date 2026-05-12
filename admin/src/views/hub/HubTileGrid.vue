<template>
  <div class="hub-tile-grid" :class="gridClass">
    <div
      v-for="item in tiles"
      :key="item.path"
      class="hub-tile"
      role="link"
      tabindex="0"
      @click="go(item.path)"
      @keydown.enter.prevent="go(item.path)"
    >
      <div class="hub-tile-icon" :style="{ background: item.tint }">
        <el-icon :size="24"><component :is="item.icon" /></el-icon>
      </div>
      <div class="hub-tile-body">
        <span class="hub-tile-label">{{ item.label }}</span>
        <span class="hub-tile-hint">{{ item.hint }}</span>
      </div>
      <el-icon class="hub-tile-arrow"><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { spaPush } from '@/utils/spaNavigate'

const props = defineProps({
  tiles: {
    type: Array,
    required: true
  },
  single: {
    type: Boolean,
    default: false
  }
})

function go(path) {
  spaPush(path)
}

/** 按入口数量排布，避免 3 个卡片时用 2 列栅格产生右下角空洞 */
const gridClass = computed(() => {
  if (props.single) return 'is-single'
  const n = props.tiles?.length ?? 0
  if (n <= 1) return 'is-single'
  if (n === 2) return 'is-pair'
  if (n === 3) return 'is-triple'
  return 'is-quad'
})
</script>

<style lang="scss" scoped>
@import './hub-shared.scss';
</style>

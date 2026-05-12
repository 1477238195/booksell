<template>
  <div class="order-center">
    <div class="order-center__sidebar">
      <div class="order-center__side-head">
        <span class="order-center__side-title">订单中心</span>
        <span class="order-center__side-sub">买卖与订单一览</span>
      </div>
      <nav class="order-center__nav" aria-label="订单中心导航">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="order-center__nav-item"
          active-class="is-active"
        >
          <span class="order-center__nav-icon" :style="{ background: item.tint }">
            <el-icon :size="18"><component :is="item.icon" /></el-icon>
          </span>
          <span class="order-center__nav-text">
            <span class="order-center__nav-label">{{ item.label }}</span>
            <span class="order-center__nav-hint">{{ item.hint }}</span>
          </span>
        </router-link>
      </nav>
    </div>

    <div class="order-center__main">
      <header class="order-center__crumb">
        <span class="order-center__crumb-root">订单中心</span>
        <span class="order-center__crumb-sep">/</span>
        <span class="order-center__crumb-current">{{ currentTitle }}</span>
      </header>
      <div class="order-center__body">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { List, ShoppingBag, Sell, Star, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()

const navItems = [
  {
    name: 'OrderMyOrders',
    label: '我的订单',
    hint: '全部订单列表',
    icon: List,
    tint: 'linear-gradient(135deg, #9a2f3c 0%, #7b1f2a 100%)'
  },
  {
    name: 'MyPurchase',
    label: '我的买入',
    hint: '我买到的书',
    icon: ShoppingBag,
    tint: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)'
  },
  {
    name: 'MySales',
    label: '我的卖出',
    hint: '我卖出的书',
    icon: Sell,
    tint: 'linear-gradient(135deg, #059669 0%, #047857 100%)'
  },
  {
    name: 'OrderFavorites',
    label: '我的收藏',
    hint: '心仪书籍汇总',
    icon: Star,
    tint: 'linear-gradient(135deg, #d97706 0%, #b45309 100%)'
  },
  {
    name: 'OrderCart',
    label: '购物车',
    hint: '待结算书目',
    icon: ShoppingCart,
    tint: 'linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%)'
  }
]

const currentTitle = computed(() => {
  const last = route.matched[route.matched.length - 1]
  return last?.meta?.title || '我的订单'
})
</script>

<style lang="scss" scoped>
$wine: #7b1f2a;
$paper: #faf6ef;
$card: #fffefb;

.order-center {
  display: flex;
  align-items: stretch;
  gap: 0;
  min-height: calc(100vh - 72px);
  margin: -10px;
  padding: 16px 18px 28px;
  box-sizing: border-box;
  background: var(--market-gradient-page, linear-gradient(180deg, #faf6ef 0%, #f6f1e9 38%, #efe8dc 100%));
}

.order-center__sidebar {
  flex: 0 0 248px;
  width: 248px;
  background: $card;
  border: 1px solid rgba(44, 24, 16, 0.08);
  border-radius: 14px 0 0 14px;
  box-shadow: -2px 0 24px rgba(44, 24, 16, 0.05) inset;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.order-center__side-head {
  padding: 18px 16px 14px;
  border-bottom: 1px solid rgba(123, 31, 42, 0.1);
  background: linear-gradient(180deg, rgba(123, 31, 42, 0.06), transparent);
}

.order-center__side-title {
  display: block;
  font-size: 16px;
  font-weight: 800;
  color: $wine;
  letter-spacing: 0.06em;
  font-family: 'Songti SC', 'SimSun', serif;
}

.order-center__side-sub {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #78716c;
}

.order-center__nav {
  padding: 10px 0 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-center__nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 10px;
  padding: 12px 12px 12px 14px;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  border-left: 3px solid transparent;
  transition:
    background 0.18s ease,
    border-color 0.18s ease;

  &:hover {
    background: rgba(123, 31, 42, 0.06);
  }

  &.is-active {
    background: rgba(123, 31, 42, 0.1);
    border-left-color: $wine;

    .order-center__nav-label {
      color: $wine;
      font-weight: 700;
    }
  }
}

.order-center__nav-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.order-center__nav-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.order-center__nav-label {
  font-size: 14px;
  font-weight: 600;
  color: #292524;
}

.order-center__nav-hint {
  font-size: 11px;
  color: #a8a29e;
  line-height: 1.3;
}

.order-center__main {
  flex: 1;
  min-width: 0;
  background: $card;
  border: 1px solid rgba(44, 24, 16, 0.08);
  border-left: none;
  border-radius: 0 14px 14px 0;
  box-shadow: 0 8px 28px rgba(44, 24, 16, 0.06);
  display: flex;
  flex-direction: column;
}

.order-center__crumb {
  flex-shrink: 0;
  padding: 14px 20px 10px;
  font-size: 13px;
  color: #78716c;
  border-bottom: 1px solid rgba(44, 24, 16, 0.06);
}

.order-center__crumb-root {
  color: #57534e;
}

.order-center__crumb-sep {
  margin: 0 8px;
  color: #d6d3d1;
}

.order-center__crumb-current {
  font-weight: 700;
  color: $wine;
}

.order-center__body {
  flex: 1;
  min-height: 200px;
  padding: 4px 8px 16px;
  overflow: auto;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}

@media (max-width: 900px) {
  .order-center {
    flex-direction: column;
    margin: -10px -6px;
    padding: 12px;
  }

  .order-center__sidebar {
    flex: none;
    width: 100%;
    border-radius: 14px 14px 0 0;
    border-bottom: none;
  }

  .order-center__main {
    border-radius: 0 0 14px 14px;
    border-left: 1px solid rgba(44, 24, 16, 0.08);
    border-top: none;
  }

  .order-center__nav {
    flex-direction: row;
    flex-wrap: wrap;
    padding: 8px;
  }

  .order-center__nav-item {
    flex: 1 1 140px;
    margin: 4px;
    border-left: none;
    border-bottom: 3px solid transparent;

    &.is-active {
      border-left: none;
      border-bottom-color: $wine;
    }
  }
}
</style>

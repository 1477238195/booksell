<template>
  <div class="favorites-page">
    <p v-if="store.favorites.length" class="favorites-page__hint">共 {{ store.favorites.length }} 本 · 保存在本机浏览器（按账号区分）</p>
    <div v-if="store.favorites.length" class="favorites-grid">
      <article v-for="item in store.favorites" :key="item.bookId" class="fav-card">
        <router-link :to="{ name: 'BookDetail', params: { bookId: String(item.bookId) } }" class="fav-card__cover">
          <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" class="fav-card__img" lazy />
          <div v-else class="fav-card__ph">无封面</div>
        </router-link>
        <div class="fav-card__body">
          <router-link :to="{ name: 'BookDetail', params: { bookId: String(item.bookId) } }" class="fav-card__title">
            {{ item.title }}
          </router-link>
          <p class="fav-card__seller">{{ item.sellerName || '卖家' }}</p>
          <div class="fav-card__row">
            <span class="fav-card__price">¥ {{ formatPrice(item.price) }}</span>
            <el-button link type="danger" size="small" @click.prevent="store.removeFavorite(item.bookId)">取消收藏</el-button>
          </div>
        </div>
      </article>
    </div>
    <el-empty v-else description="还没有收藏书籍">
      <el-button type="primary" @click="$router.push('/dashboard')">去市集看看</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useBookMarketStore } from '@/store/bookMarket'

const store = useBookMarketStore()

onMounted(() => {
  store.loadFromStorage()
})

function formatPrice(val) {
  const n = Number(val)
  return Number.isFinite(n) ? parseFloat(n.toFixed(2)) : val
}
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 8px 12px 16px;
}

.favorites-page__hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #78716c;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.fav-card {
  border-radius: 12px;
  overflow: hidden;
  background: var(--market-card, #fffefb);
  border: 1px solid rgba(44, 24, 16, 0.08);
  transition:
    box-shadow 0.2s,
    border-color 0.2s;

  &:hover {
    border-color: rgba(255, 106, 0, 0.35);
    box-shadow: 0 8px 20px rgba(44, 24, 16, 0.08);
  }
}

.fav-card__cover {
  display: block;
  aspect-ratio: 1;
  background: linear-gradient(145deg, #f3ece4, #e8dfd4);
}

.fav-card__img {
  width: 100%;
  height: 100%;
  display: block;
}

.fav-card__ph {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #a8a29e;
}

.fav-card__body {
  padding: 10px 12px 12px;
}

.fav-card__title {
  font-size: 13px;
  font-weight: 700;
  color: #1c1917;
  text-decoration: none;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  &:hover {
    color: #7b1f2a;
  }
}

.fav-card__seller {
  margin: 6px 0 0;
  font-size: 11px;
  color: #a8a29e;
}

.fav-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
}

.fav-card__price {
  font-size: 16px;
  font-weight: 800;
  color: #d4380d;
}
</style>

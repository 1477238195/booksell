<template>
  <div class="cart-page">
    <template v-if="store.cartItems.length">
      <div class="cart-toolbar">
        <span class="cart-toolbar__hint">共 {{ store.cartTotalQty }} 件 · 数据保存在本机浏览器（按账号区分）</span>
        <el-button text type="danger" size="small" @click="clearAll">清空购物车</el-button>
      </div>
      <div class="cart-list">
        <article v-for="row in store.cartItems" :key="row.bookId" class="cart-row">
          <router-link :to="{ name: 'BookDetail', params: { bookId: String(row.bookId) } }" class="cart-row__cover">
            <el-image v-if="row.coverImage" :src="row.coverImage" fit="cover" class="cart-row__img" />
            <div v-else class="cart-row__ph">无封面</div>
          </router-link>
          <div class="cart-row__main">
            <router-link :to="{ name: 'BookDetail', params: { bookId: String(row.bookId) } }" class="cart-row__title">
              {{ row.title }}
            </router-link>
            <p class="cart-row__meta">{{ row.sellerName || '卖家' }} · 库存 {{ row.stock ?? '—' }}</p>
            <div class="cart-row__price">¥ {{ formatPrice(row.price) }}</div>
          </div>
          <div class="cart-row__qty">
            <span class="cart-row__qty-label">数量</span>
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="Math.max(1, Number(row.stock) || 999)"
              size="small"
              @update:model-value="v => onQty(row.bookId, v)"
            />
          </div>
          <div class="cart-row__actions">
            <el-button type="primary" size="small" round @click="buyOne(row)">结算本件</el-button>
            <el-button text type="danger" size="small" @click="store.removeFromCart(row.bookId)">删除</el-button>
          </div>
        </article>
      </div>
      <p class="cart-footnote">多件合并支付需后端支持；当前请逐件点击「结算本件」下单。</p>
    </template>
    <el-empty v-else description="购物车还是空的">
      <el-button type="primary" @click="$router.push('/dashboard/book/list')">去逛逛书籍</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useBookMarketStore } from '@/store/bookMarket'
import { OrderApi } from '@/api/OtherApi'

const store = useBookMarketStore()

onMounted(() => {
  store.loadFromStorage()
})

function formatPrice(val) {
  const n = Number(val)
  return Number.isFinite(n) ? parseFloat(n.toFixed(2)) : val
}

function onQty(bookId, v) {
  store.setCartQuantity(bookId, v)
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('确定清空购物车？', '提示', { type: 'warning' })
    store.cartItems = []
    store.persistCart()
  } catch {
    /* cancel */
  }
}

async function buyOne(row) {
  try {
    await ElMessageBox.confirm(`购买《${row.title}》× ${row.quantity} ？\n单价 ¥${formatPrice(row.price)}`, '确认购买', {
      confirmButtonText: '下单',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await OrderApi.createOrder(row.bookId, row.quantity)
    if (res.status === 0) {
      ElMessage.success('下单成功，请前往「我的买家订单」完成支付')
      store.removeFromCart(row.bookId)
      window.dispatchEvent(new CustomEvent('balance-updated'))
    }
  } catch {
    /* cancel or error handled by request */
  }
}
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 8px 12px 16px;
}

.cart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #78716c;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-row {
  display: grid;
  grid-template-columns: 72px 1fr auto auto;
  gap: 14px;
  align-items: center;
  padding: 12px 14px;
  background: var(--market-card, #fffefb);
  border: 1px solid rgba(44, 24, 16, 0.08);
  border-radius: 12px;
}

.cart-row__cover {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.cart-row__img {
  width: 100%;
  height: 100%;
  display: block;
}

.cart-row__ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #a8a29e;
  background: #f5f5f4;
}

.cart-row__title {
  font-weight: 700;
  color: #1c1917;
  text-decoration: none;
  font-size: 14px;
  line-height: 1.35;

  &:hover {
    color: #7b1f2a;
  }
}

.cart-row__meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #78716c;
}

.cart-row__price {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 800;
  color: #d4380d;
}

.cart-row__qty {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.cart-row__qty-label {
  font-size: 11px;
  color: #a8a29e;
}

.cart-row__actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: stretch;
}

.cart-footnote {
  margin: 14px 0 0;
  font-size: 12px;
  color: #a8a29e;
}

@media (max-width: 768px) {
  .cart-row {
    grid-template-columns: 64px 1fr;
    grid-template-rows: auto auto;
  }

  .cart-row__qty,
  .cart-row__actions {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
</style>

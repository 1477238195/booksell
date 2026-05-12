import { defineStore } from 'pinia'
import { useUserStore } from '@/store/user'

const PREFIX = 'alex-book-market'

function cartKey(uid) {
  return `${PREFIX}-cart-${uid}`
}

function favKey(uid) {
  return `${PREFIX}-favorites-${uid}`
}

export const useBookMarketStore = defineStore('bookMarket', {
  state: () => ({
    cartItems: [],
    favorites: []
  }),

  getters: {
    cartTotalQty(state) {
      return state.cartItems.reduce((s, i) => s + (Number(i.quantity) || 0), 0)
    },
    favoriteBookIds(state) {
      return new Set(state.favorites.map(f => f.bookId))
    }
  },

  actions: {
    currentUid() {
      const id = useUserStore().userInfo?.userId
      return id !== undefined && id !== null && id !== '' ? String(id) : 'guest'
    },

    loadFromStorage() {
      const uid = this.currentUid()
      try {
        const rawC = localStorage.getItem(cartKey(uid))
        this.cartItems = rawC ? JSON.parse(rawC) : []
        if (!Array.isArray(this.cartItems)) this.cartItems = []
        const rawF = localStorage.getItem(favKey(uid))
        this.favorites = rawF ? JSON.parse(rawF) : []
        if (!Array.isArray(this.favorites)) this.favorites = []
      } catch {
        this.cartItems = []
        this.favorites = []
      }
    },

    persistCart() {
      localStorage.setItem(cartKey(this.currentUid()), JSON.stringify(this.cartItems))
    },

    persistFavorites() {
      localStorage.setItem(favKey(this.currentUid()), JSON.stringify(this.favorites))
    },

    /**
     * @param {object} book — 详情或列表里的书籍对象
     */
    addToCart(book, qty = 1) {
      this.loadFromStorage()
      const bookId = book.bookId
      const maxStock = Math.max(1, Number(book.stock) || 999)
      let q = Math.max(1, Number(qty) || 1)
      const existing = this.cartItems.find(i => i.bookId === bookId)
      if (existing) {
        q = existing.quantity + q
      }
      q = Math.min(maxStock, q)
      const payload = {
        bookId,
        title: book.title,
        price: book.price,
        coverImage: book.coverImage || '',
        stock: book.stock,
        sellerId: book.sellerId,
        sellerName: book.sellerName || '',
        quantity: q
      }
      if (existing) {
        existing.quantity = q
        existing.price = book.price
        existing.stock = book.stock
      } else {
        this.cartItems.push(payload)
      }
      this.persistCart()
    },

    removeFromCart(bookId) {
      this.cartItems = this.cartItems.filter(i => i.bookId !== bookId)
      this.persistCart()
    },

    setCartQuantity(bookId, quantity) {
      const row = this.cartItems.find(i => i.bookId === bookId)
      if (!row) return
      const maxStock = Math.max(1, Number(row.stock) || 999)
      row.quantity = Math.max(1, Math.min(maxStock, Number(quantity) || 1))
      this.persistCart()
    },

    toggleFavorite(book) {
      this.loadFromStorage()
      const bookId = book.bookId
      const idx = this.favorites.findIndex(f => f.bookId === bookId)
      if (idx >= 0) {
        this.favorites.splice(idx, 1)
      } else {
        this.favorites.push({
          bookId,
          title: book.title,
          price: book.price,
          coverImage: book.coverImage || '',
          sellerName: book.sellerName || '',
          addedAt: Date.now()
        })
      }
      this.persistFavorites()
    },

    removeFavorite(bookId) {
      this.favorites = this.favorites.filter(f => f.bookId !== bookId)
      this.persistFavorites()
    },

    isFavorite(bookId) {
      return this.favorites.some(f => f.bookId === bookId)
    }
  }
})

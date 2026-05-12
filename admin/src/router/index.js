import {
  createRouter,
  createWebHashHistory
} from 'vue-router'
import HubLayout from '@/layout/HubLayout/index.vue'
import { usePermissionStore } from '@/store/permission'
import MarketHome from '@/views/home/MarketHome.vue'

/**
 * 路由设计原则：
 * - /dashboard 作为独立路由，使用 HubLayout，不再作为 / 的嵌套子路由
 * - / 根路径只做重定向，避免 redirect + children 同时存在导致的路由匹配混乱
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true
  },
  // 根路径重定向，不带任何嵌套子路由
  { path: '/', redirect: '/dashboard' },
  // 市集首页 — 独立路由，作为 HubLayout 的直接子路由
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: HubLayout,
    redirect: '/dashboard/home',
    meta: { title: '市集首页' },
    children: [
      {
        path: 'home',
        name: 'MarketHome',
        component: MarketHome,
        meta: { title: '市集首页' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人信息', icon: 'el-icon-User' }
      },
      {
        path: 'profile/edit',
        name: 'EditProfile',
        component: () => import('@/views/profile/edit.vue'),
        meta: { title: '修改个人信息' }
      },
      { path: 'book', redirect: '/dashboard/book/index' },
      {
        path: 'book/index',
        name: 'BookHub',
        component: () => import('@/views/hub/BookHub.vue'),
        meta: { title: '书籍管理', icon: 'el-icon-Reading' }
      },
      {
        path: 'book/list',
        name: 'BookList',
        component: () => import('@/views/book/list.vue'),
        meta: { title: '书籍列表', icon: 'el-icon-List' }
      },
      {
        path: 'book/detail/:bookId',
        name: 'BookDetail',
        component: () => import('@/views/book/detail.vue'),
        meta: { title: '书籍详情' }
      },
      {
        path: 'book/edit/:bookId',
        name: 'EditBook',
        component: () => import('@/views/book/edit.vue'),
        meta: { title: '编辑书籍' }
      },
      {
        path: 'book/my',
        name: 'MyBooks',
        component: () => import('@/views/book/my-books.vue'),
        meta: { title: '我的书籍', icon: 'el-icon-Collection' }
      },
      {
        path: 'book/publish',
        name: 'PublishBook',
        component: () => import('@/views/book/publish.vue'),
        meta: { title: '发布书籍', icon: 'el-icon-Plus' }
      },
      {
        path: 'book/category',
        name: 'BookCategory',
        component: () => import('@/views/book/category.vue'),
        meta: {
          title: '分类管理',
          icon: 'el-icon-Menu',
          roles: [1]
        }
      },
      { path: 'platform', redirect: '/dashboard/platform/index' },
      {
        path: 'platform/index',
        name: 'PlatformHub',
        component: () => import('@/views/hub/PlatformHub.vue'),
        meta: { title: '平台管理', icon: 'el-icon-Setting', roles: [1] }
      },
      {
        path: 'platform/books',
        name: 'PlatformBooks',
        component: () => import('@/views/platform/admin-books.vue'),
        meta: { title: '图书管理', roles: [1] }
      },
      {
        path: 'platform/users',
        name: 'PlatformUsers',
        component: () => import('@/views/platform/admin-users.vue'),
        meta: { title: '用户管理', roles: [1] }
      },
      {
        path: 'platform/wanted',
        name: 'PlatformWanted',
        component: () => import('@/views/platform/admin-wanted.vue'),
        meta: { title: '求购管理', roles: [1] }
      },
      {
        path: 'platform/category',
        name: 'PlatformCategory',
        component: () => import('@/views/book/category.vue'),
        meta: { title: '分类管理', roles: [1] }
      },
      {
        path: 'order',
        component: () => import('@/views/order/OrderCenter.vue'),
        redirect: { name: 'OrderMyOrders' },
        meta: { title: '订单中心', icon: 'el-icon-ShoppingCart' },
        children: [
          {
            path: 'my-orders',
            name: 'OrderMyOrders',
            component: () => import('@/views/order/list.vue'),
            meta: { title: '我的订单', icon: 'el-icon-List' }
          },
          {
            path: 'my-purchase',
            name: 'MyPurchase',
            component: () => import('@/views/order/my-purchase.vue'),
            meta: { title: '我的买入', icon: 'el-icon-ShoppingBag' }
          },
          {
            path: 'my-sales',
            name: 'MySales',
            component: () => import('@/views/order/my-sales.vue'),
            meta: { title: '我的卖出', icon: 'el-icon-Sell' }
          },
          {
            path: 'favorites',
            name: 'OrderFavorites',
            component: () => import('@/views/order/favorites.vue'),
            meta: { title: '我的收藏', icon: 'el-icon-Star' }
          },
          {
            path: 'cart',
            name: 'OrderCart',
            component: () => import('@/views/order/cart.vue'),
            meta: { title: '购物车', icon: 'el-icon-ShoppingCart' }
          }
        ]
      },
      { path: 'order/index', redirect: { name: 'OrderMyOrders' } },
      { path: 'order/list', redirect: { name: 'OrderMyOrders' } },
      { path: 'wanted', redirect: '/dashboard/wanted/index' },
      {
        path: 'wanted/index',
        name: 'WantedHub',
        component: () => import('@/views/hub/WantedHub.vue'),
        meta: { title: '求购信息', icon: 'el-icon-Search' }
      },
      {
        path: 'wanted/detail/:wantedId',
        name: 'WantedDetail',
        component: () => import('@/views/wanted/detail.vue'),
        meta: { title: '求购详情' }
      },
      {
        path: 'wanted/list',
        name: 'WantedList',
        component: () => import('@/views/wanted/list.vue'),
        meta: { title: '求购列表', icon: 'el-icon-List' }
      },
      {
        path: 'wanted/my',
        name: 'MyWanted',
        component: () => import('@/views/wanted/my-wanted.vue'),
        meta: { title: '我的求购', icon: 'el-icon-Collection' }
      },
      {
        path: 'wanted/publish',
        name: 'PublishWanted',
        component: () => import('@/views/wanted/publish.vue'),
        meta: { title: '发布求购', icon: 'el-icon-Plus' }
      },
      { path: 'forum', redirect: '/dashboard/forum/list' },
      {
        path: 'forum/list',
        name: 'ForumList',
        component: () => import('@/views/forum/list.vue'),
        meta: { title: '书友论坛', icon: 'el-icon-ChatDotRound' }
      },
      {
        path: 'forum/topic/:topicId',
        name: 'ForumTopic',
        component: () => import('@/views/forum/topic.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'forum/publish',
        name: 'ForumPublish',
        component: () => import('@/views/forum/publish.vue'),
        meta: { title: '发布帖子' }
      },
      { path: 'message', redirect: '/dashboard/message/list' },
      { path: 'message/index', redirect: '/dashboard/message/list' },
      {
        path: 'message/list',
        name: 'MessageList',
        component: () => import('@/views/message/list.vue'),
        meta: { title: '消息列表', icon: 'el-icon-Message' }
      },
      {
        path: 'message/chat',
        name: 'MessageChat',
        component: () => import('@/views/message/chat.vue'),
        meta: { title: '消息对话', icon: 'el-icon-ChatLineRound' },
        hidden: true
      },
      {
        path: 'ai/chat',
        name: 'AiChat',
        component: () => import('@/views/ai/chat.vue'),
        meta: { title: 'AI书籍助手', icon: 'el-icon-Message' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error-page/404'),
    hidden: true
  }
]

export const asyncRoutes = []

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export const resetRouter = () => {
  try {
    const permissionStore = usePermissionStore()

    permissionStore.addRoutes.forEach(route => {
      const { name } = route
      if (name) {
        try {
          router.removeRoute(name)
        } catch (e) {
          // ignore
        }
      }
    })

    permissionStore.resetRoutes()
  } catch (e) {
    // pinia not ready yet, ignore
  }

  if (window.__resetRoutesFlag) {
    window.__resetRoutesFlag()
  }
}

export default router

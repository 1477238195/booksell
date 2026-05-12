import router, { asyncRoutes } from '@/router'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getCookieItem } from './utils/storage'
import getPageTitle from './utils/get-page-title'

let isRoutesAdded = false

window.__resetRoutesFlag = () => {
  isRoutesAdded = false
}

NProgress.configure({ showSpinner: false })

const whiteList = [
  '/login', '/auth-redirect', '/register',
  '/' // 根路径只做重定向，无需登录
]

// 导航锁：防止并发导航导致页面跳回
let isNavigating = false
let pendingNavigation = null

router.beforeEach(async (to, from, next) => {
  // 如果有正在进行的导航，等待它完成
  if (isNavigating && pendingNavigation) {
    console.log('[路由守卫] 有进行中的导航，等待完成...')
    await pendingNavigation.catch(() => {})
  }

  NProgress.start()

  const leafMeta = to.matched[to.matched.length - 1]?.meta
  document.title = getPageTitle(leafMeta?.title ?? to.meta.title)
  const hasToken = getCookieItem('token') || ''

  if (hasToken) {
    if (to.path === '/login') {
      next()
      NProgress.done()
      return
    }

    if (whiteList.indexOf(to.path) !== -1) {
      next()
      NProgress.done()
      return
    }

    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 如果有 token 但没有 userInfo，先获取用户信息
    if (!userStore.userInfo) {
      console.log('[路由守卫] 有 token 但无 userInfo，尝试获取用户信息...')
      try {
        const { default: AuthApi } = await import('@/api/AuthApi')
        const res = await AuthApi.getCurrentUser()
        if (res.status === 0 && res.data) {
          userStore.setUserInfo({ userInfo: res.data })
          console.log('[路由守卫] 用户信息获取成功:', res.data)
        }
      } catch (e) {
        console.error('[路由守卫] 获取用户信息失败:', e)
      }
    }

    const leaf = to.matched[to.matched.length - 1]
    const roleLimit = leaf?.meta?.roles
    if (Array.isArray(roleLimit) && roleLimit.length > 0 && userStore.userInfo) {
      const ur = Number(userStore.userInfo.role)
      if (!roleLimit.map(Number).includes(ur)) {
        ElMessage.warning('无权限访问该页面')
        next({ path: '/dashboard', replace: true })
        NProgress.done()
        return
      }
    }

    try {
      const hasRoles = userStore.userInfo && (
        userStore.userInfo.role !== undefined ||
        (userStore.userInfo.roles && userStore.userInfo.roles.length > 0)
      )

      // 只有在有角色信息时才处理动态路由
      if (hasRoles) {
        let accessRoutes = permissionStore.restoreRoutesFromCache()

        if (accessRoutes.length === 0) {
          accessRoutes = await permissionStore.generateRoutes()
        }

        const routesWereAdded = accessRoutes && accessRoutes.length > 0 && !isRoutesAdded

        if (accessRoutes && accessRoutes.length > 0) {
          accessRoutes.forEach(route => {
            if (route.name) {
              try {
                router.removeRoute(route.name)
              } catch (e) {}
            }
            router.addRoute(route)
          })
          isRoutesAdded = true
        }

        if (routesWereAdded) {
          next({ ...to, replace: true })
          NProgress.done()
          return
        }
      }

      next()
      NProgress.done()
    } catch (error) {
      next()
      NProgress.done()
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
    NProgress.done()
  }
})

router.afterEach(() => {
  NProgress.done()
})

import router from '@/router'

/**
 * 路由导航函数，直接调用 router.push
 */
export function spaPush(path) {
  return router.push(path)
}

/**
 * 将用户对象或路径转为可用于 <img> / el-avatar 的地址（本地上传为 /upload/...）
 */
export function resolveAvatarUrl(userOrPath) {
  const raw =
    typeof userOrPath === 'string'
      ? userOrPath
      : (userOrPath?.avatar || userOrPath?.headUrl || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw) || raw.startsWith('data:')) return raw
  return raw.startsWith('/') ? raw : `/${raw}`
}

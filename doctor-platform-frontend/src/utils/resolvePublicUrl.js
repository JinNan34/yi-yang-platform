/**
 * 将后端返回的公开资源路径规范为可安全用于 img/src 的同源路径。
 * 兼容历史错误拼接 `//files/...`（协议相对 URL）及 `/files/...` 等形式。
 */
export function resolvePublicUrl(path) {
  if (!path) return ''
  const s = String(path).trim()
  if (/^https?:\/\//i.test(s)) return s
  if (s.startsWith('//')) {
    const rest = s.replace(/^\/+/, '')
    if (rest.startsWith('files/')) return '/api/' + rest
    if (rest.startsWith('api/')) return '/' + rest
    return '/api/' + rest
  }
  if (s.startsWith('/api/')) return s
  if (s.startsWith('/files/')) return '/api' + s
  return s
}

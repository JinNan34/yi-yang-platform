/** 仅允许站内相对路径，防止开放重定向 */
export function safeRedirectPath(raw) {
  const r = typeof raw === 'string' ? raw : ''
  if (!r || !r.startsWith('/') || r.startsWith('//') || r.includes('://')) return '/'
  return r
}

import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import router from '../router'

const baseURL = (import.meta.env.VITE_API_BASE?.trim() || '/api').replace(/\/$/, '') || '/api'

let handling401 = false

function extractMessage(err) {
  const body = err.response?.data
  if (body && typeof body.message === 'string' && body.message) return body.message
  if (err.code === 'ECONNABORTED') return '请求超时，请稍后重试'
  if (!err.response) return '无法连接服务器，请检查网络或后端是否已启动'
  return err.message || '网络错误'
}

const http = axios.create({
  baseURL,
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const user = useUserStore()
  if (user.token) {
    config.headers.Authorization = `Bearer ${user.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number') {
      if (body.code !== 0) {
        ElMessage.error(body.message || '请求失败')
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return res.data
  },
  (err) => {
    const status = err.response?.status
    const msg = extractMessage(err)
    const reqUrl = err.config?.url || ''
    const isLoginAttempt = reqUrl.includes('/auth/login')

    if (status === 401 && isLoginAttempt) {
      ElMessage.error(msg || '用户名或密码错误')
      return Promise.reject(err)
    }

    if (status === 401) {
      const user = useUserStore()
      user.clear()
      if (!handling401) {
        handling401 = true
        const current = router.currentRoute.value
        const redirect = current.name !== 'login' ? current.fullPath : undefined
        router
          .push({ name: 'login', ...(redirect ? { query: { redirect } } : {}) })
          .finally(() => {
            setTimeout(() => {
              handling401 = false
            }, 600)
          })
      }
      return Promise.reject(err)
    }

    if (status === 403) {
      ElMessage.error(msg || '无权限执行此操作')
      return Promise.reject(err)
    }

    if (isLoginAttempt && !err.response) {
      return Promise.reject(err)
    }

    if (status === 429) {
      ElMessage.warning(msg || '请求过于频繁，请稍后再试')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default http

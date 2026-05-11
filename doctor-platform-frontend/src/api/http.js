import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '/api',
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
        return Promise.reject(new Error(body.message))
      }
      return body.data
    }
    return res.data
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      const user = useUserStore()
      user.clear()
      router.push('/login')
    }
    const body = err.response?.data
    const msg =
      (body && typeof body.message === 'string' && body.message) ||
      err.message ||
      '网络错误'
    if (status === 429) {
      ElMessage.warning(msg)
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default http
